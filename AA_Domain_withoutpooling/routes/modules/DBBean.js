var mysql = require("mysql");

// Connection configuration
var conn_conf = {
	host : 'localhost',
	port : 7000,
	user : 'root',
	password : '',
	database : 'exchange'
}

var connection = undefined;

function DBBean() {
}

DBBean.prototype.estab_connection = function() {
	connection = mysql.createConnection(conn_conf);

	connection.connect(function(err) {
	
		if (!err) {
			console.log("Connected to " + conn_conf.database + " on " + conn_conf.host);			
		} else {
			console.log("Connection Failed, unable to connection to DB");
		}
	});

}

DBBean.prototype.executeSql = function(stringSQL, results) {

	if (connection == undefined) {
		results(undefined);
	}
	
	connection.query(stringSQL, function(err, rows) {
		//console.log("Executing SQL statement...\n" + stringSQL);
		
		if (err) {
			console.log(err);
			console.log("Error in retrieval of information...");
			results(undefined);
		} else {
			// Data Found
			//console.log("Success in retrieval of information...");
			//for (var i=0; i<rows.length; i++) {
				//console.log(rows[i]);
			//}
			
			results(rows);
		}
	});
}

DBBean.prototype.executeUpdate = function(strSQL) {
	if (connection == undefined) {
		throw new Error("No connection to database");
	}
	
	connection.query(strSQL, function(err, results) {
		//console.log("Executing SQL statement...\n" + strSQL);
		
		if (err) {
			console.log(err);
			console.log("Error in executing sql...");
			//throw new Error("Error in executing sql...");
		} else {
			//console.log("Success in executing sql...");
			//callback(results);
		}
	});
}

DBBean.prototype.close = function() {
	connection.end();
}

module.exports.DBBean = DBBean;