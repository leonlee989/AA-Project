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
	
	var stock = req.session.stock = req.param('stock');
	var tempBidPrice = req.session.tempBidPrice = req.param('bidprice');
	
	var newBid = new bidModule.Bid(stock, tempBidPrice, userId, new Date());
	exchangeBean.placeNewBidAndAttemptMatch(newBid, function(bidIsAccepted) {
		if (bidIsAccepted) {
			res.render('buySuccess.ejs', { 
				userId: req.session.userId, 
				stock: req.session.stock,
				bidPrice: req.session.tempBidPrice
			});
		} else {
			res.render('buyFail.ejs', { 
				userId: req.session.userId, 
				stock: req.session.stock,
				bidPrice: req.session.tempBidPrice
			});
		}
	});
}

exports.sell = function(req, res) {
	res.render('sell.ejs',  { 
		userId: req.session.userId, 
		authenticatedUser: req.session.authenticatedUser 
	});
};

exports.processSell = function(req, res) {
	var userId = req.session.userId;
	
	var stock = req.session.stock = req.param('stock');
	var tempAskPrice = req.session.tempAskPrice = req.param('askprice');
	
	var newAsk = new askModule.Ask(stock, tempAskPrice, userId, new Date());
	exchangeBean.placeNewAskAndAttemptMatch(newAsk);
	
	res.render('sellSuccess.ejs', { 
		userId: req.session.userId, 
		stock: req.session.stock,
		askPrice: req.session.tempAskPrice
	});
}

exports.logout = function(req,res) {
	delete req.session.userId;
	delete req.session.authenticatedUser;
	
	res.render('logout.ejs');
};

exports.current = function(req, res) {
	exchangeBean.getHighestBidPrice("smu", function(smuhighestprice) {
		exchangeBean.getHighestBidPrice("nus", function(nushigestprice) {
			exchangeBean.getHighestBidPrice("ntu", function(ntuhighestprice) {
				
				exchangeBean.getLowestAskPrice("smu", function(smulowestprice) {
					exchangeBean.getLowestAskPrice("nus", function(nuslowestprice) {
						exchangeBean.getLowestAskPrice("ntu", function(ntulowestprice) {
						
							res.render('current.ejs', { 
								smuLatestPrice: exchangeBean.getLatestPrice("smu"),
								smuHighestBidPrice: smuhighestprice,
								smuLowestAskPrice: smulowestprice,
								
								nusLatestPrice: exchangeBean.getLatestPrice("nus"),
								nusHighestBidPrice: nushigestprice,
								nusLowestAskPrice: nuslowestprice,
								
								ntuLatestPrice: exchangeBean.getLatestPrice("ntu"),
								ntuHighestBidPrice: ntuhighestprice,
								ntuLowestAskPrice: ntulowestprice
							});
						});
					});
				});
			});
		});
	});
}

exports.viewOrders = function(req, res) {
	exchangeBean.getAllCreditRemainingForDisplay(function(value) {
		exchangeBean.getUnfulfilledBidsForDisplay("smu", function(smubids) {
			exchangeBean.getUnfulfilledBidsForDisplay("nus", function(nusbids) {
				exchangeBean.getUnfulfilledBidsForDisplay("ntu", function(ntubids) {
					
					exchangeBean.getUnfulfilledAsks("smu", function(smuask) {
						exchangeBean.getUnfulfilledAsks("smu", function(nusask) {
							exchangeBean.getUnfulfilledAsks("smu", function(ntuask) {
				
								res.render('viewOrders.ejs', { 

									smuUnfulfilledBidsForDisplay: smubids,
									smuUnfulfilledAsks: smuask,
									
									nusUnfulfilledBidsForDisplay: nusbids,
									nusUnfulfilledAsks: nusask,
									
									ntuUnfulfilledBidsForDisplay: ntubids,
									ntuUnfulfilledAsks: ntuask,
									
									AllCreditRemainingForDisplay: value
								});
							});
						});
					});
				});
			});
		});
	});
}

exports.endTradingDay = function(req, res) {
	exchangeBean.endTradingDay(); // clean up instance variables
    //res.clear();
	exchangeBean.sendToBackOffice("");
	
	res.render('endTradingDay.ejs');
}