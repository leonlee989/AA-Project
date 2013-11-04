// import
var bidModule = require("./Bid");
var askModule = require("./Ask");
var matchedTransactionModule = require("./MatchedTransaction");
var fs = require('fs');
var request = require('request');

var db_module = require("./DBBean");
var db = undefined;

function ExchangeBean() {
	// location of log files - change if necessary
	this.MATCH_LOG_FILE = "c:\\temp\\matched.log";
	this.REJECTED_BUY_ORDERS_LOG_FILE = "c:\\temp\\rejected.log";

	// used to calculate remaining credit available for buyers
	this.DAILY_CREDIT_LIMIT_FOR_BUYERS = 1000000000;

	// keeps track of the lastest price for each of the 3 stocks
	this.latestPriceForSmu = -1;
	this.latestPriceForNus = -1;
	this.latestPriceForNtu = -1;

	 // Establishing connection to mysql cluster
	 db = new db_module.DBBean();
	 db.estab_connection();
 }
 
 // this method is called once at the end trading day. It can be called manually, or by a timed daemon
 // this is a good chance to "clean up" everything to get ready for the next trading
 ExchangeBean.prototype.endTradingDay = function() {
	
	//try {
		//db.estab_connection();
		
		// reset attributes
	this.latestPriceForSmu = -1;
	this.latestPriceForNus = -1;
	this.latestPriceForNtu = -1;
	
	//dump all unfulfilled buy and sell orders
	db.executeUpdate("delete from ask");
	db.executeUpdate("delete from bid");
	
	// reset all credit limits of users
	db.executeUpdate("delete from credit");
	//} catch (err) {
		//console.log("Connecting database failed");
	//} finally {
		//db.close();
	//};
 }
 
 ExchangeBean.prototype.sendToBackOffice = function(IPAddress) {	
	var module = this;
	
	var domain = "10.0.106.239";
	if (IPAddress != "") {
		// Open Secondary Connection
		console.log("Secondary connection opened");
		domain = IPAddress;
	}
	
	var url = "http://" + domain + ":81/aabo/Service.asmx/ProcessTransaction"
	var post_data = {
	  'teamId' : 'G3T7',
	  'teamPassword': 'lime'
	};
			
	var rs = fs.createReadStream(this.MATCH_LOG_FILE);
	rs.setEncoding("utf8");
	
	// File Opened
	rs.once('open', function(fd) {  
		console.log( 'File Opened' );  
	});  
	
	// File Closed
	rs.once('close', function() {  
		console.log( 'File closed');  
	});  

	// File Read
	rs.once('data', function(data) {  
		console.log( 'Reading data...' );  
		console.log( data );  
		var value = data.split("\n");

		value.forEach(function(matchedTransactions) {
			
			if (matchedTransactions != "") {
				
				post_data.transactionDescription = matchedTransactions;
				request.post(
					url,
					{form: post_data},
					function (error, response, body) {
						
						if (!error && response.statusCode == 200) {
							console.log(body);
							module.renderResponse(body);
						} else {
							console.log("Connection Failed: Page cannot be loaded");		
							module.sendToBackOffice("10.4.12.30");
						}
					}
				);
			}
		});
		
		// Remove data from file
		fs.writeFile(module.MATCH_LOG_FILE, "", function(err) {
			if (!err) {
				console.log("Content is being removed from " + module.MATCH_LOG_FILE);
			} else {
				console.log("Error in removing the content");
			}
		});
	});  

	// File error
	rs.once('error', function(exception) {  
		console.log( 'rs_exception...' );  
		console.log( exception );  
	});  
}

// condition set when when response is given back from the remote server
ExchangeBean.prototype.renderResponse = function(body) {
	var index = body.search("true");
	
	if (index == -1) {
		console.log("Alert - Log authentication to the remote server failed");
	}
}

 // returns a String of unfulfilled bids for a particular stocks
 // returns an empty string if no such bid
 // bods are separated by <br> for display on HTML page
ExchangeBean.prototype.getUnfulfilledBidsForDisplay = function(stock, callback) {
	var returnString = "";
		
	db.executeSql("select * from bid", function(value) {
		value.forEach(function(result) {
			if (result.stockName == stock) {
				returnString += JSON.stringify(result);
			}			
		});
		
		callback(returnString);
	});
 }
 
 // return a String of unfilled asks for a particular stock
 // returns an empty string if no such ask
 // asks are separated by <br> for display on HTML page
 ExchangeBean.prototype.getUnfulfilledAsks = function(stock, callback) {
	var returnString = "";
	
	db.executeSql("select * from ask", function(value) {
		value.forEach(function(result) {
			if (result.stockName == stock) {
				returnString += JSON.stringify(result);
			}			
		});
		
		callback(returnString);
	});
 }
 
