var mysql = require("mysql");

// Connection configuration
var clusterConfig = {
  removeNodeErrorCount: 1, // Remove the node immediately when connection fails.
  defaultSelector: 'ORDER',
};

var conn_conf = {
	host : 'localhost',
	port : 3306,
	user : 'root',
	password : 'root',
	database : 'exchangemel'
}

var conn_conf_Master = {
	host : '192.168.0.2',
	port : 7000,
	user : 'root',
	password : 'root',
	database : 'exchangemel'
}

var conn_conf_Slave = {
	host : '192.168.0.3',
	port : 7000,
	user : 'root',
	password : 'root',
	database : 'exchangemel'
}

var poolCluster = undefined;

function DBBean() {
}

DBBean.prototype.estab_connection = function() {
	if (poolCluster == undefined) {
		poolCluster = mysql.createPoolCluster(clusterConfig);
		
		poolCluster.add(conn_conf); //annonymous
		poolCluster.add("MASTER", conn_conf_Master); //Master
		poolCluster.add("SLAVE1", conn_conf_Slave); //Slave 1
	}
}

DBBean.prototype.executeSql = function(stringSQL, query, callback) {

	if (poolCluster == undefined) {
		this.estab_connection();
	}
	
	poolCluster.getConnection(function(err, connection) {
		if (connection != null) {
			//var options = {sql: stringSQL, nestTables: true};
			
			connection.query(stringSQL, query, function(err, results) {
				
				console.log("Executing SQL statement...\n" + stringSQL);
				
				if (err) {
					console.log("An error has occured as shown below:");
					console.log(err);
					//console.log("Error in retrieval of information...");
					callback(undefined);
				} else {
					// Data Found
					console.log("Successfully in calling stored procedure... " + JSON.stringify(results[0]));
					
					callback(results[0]);
				}
				
				connection.release();
			});
		} else {
			console.log("Connection failed");
		}	
	});
	
	poolCluster.on('remove', function (nodeId) {
		console.log('REMOVED NODE : ' + nodeId); // nodeId = removed node 
		results('REMOVED NODE : ' + nodeId);
	});
}
/*
DBBean.prototype.executeUpdate = function(strSQL) {
	if (poolCluster == undefined) {
		this.estab_connection();
	}
	
	poolCluster.getConnection(function(err, connection) {
		if (connection != null) {
			//var options = {sql: stringSQL, nestTables: true};
			
			connection.query(stringSQL, query, function(err, results) {
				console.log("Executing SQL statement...\n" + stringSQL);
				
				if (err) {
					console.log("An error has occured as shown below:");
					console.log(err);
					//console.log("Error in retrieval of information...");
					callback(undefined);
				} else {
					// Data Found
					console.log("Successfully in calling stored procedure... " + JSON.stringify(results[0]));
					
					callback(results[0]);
				}
				
				connection.release();
			});
		} else {
			console.log("Connection failed");
		}	
	});
	
	poolCluster.on('remove', function (nodeId) {
		console.log('REMOVED NODE : ' + nodeId); // nodeId = removed node 
		results('REMOVED NODE : ' + nodeId);
	});
}
*/
DBBean.prototype.close = function() {
	poolCluster.end();
}

module.exports.DBBean = DBBean;