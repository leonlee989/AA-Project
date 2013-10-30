var mysql = require("mysql");

// Connection configuration
var conn_conf = {
	host : 'localhost',
	port : 3306,
	user : 'root',
	password : 'root',
	database : 'exchange'
}

var connection = undefined;

function DBBean() {
}

DBBean.prototype.estab_connection =  function() {
	connection = mysql.createConnection(conn_conf);
	
	connection.connect(function(err) {
	
		if (err) {
			console.log("Connection Failed, unable to connection to DB");
			return false;
		} else {
			console.log("Connected to " + conn_conf.database + " on " + conn_conf.host);
			return true;
		}
	});
}

DBBean.prototype.executeSql = function(stringSQL, results) {

	if (connection == undefined) {
		return undefined;
	}
	
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
	});
}

DBBean.prototype.executeUpdate = function(strSQL) {
	if (connection == undefined) {
		return undefined;
	}
	
	connection.query(strSQL, function(err, results) {
		console.log("Executing SQL statement...\n" + strSQL);
		
		if (err) {
			console.log(err);
			console.log("Error in executing sql...");
			return undefined;
		} else {
			console.log("Success in executing sql...");
			return results;
		}
	});
}

DBBean.prototype.close = function() {
	connection.end();
}

function updateStatement(tableName, fieldName, conditionFields, condition, valueList) {
	if (!connection) {
		return undefined;
	}
	
	var stringFields = "";
	
	for (var i=0; i<fieldName.length; i++) {
		if (i = 0) {
			stringFields += fieldName[i] + " = ? ";
		} else {
			stringFields += ", " + fieldName[i] + " = ? "
		}
	}
	
	connection.query("UPDATE " + tableName + " SET " + stringFields + " WHERE " + conditionFields + " = ?", valueList, function(err, results) {
		console.log("Executing SQL statement...\n" + "UPDATE " + tableName + " SET " + stringFields + " WHERE " + conditionFields + " = ?\n");
		
		if (err) {
			console.log("Error in updating information...");
			return undefined;
		} else {
			console.log("Successful in updating information...");
			return results;
		}
	});
}

module.exports.DBBean = DBBean;