// returns the highest bid for a particular stock
// return -1 if there is no bid at all
ExchangeBean.prototype.getHighestBidPrice = function(stock, callback) {
	this.getHighestBid(stock, function(highestBid) {
		if (highestBid === undefined) {
			callback(-1);
		} else {
			callback(highestBid.getPrice());
		}
	});
}
 
// retrieve unfulfiled current (highest) bid for a particular stock
// return null if there is no unfulfiled bid for this stock	
ExchangeBean.prototype.getHighestBid = function(stock, callback) {
	var highestBid = new bidModule.Bid(undefined, 0, undefined, undefined);
	
	db.executeSql("select * from bid", function(results) {
		results.forEach(function(result_bid) {
			
			if (result_bid.stockName == stock && result_bid.price >= highestBid.getPrice()) {
				// There are 2 sets of the same ask amount, the earlier one is considered the highest ask
				if (result_bid.price == highestBid.getPrice()) {
					
					if ((new Date(result_bid.bidDate)).toLocaleString() < highestBid.getDate().toLocaleString()) {
						highestBid = new bidModule.Bid(result_bid.stockName, result_bid.price, result_bid.userID, new Date(result_bid.bidDate));
					}
				} else {
					highestBid = new bidModule.Bid(result_bid.stockName, result_bid.price, result_bid.userID, new Date(result_bid.bidDate));
				}
			} 
		});

		if (highestBid.getUserId() === undefined) {
			callback(undefined);
		}
		
		callback(highestBid);
	});	
}
 
// returns the lowest ask for a particular stock
// returns -1 if there is no ask at all
ExchangeBean.prototype.getLowestAskPrice = function(stock, callback) {
	this.getLowestAsk(stock, function(lowestAsk) {
		if (lowestAsk === undefined) {
			callback(-1);
		} else {
			callback(lowestAsk.getPrice());
		}
	});
}
  
// retrieve unfulfiled current (lowest) ask for a particular stock
// returns null if there is no unfulfiled asks for this stock
ExchangeBean.prototype.getLowestAsk = function(stock, callback) {
	
	var lowestAsk = new askModule.Ask(undefined, Number.MAX_VALUE, undefined, undefined);
	
	db.executeSql("select * from ask", function(results) {
		results.forEach(function(result_Ask) {
			
			if (result_Ask.stockName == stock && result_Ask.price <= lowestAsk.getPrice()) {
				// There are 2 sets of the same ask amount, the earlier one is considered the highest ask
				if (result_Ask.price == lowestAsk.getPrice()) {
					
					if ((new Date(result_Ask.askDate)).toLocaleString() < lowestAsk.getDate().toLocaleString()) {
						lowestAsk = new askModule.Ask(result_Ask.stockName, result_Ask.price, result_Ask.userID, new Date(result_Ask.askDate));
					}
				} else {
					lowestAsk = new askModule.Ask(result_Ask.stockName, result_Ask.price, result_Ask.userID, new Date(result_Ask.askDate));
				}
			} 
		});
		
		if (lowestAsk.getUserId() === undefined) {
			callback(undefined);
		}
		callback(lowestAsk);
	});
}

ExchangeBean.prototype.getCreditRemaining = function(buyerUserId, callback) {

	var module = this;
	db.executeSql("select credit_limit from credit where userid='" + buyerUserId + "'", function(value) {
		
		if (value != undefined && value != "") {
			//console.log(JSON.stringify(value[0]));
			callback(value[0].credit_limit);
		} else {
			db.executeUpdate("insert into credit (userid, credit_limit) values ('" + buyerUserId + "', " + module.DAILY_CREDIT_LIMIT_FOR_BUYERS + ")");
			db.executeSql("select credit_limit from credit where userid='" + buyerUserId + "'", function(value) {
				callback(value.credit_limit);
			});
		}
	});
}

// check if a buyer is eligible to place an order based on his credit limit
// if he is eligible, this method adjusts his credit limit and returns true
// if he is not eligible, this method logs the bid and returns false
ExchangeBean.prototype.validateCreditLimit = function(bid, callback) {
	var module = this;
	
	this.getCreditRemaining(bid.getUserId(), function(creditRemaining) {
		var totalPriceOfBid = bid.getPrice() * 1000; //each bid is for 1000 shares
		var newRemainingCredit = creditRemaining - totalPriceOfBid

		if (newRemainingCredit < 0) {
			// no go - log failed bid and return false
			module.logRejectedBuyOrder(bid);
			callback(false);
		} else {
			// it's ok - adjust credit limit and return true
			
			//this.creditRemaining[bid.getUserId()] = newRemainingCredit;
			db.executeUpdate("update credit set credit_limit=" + newRemainingCredit + " where userid='" + bid.getUserId() + "'");
			callback(true);
		}
	});
}

