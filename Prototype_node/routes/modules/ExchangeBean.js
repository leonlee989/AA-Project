// import
var bidModule = require("./Bid");
var askModule = require("./Ask");
var matchedTransactionModule = require("./MatchedTransaction");
var fs = require('fs');

function ExchangeBean() {
	// location of log files - change if necessary
	this.MATCH_LOG_FILE = "c:\\temp\\matched.log";
	this.REJECTED_BUY_ORDERS_LOG_FILE = "c:\\temp\\rejected.log";

	// used to calculate remaining credit available for buyers
	this.DAILY_CREDIT_LIMIT_FOR_BUYERS = 1000000;

	// used for keeping track of fulfiling asks and bids in the system
	// once asks or bid are matched, they must be removed from these arrayllists.
	this.unfulfilledAsks = new Array();
	this.unfulfilledBids = new Array();

	// used to keep track of all matched transactions (asks/bids) in the system
	// matchedTransactions is cleaned once the records are written to the log file successfully
	this.matchedTransactions = new Array();

	// keeps track of the lastest price for each of the 3 stocks
	this.latestPriceForSmu = -1;
	this.latestPriceForNus = -1;
	this.latestPriceForNtu = -1;

	// keeps track of the remaining credit limits of each buyers. This should be
	// checked every time a buy order is submitted. Buy orders that breach the 
	// credit limit should be rejected and logged
	// The key for this Hashtable is the user ID of the buyer, and the corresponding value is the Remaining credit limit
	// the remaining credit limit should not go below 0 under any circumstance!
	 this.creditRemaining = new Array();
 }
 
 // this method is called once at the end trading day. It can be called manually, or by a timed daemon
 // this is a good chance to "clean up" everything to get ready for the next trading
 ExchangeBean.prototype.endTradingDay = function() {
	// reset attributes
	this.latestPriceForSmu = -1;
	this.latestPriceForNus = -1;
	this.latestPriceForNtu = -1;
	
	//dump all unfulfilled buy and sell orders
	this.unfulfilledAsks = [];
	this.unfulfilledBids = [];
	
	// reset all credit limits of users
	this.creditRemaining = [];
 }
 
 // returns a String of unfulfilled bids for a particular stocks
 // returns an empty string if no such bid
 // bods are separated by <br> for display on HTML page
ExchangeBean.prototype.getUnfulfilledBidsForDisplay = function(stock) {
	var returnString = "";
	for ( var i = 0; i < this.unfulfilledBids.length; i++) {
		var bid = this.unfulfilledBids[i];
		
		if (bid.getStock() == stock) {
			returnString = returnString + bid.toString() + "<br/>"; 
		}
	}
	
	return returnString;
 }
 
 // return a String of unfilled asks for a particular stock
 // returns an empty string if no such ask
 // asks are separated by <br> for display on HTML page
 ExchangeBean.prototype.getUnfulfilledAsks = function(stock) {
	var returnString = "";
	for (var i = 0; i < this.unfulfilledAsks.length; i++) {
		var ask = this.unfulfilledAsks[i];
		if (ask.getStock() == stock) {
			returnString = returnString + ask.toString() + "<br/>";
		}		
	}
	
	return returnString;
 }
 
// returns the highest bid for a particular stock
// return -1 if there is no bid at all
ExchangeBean.prototype.getHighestBidPrice = function(stock) {
	var highestBid = this.getHighestBid(stock);
	if (highestBid === undefined) {
		return -1;
	} else {
		return highestBid.getPrice();
	}
}
 
// retrieve unfulfiled current (highest) bid for a particular stock
// return null if there is no unfulfiled bid for this stock
ExchangeBean.prototype.getHighestBid = function(stock) {
	var highestBid = new bidModule.Bid(undefined, 0, undefined);
	for(var i = 0; i < this.unfulfilledBids.length; i++) {
		var bid = this.unfulfilledBids[i];
		if (bid.getStock() == stock && bid.getPrice() >= highestBid.getPrice()) {
			// if there are 2 bids of the same amount, the earlier one is considered the highest bid
			if (bid.getPrice() == highestBid.getPrice()) {
				if (bid.getDate().getTime() < highestBid.getDate().getTime()) {
					highestBid = bid;
				}
			} else {
				highestBid = bid;
			}
		} 
	}
	if (highestBid.getUserId() === undefined) {
		return undefined;
	}
	return highestBid;
}
 
