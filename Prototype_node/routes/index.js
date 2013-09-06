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
	var authehticatedUser = req.session.authenticatedUser;
	
	//req.session.stock = req.param('stock');
	//req.session.tempBidPrice = req.param('bidprice');
	var stock = req.param('stock');
	var tempBidPrice = req.param('bidprice');
	
	var module = require('./modules/Bid');
	var newBid = new module.Bid(stock, tempBidPrice, userId);
	
	//var bidIsAccepted = exchangeBean.placeNewBidAndAttemptMatch(newBid);
	var bidIsAccepted = true;
	
	if (!bidIsAccepted) {
		res.render('buySuccess.ejs', { 
			userId: req.session.userId, 
			authenticatedUser: req.session.authenticatedUser,
			stock: stock,
			bidPrice: tempBidPrice
		});
	} else {
		res.render('buyFail.ejs', { 
			userId: req.session.userId, 
			authenticatedUser: req.session.authenticatedUser,
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

exports.logout = function(req,res) {
	delete req.session.userId;
	delete req.session.authenticatedUser;
	
	res.render('logout.ejs');
};