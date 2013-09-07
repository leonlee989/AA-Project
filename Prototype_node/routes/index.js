/*
 * GET home page.
 */
var bidModule = require('./modules/Bid');
var askModule = require("./modules/Ask");
var matchedTransactionModule = require('./modules/MatchedTransaction');

var exchangeBeanModule = require('./modules/ExchangeBean');
var exchangeBean = new exchangeBeanModule.ExchangeBean();

exports.index = function(req, res){
	res.render('index.ejs');
};

/*
 * GET static pages.
 */
exports.static1 = function(req, res){
	res.render('staticpages/static1.ejs');
};

exports.static2 = function(req, res){
	res.render('staticpages/static2.ejs');
};

exports.static3 = function(req, res){
	res.render('staticpages/static3.ejs');
};

exports.static4 = function(req, res){
	res.render('staticpages/static4.ejs');
};

exports.static5 = function(req, res){
	res.render('staticpages/static5.ejs');
};

exports.static6 = function(req, res){
	res.render('staticpages/static6.ejs');
};

/*
 * GET dynamic pages.
 */
exports.login = function(req, res){
	res.render('login.ejs');
};

exports.authenUser = function(req, res, next) {
	if (req.session.authenticatedUser==null){ 
		res.redirect('/login');
	} else {
		next();
	}
}

exports.processlogin = function(req, res) {
	req.session.userId = req.param('id');
	req.session.authenticatedUser = true;
	// Write codes for Operation for checking credential in database below
	
	// Render login success file
	res.render('loginsuccess.ejs',  { 
		userId: req.session.userId, 
		authenticatedUser: req.session.authenticatedUser 
	});
};

exports.buy = function(req, res) {
	res.render('buy.ejs',  { 
		userId: req.session.userId, 
		authenticatedUser: req.session.authenticatedUser 
	});
};

exports.processBuy = function(req, res) {
	var userId = req.session.userId;
	var authenticatedUser = req.session.authenticatedUser;
	
	//req.session.stock = req.param('stock');
	//req.session.tempBidPrice = req.param('bidprice');
	var stock = req.param('stock');
	var tempBidPrice = req.param('bidprice');
	
	var newBid = new bidModule.Bid(stock, tempBidPrice, userId);
	var bidIsAccepted = exchangeBean.placeNewBidAndAttemptMatch(newBid);
	
	if (!bidIsAccepted) {
		res.render('buySuccess.ejs', { 
			userId: userId, 
			authenticatedUser: authenticatedUser,
			stock: stock,
			bidPrice: tempBidPrice
		});
	} else {
		res.render('buyFail.ejs', { 
			userId: userId, 
			authenticatedUser: authenticatedUser,
			stock: stock,
			bidPrice: tempBidPrice
		});
	}
}

exports.sell = function(req, res) {
	res.render('sell.ejs',  { 
		userId: req.session.userId, 
		authenticatedUser: req.session.authenticatedUser 
	});
};

exports.processSell = function(req, res) {
	var userId = req.session.userId;
	var authenticatedUser = req.session.authenticatedUser;
	
	//req.session.stock = req.param('stock');
	//req.session.tempAskPrice = req.param('askprice');
	
	var stock = req.param('stock');
	var tempAskPrice = req.param('askprice');
	
	var newAsk = new askModule.Ask(stock, tempAskPrice, userId);
	
	exchangeBean.placeNewAskAndAttemptMatch(newAsk);
	
	res.render('sellSuccess.ejs', { 
		userId: userId, 
		authenticatedUser: authenticatedUser,
		stock: stock,
		askPrice: tempAskPrice
	});
	
}

exports.logout = function(req,res) {
	delete req.session.userId;
	delete req.session.authenticatedUser;
	
	res.render('logout.ejs');
};

exports.current = function(req, res) {
	res.render('current.ejs', { 
		smuLatestPrice: exchangeBean.getLatestPrice("smu"),
		smuHighestBidPrice: exchangeBean.getHighestBidPrice("smu"),
		smuLowestAskPrice: exchangeBean.getLowestAskPrice("smu"),
		
		nusLatestPrice: exchangeBean.getLatestPrice("nus"),
		nusHighestBidPrice: exchangeBean.getHighestBidPrice("nus"),
		nusLowestAskPrice: exchangeBean.getLowestAskPrice("nus"),
		
		ntuLatestPrice: exchangeBean.getLatestPrice("ntu"),
		ntuHighestBidPrice: exchangeBean.getHighestBidPrice("ntu"),
		ntuLowestAskPrice: exchangeBean.getLowestAskPrice("ntu")
	});
}

exports.viewOrders = function(req, res) {
	res.render('viewOrders.ejs', { 
		smuUnfulfilledBidsForDisplay: exchangeBean.getUnfulfilledBidsForDisplay("smu"),
		smuUnfulfilledAsks: exchangeBean.getUnfulfilledAsks("smu"),
		
		nusUnfulfilledBidsForDisplay: exchangeBean.getUnfulfilledBidsForDisplay("nus"),
		nusUnfulfilledAsks: exchangeBean.getUnfulfilledAsks("nus"),
		
		ntuUnfulfilledBidsForDisplay: exchangeBean.getUnfulfilledBidsForDisplay("ntu"),
		ntuUnfulfilledAsks: exchangeBean.getUnfulfilledAsks("ntu"),
		
		AllCreditRemainingForDisplay: exchangeBean.getAllCreditRemainingForDisplay()
	});
}

exports.endTradingDay = function(req, res) {
	exchangeBean.endTradingDay(); // clean up instance variables
    res.clear();
	
	res.render('endTradingDay.ejs');
}