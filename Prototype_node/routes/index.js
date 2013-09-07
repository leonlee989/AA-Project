/*
 * GET home page.
 */
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
	
	var bidModule = require('./modules/Bid');
	var newBid = new bidModule.Bid(stock, tempBidPrice, userId);
	
	//var bidIsAccepted = exchangeBean.placeNewBidAndAttemptMatch(newBid);
	var bidIsAccepted = true;
	
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
	
	var askModules = require("./modules/Ask");
	var newAsk = new askModules.Ask(stock, tempAskPrice, userId);
	
	// exchangeBean.placeNewAskAndAttemptMatch(newAsk);
	
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
	/*
	res.render('sellSuccess.ejs', { 
		smuLatestPrice: exchangeBean.getLatestPrice("smu"),
		smuHighestBidPrice: exchangeBean.getHighestBidPrice("smu"),
		smuLowestAskPrice: exchangeBean.getLowestAskPrice("smu"),
		
		nusLatestPrice: exchangeBean.getLatestPrice("nus"),
		nusHighestBidPrice: exchangeBean.getHighestBidPrice("nus"),
		nusLowestAskPrice: exchangeBean.getLowestAskPrice("nus"),
		
		ntuLatestPrice: exchangeBean.getLatestPrice("ntu"),
		ntuHighestBidPrice: exchangeBean.getHighestBidPrice("ntu"),
		ntuLowestAskPrice: exchangeBean.getLowestAskPrice("ntu")
	}
	*/
	res.render('current.ejs', { 
		smuLatestPrice: 20,
		smuHighestBidPrice: 60,
		smuLowestAskPrice: 30,
		
		nusLatestPrice: 70,
		nusHighestBidPrice: 80,
		nusLowestAskPrice: 10,
		
		ntuLatestPrice: 30,
		ntuHighestBidPrice: 50,
		ntuLowestAskPrice: 30
	});
}

exports.viewOrders = function(req, res) {
	/*
	res.render('sellSuccess.ejs', { 
		smuUnfulfilledBidsForDisplay: exchangeBean.getUnfulfilledBidsForDisplay("smu"),
		smuUnfulfilledAsks: exchangeBean.getUnfulfilledAsks("smu"),
		
		nusUnfulfilledBidsForDisplay: exchangeBean.getUnfulfilledBidsForDisplay("nus"),
		nusUnfulfilledAsks: exchangeBean.getUnfulfilledAsks("nus"),
		
		ntuUnfulfilledBidsForDisplay: exchangeBean.getUnfulfilledBidsForDisplay("ntu"),
		ntuUnfulfilledAsks: exchangeBean.getUnfulfilledAsks("ntu")
		
		AllCreditRemainingForDisplay: exchangeBean.getAllCreditRemainingForDisplay()
	}
	*/
	res.render('viewOrders.ejs', { 
		smuUnfulfilledBidsForDisplay: 20,
		smuUnfulfilledAsks: 60,
		
		nusUnfulfilledBidsForDisplay: 70,
		nusUnfulfilledAsks: 80,
		
		ntuUnfulfilledBidsForDisplay: 30,
		ntuUnfulfilledAsks: 50,
		
		AllCreditRemainingForDisplay: 10200
	});
}

exports.endTradingDay = function(req, res) {
	//exchangeBean.endTradingDay(); // clean up instance variables
    //session.invalidate(); - clear session
	
	res.render('endTradingDay.ejs');
}