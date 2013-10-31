var mongo = require("mongodb"), 
Server = mongo.Server,
Db = mongo.Db;

function DBBean() {
	this.database = undefined;
}

DBBean.prototype.connect = function() {

	var server = new Server("192.168.0.10", 27017, {auto_reconnect: true});
	this.database = new Db("exchange", server);
	
	this.database.open(function(err, db) {
		if (!err) {
			console.log("Database is connected to MongoDB");
		} else {
			cnosole.log("Database cannot be connected to MongoDB");
		}
	});
}

DBBean.prototype.executeSql = function(tableName, callback) {
	var collection = this.database.collection(tableName);
	
	collection.find().toArray(function(err, items) {
		if (!err) {
			console.log("Retrieval of information from " + tableName + " success...");
			console.log("Query found as followed:" + JSON.stringify(items));
			callback(items);
		} else {
			console.log("Retrieval of information from " + tableName + " failed...");
		}
	});
}

DBBean.prototype.selectOneSql = function(tableName, query, callback) {
	var collection = this.database.collection(tableName);
	
	collection.findOne(query, function(err, item) {
		if (!err) {
			console.log("Retrieval of information from " + tableName + " by " + JSON.stringify(query) + " success...");
			console.log("Query found as followed:" + JSON.stringify(item));
			callback(item);
		} else {
			console.log("Retrieval of information from " + tableName + " by " + JSON.stringify(query) + " failed...");
		}
	})
}

DBBean.prototype.updateSql = function(tableName, query, setValue, callback) {
	var collection = this.database.collection(tableName);
	console.log("Executing updating commanding for " + tableName + " with " + JSON.stringify(query) + " setting " + JSON.stringify(setValue));
	collection.update(query, {$set:setValue}, {w:1}, function(err, results) {
		if (!err) {
			console.log("Update Success...");
			callback(results);
		} else {
			console.log("Update Failed...");
		}
	});
}

DBBean.prototype.insertSql = function(tableName, objJson, callback) {
	var collection = this.database.collection(tableName);
	
	console.log("Executing inserting commanding for " + tableName + " inserting values of: \n " + JSON.stringify(objJson));
	collection.insert(objJson, {w:1}, function(err, results) {
		if (!err) {
			console.log("Insert Success...");
			callback(results);
		} else {
			console.log("Insert Failed...");
		}
	});
}

DBBean.prototype.removeAllSql = function(tableName, callback) {
	var collection = this.database.collection(tableName); 
	
	console.log("Remove all rows from " + tableName);
	collection.remove();
}

DBBean.prototype.removeOneSql = function(tableName, remove_key, remove_value, callback) {
	var collection = this.database.collection(tableName);
	
	console.log("Remove " + remove_key + ":" + remove_value + " from " + tableName);
	collection.remove({remove_key:remove_value}, {w:1}, function(err, results) {
		if (!err) {
			console.log("Remove Success...");
			callback(results);
		} else {
			console.log("Remove Failed...");
		}
	});
}

DBBean.prototype.removeOneQuerySql = function(tableName, query, callback) {
	var collection = this.database.collection(tableName);
	
	console.log("Remove " + JSON.stringify(query) + " from " + tableName);
	collection.remove(query, {w:1}, function(err, results) {
		if (!err) {
			console.log("Remove Success...");
			callback(results);
		} else {
			console.log("Remove Failed...");
		}
	});
}

DBBean.prototype.countSql = function(tableName, query, callback) {
	
	var collection = this.database.collection(tableName);
	console.log("Count with " + JSON.stringify(query) + " from " + tableName);
	
	collection.count(query, function(err, countval) {
		if (!err) {
			console.log("Count Success...");
			callback(countval);
		} else {
			console.log("Count Failed...");
		}
    });
}

module.exports.DBBean = DBBean;