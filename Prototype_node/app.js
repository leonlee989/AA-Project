
/**
 * Module dependencies.
 */

var express = require('express');
var http = require('http');
var path = require('path');
var routes = require('./routes');

var app = express();

// all environments
app.set('port', process.env.PORT || 3001);

// config
app.set('views', __dirname + '/views');
app.set('view engine', 'ejs');
app.engine('.html', require('ejs').renderFile);

app.use(express.favicon());
app.use(express.logger('dev'));
app.use(express.bodyParser());
app.use(express.methodOverride());

// Use session variables
app.use(express.cookieParser());
app.use(express.session({secret: ' '}));

app.use(app.router);
app.use(require('stylus').middleware(__dirname + '/public'));
app.use(express.static(path.join(__dirname, 'public/')));
 
// development only
if ('development' == app.get('env')) {
  app.use(express.errorHandler());
}

// get site
app.get('/', routes.index);
	
app.get('/static1', routes.static1);
app.get('/static2', routes.static2);
app.get('/static3', routes.static3);
app.get('/static4', routes.static4);
app.get('/static5', routes.static5);
app.get('/static6', routes.static6);

app.get('/login', routes.login);
app.get('/logout', routes.logout);

app.get('/buy', routes.authenUser, routes.buy);
app.get('/buySuccess', routes.authenUser, routes.buySuccess);
app.get('/buyFail', routes.authenUser, routes.buyFail);

app.get('/sell', routes.authenUser, routes.sell);
app.get('/sellSuccess', routes.authenUser, routes.sellSuccess);

app.get('/current', routes.current);
app.get('/viewOrders', routes.viewOrders);
app.get('/endTradingDay', routes.endTradingDay);

// Operations 
app.post('/processlogin', routes.processlogin);
app.post('/processBuy', routes.authenUser, routes.processBuy);
app.post('/processSell', routes.authenUser, routes.processSell);

// Run server
http.createServer(app).listen(app.get('port'), function(){
  console.log('Express server listening on port ' + app.get('port'));
});