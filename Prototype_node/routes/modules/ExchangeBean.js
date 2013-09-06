// import
var bidModule = require("./Bid");

// location of log files - change if necessary
var MATCH_LOG_FILE = "c:\\temp\\matched.log";
var REJECTED_BUY_ORDERS_LOG_FILE = "c:\\temp\\rejected.log";

// used to calculate remaining credit available for buyers
var DAILY_CREDIT_LIMIT_FOR_BUYERS = 1000000;

// used for keeping track of fulfiling asks and bids in the system
// once asks or bid are matched, they must be removed from these arrayllists.
var unfilfilledAsks = new Array();
var unfulfilledBids = new Array();

// used to keep track of all matched transactions (asks/bids) in the system
// matchedTransactions is cleaned once the records are written to the log file successfully
var matachedTransactions = new Array();

// keeps track of the lastest price for each of the 3 stocks
var latestPriceForSmu = -1;
var latestPriceForNus = -1;
var latestPriceForNtu = -1;

// keeps track of the remaining credit limits of each buyers. This should be
// checked every time a buy order is submitted. Buy orders that breach the 
// credit limit should be rejected and logged
// The key for this Hashtable is the user ID of the buyer, and the corresponding value is the Remaining credit limit
// the remaining credit limit should not go below 0 under any circumstance!
 var creditRemaining = new Array();
 
 // this method is called once at the end trading day. It can be called manually, or by a timed daemon
 // this is a good chance to "clean up" everything to get ready for the next trading
 function endTradingDay() {
	// reset attributes
	latestPriceForSmu = -1;
	latestPriceForNus = -1;
	latestPriceForNtu = -1;
	
	//dump all unfulfilled buy and sell orders
	unfilfilledAsks = [];
	unfulfilledBids = [];
	
	// reset all credit limits of users
	creditRemaining = [];
 }
 
 // returns a String of unfulfilled bids for a particular stocks
 // returns an empty string if no such bid
 // bods are separated by <br> for display on HTML page
 function getUnfulfilledBidsForDisplay(stock) {
	var returnString = "";
	for ( var i = 0; i < unfulfilledBids.length; i++) {
		var bid = unfulfilledBids[i];
		
		if (bid.getStock() == stock) {
			returnString = returnString + "<br/>"; 
		}
	}
	
	return returnString;
 }
 
 // return a String of unfulliled 