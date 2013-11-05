// import
var bidModule = require("./Bid");
var askModule = require("./Ask");
var matchedTransactionModule = require("./MatchedTransaction");
var fs = require('fs');
var request = require('request');

var db_module = require("./DBBean");
var db = undefined;

function ExchangeBean() {
	 // Establishing connection to mysql cluster
	 db = new db_module.DBBean();
	 db.estab_connection();
	 
	db.executeSql("SET SQL_SAFE_UPDATES=0", [], function(value) {
		console.log(value);
	});
	 
	// location of log files - change if necessary
	this.MATCH_LOG_FILE = "c:\\temp\\matched.log";
	this.REJECTED_BUY_ORDERS_LOG_FILE = "c:\\temp\\rejected.log";

	// used to calculate remaining credit available for buyers
	this.DAILY_CREDIT_LIMIT_FOR_BUYERS = 1000000000;
 }
 
 // this method is called once at the end trading day. It can be called manually, or by a timed daemon
 // this is a good chance to "clean up" everything to get ready for the next trading
 ExchangeBean.prototype.endTradingDay = function() {
	
	// reset attributes
	updateLatestPrice("smu", -1);
	updateLatestPrice("nus", -1);
	updateLatestPrice("ntu", -1);
	
	//dump all unfulfilled buy and sell orders
	clearTable("ask");
	clearTable("bid");
	
	// reset all credit limits of users
	clearTable("credit");
	clearTable("matchedtransactiondb");

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
		
	var cs = "call GET_FILTERED_BIDS(?)";
	db.executeSql(cs, [stock], function(value) {
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
	
	var cs = "call GET_FILTERED_ASKS(?)";
	db.executeSql(cs, [stock], function(value) {
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
	//var highestBid = new bidModule.Bid(undefined, 0, undefined, undefined);
	
	var highestBid = undefined;
	
	var cs = "call GET_HIGHEST_BID(?)";
	db.executeSql(cs, [stock], function(results) {
		bidJson = results[0];
		if (bidJson != undefined) {
			highestBid = new bidModule.Bid(parseInt(bidJson.id), bidJson.stockName, parseFloat(bidJson.price), bidJson.userID, new Date(bidJson.bidDate))
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
	
	//var lowestAsk = new askModule.Ask(undefined, Number.MAX_VALUE, undefined, undefined);
	var lowestAsk = undefined;
	
	var cs = "Call GET_LOWEST_ASK(?)";
	db.executeSql(cs, [stock], function(results) {
		askJson = results[0];
		if (askJson != undefined) {
			lowestAsk = new askModule.Ask(parseInt(askJson.id), askJson.stockName, parseFloat(askJson.price), askJson.userID, new Date(askJson.askDate));
		}
		callback(lowestAsk);
	});
}

ExchangeBean.prototype.getCreditRemaining = function(buyerUserId, callback) {

	var module = this;
	
	var cs = "call GET_USER_CREDIT_LIMIT(?)";
	db.executeSql(cs, [buyerUserId], function(value) {
		creditObject = value[0];
		if (creditObject != undefined && creditObject != "") {
			callback(creditObject.credit_limit);
		} else {
			console.log("Inserting " + buyerUserId + " and " + module.DAILY_CREDIT_LIMIT_FOR_BUYERS);
			var cs2 = "call INSERT_USER_CREDIT(?,?)";
			db.executeSql(cs2, [buyerUserId, module.DAILY_CREDIT_LIMIT_FOR_BUYERS], function(value) {});
			callback(module.DAILY_CREDIT_LIMIT_FOR_BUYERS);
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
			console.log("no enough credits");
			module.logRejectedBuyOrder(bid);
			callback(false);
		} else {
			// it's ok - adjust credit limit and return true
			console.log("Update credit limit for " + bid.getUserId() + " at the price of " + newRemainingCredit);
			var cs = "call UPDATE_CREDIT_LIMIT(?,?)";
			db.executeSql(cs, [newRemainingCredit, bid.getUserId()], function(value) {});
			callback(true);
		}
	});
}

// call this to append all rejected buy orders to log file
ExchangeBean.prototype.logRejectedBuyOrder = function(bid) {
	
	// Store in database
	var cs = "call INSERT_REJECTED_LOG(?)";
	db.executeSql(cs, [bid.toString()], function (value) {});
	
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
	var cs = "call INSERT_MATCHED_LOG(?)";
	db.executeSql(cs, [transaction.toString()], function(err) {});
}

// returns a string of HTML table rows code containing the list of user IDs and their remaining credits
// this method is used by viewOrders.jsp for debugging purposes
ExchangeBean.prototype.getAllCreditRemainingForDisplay = function(callback) {
	var returnString = "";
	
	var cs = "call GET_ALL_CREDIT()";
	db.executeSql(cs, [], function(results) {
		console.log(JSON.stringify(results));
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
	var TOTAL_COUNTER = 2;
	counter = 0;
	
	var highestBidCompare = undefined;
	var lowestAskCompare = undefined;
	var newBidStockName = newBid.getStock();
	
	this.validateCreditLimit(newBid, function(value) {
		
		if (!value) {
			callback(false);
			return;
		}
		
		insertBid(newBid, function(err) {
			
			if (!err) {
				console.log("An error has occurred in inserting bid");
				callback(false);
				return;
			} else {
				// step 1: Check if there's an ask
				module.getLowestAsk(newBidStockName, function(lowestAsk) {
					
					if (lowestAsk == undefined) {
						console.log("No transaction is matched");
						callback(true);
						return;
					} else {
						lowestAskCompare = lowestAsk;
						counter++;
						console.log(counter + ". " + lowestAskCompare.getUserId());
						if (counter == TOTAL_COUNTER) {
							// this is a BUYING trade - the transaction happens at the higest bid's timestamp, and the transaction price happens at the lowest ask		
							var match = processBidMatch(highestBidCompare, lowestAskCompare, highestBidCompare.getDate(), lowestAskCompare.getPrice());
							if (match) {
								module.logMatchedTransactions(match);
							}
							callback(true); // this bid is acknowledged
							return;
						}
					}
				});		
				
				// step 2: identify the current/highest bid in unfulfilledBids of the same stock
				module.getHighestBid(newBidStockName, function(highestBid) {

					highestBidCompare = highestBid;
					counter++;
					if (counter == TOTAL_COUNTER) {
						// this is a BUYING trade - the transaction happens at the higest bid's timestamp, and the transaction price happens at the lowest ask		
						var match = processBidMatch(highestBidCompare, lowestAskCompare, highestBidCompare.getDate(), lowestAskCompare.getPrice());
						if (match) {
							module.logMatchedTransactions(match);
						}
						callback(true); // this bid is acknowledged
						return;
					}
					
				});
			}
		});
	});
}

// call this method immediatley when a new ask (selling order) comes in
ExchangeBean.prototype.placeNewAskAndAttemptMatch = function(newAsk) {

	var module = this;
	var TOTAL_COUNTER = 2;
	counter = 0;
	
	var highestBidCompare = undefined;
	var lowestAskCompare = undefined;
	var newAskStockName = newAsk.getStock();
	
	insertAsk(newAsk, function(value) {
		console.log(value);
		if (!value) {
			console.log("An error has occurred in inserting ask");
			return;
		} else {
			
			module.getHighestBid(newAskStockName, function(highestBid) {

				if (highestBid == undefined) {
					console.log("No transaction is matched");
					return;
				} else {
				
					highestBidCompare = highestBid;
					counter++;
					if (counter == TOTAL_COUNTER) {
						// this is a BUYING trade - the transaction happens at the higest bid's timestamp, and the transaction price happens at the lowest ask		
						var match = processBidMatch(highestBidCompare, lowestAskCompare, lowestAskCompare.getDate(), highestBidCompare.getPrice());
						if (match != undefined) {
							module.logMatchedTransactions(match);
						}
						
						return;
					}
				}
			});
				
			module.getLowestAsk(newAskStockName, function(lowestAsk) {
				
				lowestAskCompare = lowestAsk;
				counter++;
				
				if (counter == TOTAL_COUNTER) {
					// this is a BUYING trade - the transaction happens at the higest bid's timestamp, and the transaction price happens at the lowest ask		
					var match = processBidMatch(highestBidCompare, lowestAskCompare, lowestAskCompare.getDate(), highestBidCompare.getPrice());
					if (match != undefined) {
						module.logMatchedTransactions(match);
					}
					
					return;
				}
			});		
		}
	});
}

function processBidMatch(highestBid, lowestAsk, time, price) {
				
	// A match happens if the lowest ask is <= highest bid
	if (lowestAsk.getPrice() <= highestBid.getPrice()) {
		// a match is found		
		deleteBid(highestBid, function(value) {});
		deleteAsk(lowestAsk, function(value){});
		
		// this is a SELLING trade - the transaction happens at the lowest ask's timestamp, and the transaction price happens at the highest bid
		var match = new matchedTransactionModule.MatchedTransaction(highestBid, lowestAsk, time, price);
		 
		var cs_insertTransaction = "call INSERT_MATCHED_TRANSACTION(?,?,?,?,?,?,?,?,?,?,?)";
		db.executeSql(cs_insertTransaction, [highestBid.getPrice(), highestBid.getUserId(), convertToTimeStamp(highestBid.getDate()), 
			lowestAsk.getPrice(), lowestAsk.getUserId(), convertToTimeStamp(lowestAsk.getDate()), convertToTimeStamp(match.getDate()), 
			match.getPrice(), match.getStock(), lowestAsk.getId(), highestBid.getId()], function(err) {
				console.log(JSON.stringify(err));
			});
		
		// to be included here: inform Back Office Server of match
		// to be done in v1.0
		updateLatestPrice(match.getStock(), match.getPrice());
		
		console.log("Transaction is matched");
		
		return match;
	}
				
}
/************************************************************************************************************************
Get SQL Procedure Statements
*************************************************************************************************************************/
// updates either latestPriceForSmu, latestPriceForNus or latestPriceForNtu
// based on the MatchedTransaction object passed in
ExchangeBean.prototype.getLatestPrice = function(stock, callback) {

	var cs = "call GET_STOCK_PRICE(?)";
	db.executeSql(cs, [stock], function(value) {
		if (value != undefined) {
			callback(value[0].price);
		} else {
			callback(-1);
		}
	});
}

// updates either latestPriceForSmu, latestPriceForNus or latestPriceForNtu
// based on the MatchedTransaction object passed in
function updateLatestPrice(stock, price) {
	var cs = "call UPDATE_STOCK_PRICE(?,?)";
	db.executeSql(cs, [price, stock], function(err) {
		if (err) {
			console.log("Unable to update " + stock + " with the price of " + price);
		} else {
			console.log("Stock updated");
		}
	});
}

function clearTable(tableName) {
	db.executeSql("delete from " + tableName, [], function(err) {
		if (err) {
			console.log("Delete all data from " + tableName + " failed. Refer to the below error: ");
			console.log(err);
		}
	});
	
}

function insertBid(bid, callback) {
	console.log(bid);
	var cs = "call INSERT_BID(?,?,?,?)";
	db.executeSql(cs, [bid.getStock(), bid.getPrice(), bid.getUserId(), convertToTimeStamp(bid.getDate())], function(err) {
		if (err) {
			console.log("Error found inserting bid");
			callback(false);
		} else {
			callback(true);
		}
	});
}

function insertAsk(ask, callback) {
	var cs = "call INSERT_ASK(?,?,?,?)";
	db.executeSql(cs, [ask.getStock(), ask.getPrice(), ask.getUserId(), convertToTimeStamp(ask.getDate())], function(err) {
		if (err) {
			console.log("Error found inserting ask");
			callback(false);
		} else {
			console.log("successful in inserting ask");
			callback(true);
		}
	});
}

function deleteBid(bid, callback) {
	var cs = "call DELETE_BID(?,?,?,?)";
	db.executeSql(cs, [bid.getStock(), bid.getPrice(), bid.getUserId(), convertToTimeStamp(bid.getDate())], function(err) {
		if (err) {
			console.log("Error found in deleting bid");
			callback(false);
		} else {
			callback(true);
		}
	});
}

function deleteAsk(ask, callback) {
	var cs = "call DELETE_ASK(?,?,?,?)";
	db.executeSql(cs, [ask.getStock(), ask.getPrice(), ask.getUserId(), convertToTimeStamp(ask.getDate())], function(err) {
		if (err) {
			console.log("Error found in deleting ask");
			callback(false);
		} else {
			callback(true);
		}
	});
}
/************************************************************************************************************************
Conversion variable to time stamp
*************************************************************************************************************************/
function convertToTimeStamp(date) {
	
	return date.getFullYear() + '-' +
    ('00' + (date.getMonth()+1)).slice(-2) + '-' +
    ('00' + date.getDate()).slice(-2) + ' ' + 
    ('00' + date.getHours()).slice(-2) + ':' + 
    ('00' + date.getMinutes()).slice(-2) + ':' + 
    ('00' + date.getSeconds()).slice(-2);

}

module.exports.ExchangeBean = ExchangeBean;