// returns the lowest ask for a particular stock
// returns -1 if there is no ask at all
ExchangeBean.prototype.getLowestAskPrice = function(stock) {
	var lowestAsk = this.getLowestAsk(stock);
	if (lowestAsk === undefined) {
		return -1;
	} else {
		return lowestAsk.getPrice();
	}
}
  
// retrieve unfulfiled current (lowest) ask for a particular stock
// returns null if there is no unfulfiled asks for this stock
ExchangeBean.prototype.getLowestAsk = function(stock) {
	var lowestAsk = new askModule.Ask(undefined, Number.MAX_VALUE, undefined);
	for (var i = 0; i < this.unfulfilledAsks.length; i++) {
		var ask = this.unfulfilledAsks[i];
		if (ask.getStock() == stock && ask.getPrice() <= lowestAsk.getPrice()) {
			// there are 2 asks of the same ask amount, the earlier one is considered the highest ask
			if (ask.getPrice() == lowestAsk.getPrice()) {
				//compares dates
				if (ask.getDate().getTime() < lowestAsk.getDate().getTime()) {
					lowestAsk = ask;
				}
			}  else {
				lowestAsk = ask;
			}
		}
	}
	if (lowestAsk.getUserId() === undefined) {
		return undefined;
	}
	return lowestAsk;
}

ExchangeBean.prototype.getCreditRemaining = function(buyerUserId) {
	
	if (this.creditRemaining[buyerUserId] === undefined) {
		// this buyer is not in the hash table yet. Hence create a new entry for him
		this.creditRemaining[buyerUserId] = this.DAILY_CREDIT_LIMIT_FOR_BUYERS;
	}
	return this.creditRemaining[buyerUserId];
}

// check if a buyer is eligible to place an order based on his credit limit
// if he is eligible, this method adjusts his credit limit and returns true
// if he is not eligible, this method logs the bid and returns false
ExchangeBean.prototype.validateCreditLimit = function(bid) {
	var totalPriceOfBid = bid.getPrice() * 1000; //each bid is for 1000 shares
	var remainingCredit = this.getCreditRemaining(bid.getUserId());
	var newRemainingCredit = remainingCredit - totalPriceOfBid;

	if (newRemainingCredit < 0) {
		// no go - log failed bid and return false
		this.logRejectedBuyOrder(bid);
		return false;
	} else {
		// it's ok - adjust credit limit and return true
		this.creditRemaining[bid.getUserId()] = newRemainingCredit;
		return true;
	}
}

// call this to append all rejected buy orders to log file
ExchangeBean.prototype.logRejectedBuyOrder = function(bid) {
	fs.appendFile(this.REJECTED_BUY_ORDERS_LOG_FILE, bid.toString() + "\n", function(err) {
		if(err) {
			console.log(err);
		} else {
			console.log("The file was saved!");
		}
	});
}

// call this to append all matched transactions in matchedTransactions to log file and clear matchedTransactions
ExchangeBean.prototype.logMatchedTransactions = function() {
	var log_File = this.MATCH_LOG_FILE;
	this.matchedTransactions.forEach(function(transaction) {
		
		fs.appendFile(log_File, transaction.toString() + "\n", function(err) {
			if(err) {
				console.log(err);
			} else {
				console.log("The file was saved!");
			}
		});
	});
	this.matchedTransactions = [];
}

// returns a string of HTML table rows code containing the list of user IDs and their remaining credits
// this method is used by viewOrders.jsp for debugging purposes
ExchangeBean.prototype.getAllCreditRemainingForDisplay = function() {
	var returnString = "";

	for (var key in this.creditRemaining) {
		var value = this.creditRemaining[key];
		returnString = returnString + "<tr><td>" + key + "</td><td>" + value + "</td></tr>";
	}
	return returnString;
}

