var mysql = require("mysql");

// Connection configuration
var conn_conf = {
	host : 'localhost',
	port : 7000,
	user : 'root',
	password : '',
	database : 'exchange'
}

var pool = undefined;

function DBBean() {
}

DBBean.prototype.estab_connection = function(callback) {
	if (pool == undefined) {
		pool = mysql.createPool(conn_conf);
	}
	
	pool.on('connection', function(connection) {
		connection.query('SET SESSION auto_increment_increment=1')
	});
}

DBBean.prototype.executeSql = function(stringSQL, results) {

	if (pool == undefined) {
		pool = mysql.createPool(conn_conf);
	}
	
	pool.getConnection(function(err, connection) {
		if (connection != null) {
			connection.query(stringSQL, function(err, rows) {
				console.log("Executing SQL statement...\n" + stringSQL);
				
				if (err) {
					console.log(err);
					console.log("Error in retrieval of information...");
					results(undefined);
				} else {
					// Data Found
					console.log("Success in retrieval of information...");
					for (var i=0; i<rows.length; i++) {
						console.log(rows[i]);
					}
					
					results(rows);
				}
				
				connection.release();
			});
		} else {
			console.log("Connection failed");
		}	
	});
}

DBBean.prototype.executeUpdate = function(strSQL) {
	if (pool == undefined) {
		pool = mysql.createPool(conn_conf);
	}
	
	pool.getConnection(function(err, connection) {
		if (connection != null) {
			connection.query(strSQL, function(err, results) {
				console.log("Executing SQL statement...\n" + strSQL);
				
				if (err) {
					console.log(err);
					console.log("Error in executing sql...");
				} else {
					console.log("Success in executing sql...");
					//callback(results);
				}
				
				connection.release();
			});
		} else {
			console.log("Connection failed");
		}	
	});
}

DBBean.prototype.close = function() {
	connection.end();
}

module.exports.DBBean = DBBean;