// call this to append all rejected buy orders to log file
ExchangeBean.prototype.logRejectedBuyOrder = function(bid) {
	
	// Store in database
	db.executeUpdate("insert into rejectedlog (logStatement) values ('" + bid.toString() + "')");
	
	fs.appendFile(this.REJECTED_BUY_ORDERS_LOG_FILE, bid.toString() + "\n", function(err) {
		if(err) {
			console.log(err);
		} else {
			console.log("The file was saved!");
		}
	});
}

// call this to append all matched transactions in matchedTransactions to log file and clear matchedTransactions
ExchangeBean.prototype.logMatchedTransactions = function(transaction) {
	var log_File = this.MATCH_LOG_FILE;
	
	//Store in log file
	fs.appendFile(log_File, transaction.toString() + "\n", function(err) {
		if(err) {
			console.log(err);
		} else {
			console.log("The file was saved!");
		}
	});
	
	// Store in database
	db.executeUpdate("insert into matchedlog (logStatement) values ('" + transaction.toString() + "')");
}

// returns a string of HTML table rows code containing the list of user IDs and their remaining credits
// this method is used by viewOrders.jsp for debugging purposes
ExchangeBean.prototype.getAllCreditRemainingForDisplay = function(callback) {
	var returnString = "";
	
	db.executeSql("select * from credit", function(results) {
		results.forEach(function(value) {
			returnString += "<tr><td>" + value.userid + "</td><td>" + value.credit_limit + "</td></tr>";
		});

		callback(returnString);
	});
}

// call this method immediatley when a new bid (buying order) comes in
// this method returns false if this buy order has been rejected because of a credit limit breach
// it returns true if the bid has been successfully added
ExchangeBean.prototype.placeNewBidAndAttemptMatch = function(newBid, callback) {
	var module = this;
	//throw new Error("hello world");
	this.validateCreditLimit(newBid, function(value) {
		
		if (!value) {
			callback(false);
			return;
		}
		
		// step 1: insert new bid into unfulfilledBids
		db.executeUpdate("insert into bid (stockName, price, userID, bidDate) values ('" + newBid.getStock() + "', " + newBid.getPrice() + ", '" + 
			newBid.getUserId() + "', '" + convertToTimeStamp(newBid.getDate()) + "')");
		
		// step 2: check if there is any unfulfilled asks (sell orders) for the new bid's stock. if not, just return
		// count keeps track of the number of unfulfilled asks for this stock
		db.executeSql("select count(*) as total from ask where stockName='" + newBid.getStock() + "'", function(results) {
			
			if (results[0].total == 0) {
				console.log("No transaction is matched");
				callback(true);
				return;
			}
			
			// step 3: identify the current/highest bid in unfulfilledBids of the same stock
			module.getHighestBid(newBid.getStock(), function(highestBid) {
			
				// step 4: identify the current/lowest ask in unfulfilledAsks of the same stock
				module.getLowestAsk(newBid.getStock(), function(lowestAsk) {
					
					// step 5: check if there is a match.
					// A match happens if the highest bid is bigger or equal to the lowest ask
					if (highestBid.getPrice() >= lowestAsk.getPrice()) {
						// a match is found
						db.executeUpdate("delete from bid where stockName='" + highestBid.getStock() + "' and price=" + highestBid.getPrice()
							+ " and userID='" + highestBid.getUserId() + "' and bidDate='" + convertToTimeStamp(highestBid.getDate()) + "'");
						
						db.executeUpdate("delete from ask where stockName='" + lowestAsk.getStock() + "' and price=" + lowestAsk.getPrice()
							+ " and userID='" + lowestAsk.getUserId() + "' and askDate='" + convertToTimeStamp(lowestAsk.getDate()) + "'");
							
						// this is a BUYING trade - the transaction happens at the higest bid's timestamp, and the transaction price happens at the lowest ask
						var match = new matchedTransactionModule.MatchedTransaction(highestBid, lowestAsk, highestBid.getDate(), lowestAsk.getPrice());
						
						db.executeUpdate("insert into matchedtransactiondb (bidPrice, bidUserID, bidDate, askPrice, askUserID, askDate, matchDate, price, stockName) values (" +
							highestBid.getPrice() + ", '" +
							highestBid.getUserId() + "', '" + 
							convertToTimeStamp(highestBid.getDate()) + "', " +
							lowestAsk.getPrice() + ", '" +
							lowestAsk.getUserId() + "', '" + 
							convertToTimeStamp(lowestAsk.getDate()) + "', '" + 
							convertToTimeStamp(match.getDate()) + "', " + 
							match.getPrice() + ", '" +
							match.getStock() + "')");
						
						// to be included here: inform Back Office Server of match
						// to be done in v1.0
						module.updateLatestPrice(match);
						module.logMatchedTransactions(match);
					}
					
					console.log("Transaction is matched");
					callback(true); // this bid is acknowledged
					return;
				
				});
			});
		});
	});
}

