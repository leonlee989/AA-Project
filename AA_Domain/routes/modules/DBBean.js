var mysql = require("mysql");
var queues = require('mysql-queues');
const DEBUG = true;

var numberOfTries=0;
	
// Connection configuration
var clusterConfig = {
  removeNodeErrorCount: 3, // Remove the node immediately when connection fails.
  defaultSelector: 'ORDER'
};

var conn_conf = {
	host : '192.168.1.20',
	port : 7000,
	user : 'root',
	password : '',
	database : 'exchange'
}

var conn_conf_Master = {
	host : '192.168.1.8',
	port : 7000,
	user : 'root',
	password : 'obscure',
	database : 'exchange'
}

var conn_conf_Slave = {
	host : '192.168.1.2',
	port : 7000,
	user : 'root',
	password : '',
	database : 'exchange'
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
	var module = this;
	if (poolCluster == undefined) {
		this.estab_connection();
	}
	
	poolCluster.getConnection(function(err, connection) {
		if (connection != null) {
			//var options = {sql: stringSQL, nestTables: true};
			try {
				connection.query(stringSQL, query, function(err, results) {
					
					//console.log("Executing SQL statement...\n" + stringSQL);
					
					if (err) {
						//console.log("An error has occured as shown below:");
						//console.log(err);
						
						if (numberOfTries != 3) {
							numberOfTries++;
							module.executeSql(stringSQL, query, function(value) {
								callback(value);
							});
						} else {
							//console.log("Error in retrieval of information...");
							numberOfTries=0;
							callback(undefined);
						}
					} else {
						// Data Found
						//console.log("Successfully in calling stored procedure... " + JSON.stringify(results[0]));
						numberOfTries=0;
						callback(results[0]);
					}
					
				});
			} finally {
				if (connection != null) {
					connection.release();
				}
			}
		} else {
			console.log("Connection failed");
		}	
	});
	
	poolCluster.on('remove', function (nodeId) {
		console.log('REMOVED NODE : ' + nodeId); // nodeId = removed node 
	});
}

DBBean.prototype.executeSql_Transaction = function(stringSQL1, query1, stringSQL2, query2, stringSQL3, query3, callback) {
	var module = this;
	
	if (poolCluster == undefined) {
		this.estab_connection();
	}
	
	poolCluster.getConnection(function(err, connection) {
		if (connection != null) {
			try {
				queues(connection, DEBUG);
				
				var trans = connection.startTransaction();
				
				function error() {
					if (trans.rollback && numberOfTries != 3) {
						console.log("An error of transaction occurred, transaction rolled back");
								
						numberOfTries++;
						trans.rollback();
						module.executeSql_Transaction(stringSQL1, query1, stringSQL2, query2, stringSQL3, query3);
					} else {
						numberOfTries=0;
						callback(false);
					}
				}
					
				trans.query(stringSQL1, query1, function(err, results) {
						
					if (err) {
						console.log("GOT ERROR - " + err);
						error();
					}
					else
					{
						trans.query(stringSQL2, query2, function(err) {
							if (err) error();
						});
						trans.query(stringSQL3, query3, function(err) {
							if (err) error();
						});
						trans.commit();
					}
				}).execute();
				
				connection.query("FINALLY");
				
			} finally {
				console.log("--------------------------------------------------------->" + connection);
				if (connection != null) {
					connection.release();
				}
			}
		} else {
			console.log("Connection failed");
		}	
	});
	
	poolCluster.on('remove', function (nodeId) {
		console.log('REMOVED NODE : ' + nodeId); // nodeId = removed node 
	});
}

module.exports.DBBean = DBBean;