// call this method immediatley when a new bid (buying order) comes in
// this method returns false if this buy order has been rejected because of a credit limit breach
// it returns true if the bid has been successfully added
ExchangeBean.prototype.placeNewBidAndAttemptMatch = function(newBid) {
	var okToContinue = this.validateCreditLimit(newBid);
	if (!okToContinue) {
		return false;
	}
	
	// step 1: insert new bid into unfulfilledBids
	this.unfulfilledBids.push(newBid);
	
	// step 2: check if there is any unfulfilled asks (sell orders) for the new bid's stock. if not, just return
    // count keeps track of the number of unfulfilled asks for this stock
	var count = 0;
	for (var i = 0; i < this.unfulfilledAsks.length; i++) {
		if (this.unfulfilledAsks[i].getStock() == newBid.getStock()) {
			count++;
		}
	}
	if (count == 0) {
		return true;
	}
	
	// step 3: identify the current/highest bid in unfulfilledBids of the same stock
	var highestBid = this.getHighestBid(newBid.getStock());
	
	// step 4: identify the current/lowest ask in unfulfilledAsks of the same stock
	var lowestAsk = this.getLowestAsk(newBid.getStock());
	
	// step 5: check if there is a match.
    // A match happens if the highest bid is bigger or equal to the lowest ask
	if (highestBid.getPrice() >= lowestAsk.getPrice()) {
		// a match is found
		this.unfulfilledBids.splice(highestBid,1);
		this.unfulfilledAsks.splice(lowestAsk,1);
		// this is a BUYING trade - the transaction happens at the higest bid's timestamp, and the transaction price happens at the lowest ask
		var match = new matchedTransactionModule.MatchedTransaction(highestBid, lowestAsk, highestBid.getDate(), lowestAsk.getPrice());
		this.matchedTransactions.push(match);
		
		// to be included here: inform Back Office Server of match
		// to be done in v1.0
		this.updateLatestPrice(match);
		this.logMatchedTransactions();
	}
	return true; // this bid is acknowledged
}

// call this method immediatley when a new ask (selling order) comes in
ExchangeBean.prototype.placeNewAskAndAttemptMatch = function(newAsk) {
	// step 1: insert new ask into unfulfilledAsks
	this.unfulfilledAsks.push(newAsk);
	
	// step 2: check if there is any unfulfilled bids (buy orders) for the new ask's stock. if not, just return
    // count keeps track of the number of unfulfilled bids for this stock
	var count = 0;
	for (var i = 0; i < this.unfulfilledBids.length; i++) {
		if (this.unfulfilledBids[i].getStock() == newAsk.getStock()) {
			count++;
		}
	}
	if (count == 0) {
		return; //true; // no unfulfilled asks of the same stock
	}
	
	 // step 3: identify the current/highest bid in unfulfilledBids of the same stock
	 var highestBid = this.getHighestBid(newAsk.getStock());
	 
	 // step 4: identify the current/lowest ask in unfulfilledAsks of the same stock
	 var lowestAsk = this.getLowestAsk(newAsk.getStock());
	 
	 // step 5: check if there is a match.
    // A match happens if the lowest ask is <= highest bid
	if (lowestAsk.getPrice() <= highestBid.getPrice()) {
		// a match is found
		this.unfulfilledBids.splice(highestBid, 1);
		this.unfulfilledAsks.splice(lowestAsk, 1);
		// this is a SELLING trade - the transaction happens at the lowest ask's timestamp, and the transaction price happens at the highest bid
		var match = new matchedTransactionModule.MatchedTransaction(highestBid, lowestAsk, lowestAsk.getDate(), highestBid.getPrice());
		this.matchedTransactions.push(match);
		
		// to be included here: inform Back Office Server of match
		// to be done in v1.0
		this.updateLatestPrice(match);
		this.logMatchedTransactions();
	}
	
	//return true;
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

module.exports.ExchangeBean = ExchangeBean;