// call this method immediatley when a new ask (selling order) comes in
ExchangeBean.prototype.placeNewAskAndAttemptMatch = function(newAsk, callback) {

	var module = this;
	
	// step 1: insert new ask into unfulfilledAsks
	db.executeUpdate("insert into ask (stockName, price, userID, askDate) values ('" + newAsk.getStock() + "', " + newAsk.getPrice() + ", '" + 
		newAsk.getUserId() + "', '" + convertToTimeStamp(newAsk.getDate()) + "')");
	
	// step 2: check if there is any unfulfilled bids (buy orders) for the new ask's stock. if not, just return
    // count keeps track of the number of unfulfilled bids for this stock
	var count = 0;
	db.executeSql("select count(*) as total from bid where stockName='" + newAsk.getStock() + "'", function(results) {
		
		if (results[0].total == 0) {
			console.log("No transaction is matched");
			//callback(true);
			return; //true; // no unfulfilled asks of the same stock
		}
		
		 // step 3: identify the current/highest bid in unfulfilledBids of the same stock
		 module.getHighestBid(newAsk.getStock(), function(highestBid) { 
		 
			// step 4: identify the current/lowest ask in unfulfilledAsks of the same stock
			module.getLowestAsk(newAsk.getStock(), function(lowestAsk) {
			 
				 // step 5: check if there is a match.
				// A match happens if the lowest ask is <= highest bid
				if (lowestAsk.getPrice() <= highestBid.getPrice()) {
					// a match is found		
					db.executeUpdate("delete from bid where stockName='" + highestBid.getStock() + "' and price=" + highestBid.getPrice()
							+ " and userID='" + highestBid.getUserId() + "' and bidDate='" + convertToTimeStamp(highestBid.getDate()) + "'");
							
					db.executeUpdate("delete from ask where stockName='" + lowestAsk.getStock() + "' and price=" + lowestAsk.getPrice()
								+ " and userID='" + lowestAsk.getUserId() + "' and askDate='" + convertToTimeStamp(lowestAsk.getDate()) + "'");
					
					// this is a SELLING trade - the transaction happens at the lowest ask's timestamp, and the transaction price happens at the highest bid
					var match = new matchedTransactionModule.MatchedTransaction(highestBid, lowestAsk, lowestAsk.getDate(), highestBid.getPrice());
					
					db.executeUpdate("insert into matchedtransactiondb (bidPrice, bidUserID, bidDate, askPrice, askUserID, askDate, matchDate, price, stockName) values (" +
						highestBid.getPrice() + ", '" +
						highestBid.getUserId() + "', '" + 
						convertToTimeStamp(highestBid.getDate()) + "', " +
						lowestAsk.getPrice() + ", '" +
						lowestAsk.getUserId() + "', '" + 
						convertToTimeStamp(lowestAsk.getDate()) + "', '" + 
						convertToTimeStamp(match.getDate()) + "', " + 
						match.getPrice() + ", '" +
						match.getStock() + "')");
					
					// to be included here: inform Back Office Server of match
					// to be done in v1.0
					module.updateLatestPrice(match);
					module.logMatchedTransactions(match);
				}
			});
			//callback(true);
			console.log("Transaction is matched");
			return;
		});
	});
}

// updates either latestPriceForSmu, latestPriceForNus or latestPriceForNtu
// based on the MatchedTransaction object passed in
ExchangeBean.prototype.updateLatestPrice = function(matched) {
	var stock = matched.getStock();
	var price = matched.getPrice();
	// update the correct attribute
	if (stock == "smu") {
		this.latestPriceForSmu = price;
	} else if (stock == "nus") {
		this.latestPriceForNus = price;
	} else if (stock = "ntu") {
		this.latestPriceForNtu = price;
	}
}

// updates either latestPriceForSmu, latestPriceForNus or latestPriceForNtu
// based on the MatchedTransaction object passed in
ExchangeBean.prototype.getLatestPrice = function(stock) {
	if (stock == "smu") {
		return this.latestPriceForSmu;
	} else if (stock == "nus") {
		return this.latestPriceForNus;
	} else if (stock = "ntu") {
		return this.latestPriceForNtu;
	}
	return -1 // no such stock
}

function convertToTimeStamp(date) {
	
	return date.getFullYear() + '-' +
    ('00' + (date.getMonth()+1)).slice(-2) + '-' +
    ('00' + date.getDate()).slice(-2) + ' ' + 
    ('00' + date.getHours()).slice(-2) + ':' + 
    ('00' + date.getMinutes()).slice(-2) + ':' + 
    ('00' + date.getSeconds()).slice(-2);

}

module.exports.ExchangeBean = ExchangeBean;