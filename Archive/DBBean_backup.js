var nosql = require("mysql-js");
var userModule = require("./User");

function DBBean() {}

// Connection to database for mysql cluster
DBBean.prototype.connect() {
	var dbProperties = {
		"implementation" : "ndb",
		"database" : "exchange"
	}
	
	console.log("Opening Session");
	nosql.openSession(dbProperties, null, onSession);
	
	var annotations = new nosql.Annotation();
	annotations.mapClass(userModule.User, {'table' : 'credit'});

}

// Check session with data is inserted
function onSession = function(err, session, data) {
	console.log("On Session...");
	
	if (err) {
		console.log(err);
		process.exit(0);
	} else {
		session.find(userModule.User, 'Dex', onFind);
	}
}

// When data is being found in database
function onFind = function(err, data) {
	console.log("On Found...");
	
	if (err) {
		console.log(err);
		process.exit(0);
	} else {
		console.log("Found: ", JSON.stringify(data));
		
		// Data usage is found.
	}
}