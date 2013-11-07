var mongo = require("mongodb"), 
Server = mongo.Server,
Db = mongo.Db;

function DBBean() {
	this.database = undefined;
}

var option = [
	"192.168.1.10", 
	"192.168.1.1",
	"192.168.1.20"
];

var count = 0;

DBBean.prototype.connect = function() {
	
	var module = this;
	var ipaddress = option[count];
	
	var server = new Server(ipaddress, 27017, {auto_reconnect: true, poolSize:115});
	this.database = new Db("exchange", server);
	//this.database.db("exchange");
	
	this.database.open(function(err, db) {
		if (!err) {
			console.log("Database is connected to MongoDB");
		} else {
			console.log("Database cannot be connected to MongoDB");
			
			if (count < 3) {
				count++;
				module.connect();
			} else {
				count = 0;
				throw new Error("Connection cannot be found");
			}
		}
	});
}

DBBean.prototype.executeSql = function(tableName, callback) {
	var module = this;
	var collection = this.database.collection(tableName);
	
	collection.find().toArray(function(err, items) {
		if (err) {
			console.log("Retrieval of information from " + tableName + " failed...");
			module.connect();
			module.updateSql(tableName, function(value) {
				callback(value);
			});
		} 
			
		callback(items);

	});
}

DBBean.prototype.selectOneSql = function(tableName, query, callback) {
	var collection = this.database.collection(tableName);
	var module = this;
	
	collection.findOne(query, function(err, item) {
		if (err) {
			console.log("Retrieval of information from " + tableName + " by " + JSON.stringify(query) + " failed...");
			module.connect();
			module.selectOneSql(tableName, query, function(value) {
				callback(value);
			});
		} 
		callback(item);	
	});
}

DBBean.prototype.updateSql = function(tableName, query, setValue, callback) {
	var collection = this.database.collection(tableName);
	var module = this;
	//console.log("Executing updating commanding for " + tableName + " with " + JSON.stringify(query) + " setting " + JSON.stringify(setValue));
	collection.update(query, {$set:setValue}, {w:1}, function(err, results) {
		if (err) {
			console.log("Update Failed...");
			module.connect();
			module.updateSql(tableName, query, setValue, function(value) {
				callback(value);
			});
		} 
		callback(results);
	});
}

DBBean.prototype.insertSql = function(tableName, objJson) {
	var collection = this.database.collection(tableName);
	var module = this;
	//console.log("Executing inserting commanding for " + tableName + " inserting values of: \n " + JSON.stringify(objJson));
	collection.insert(objJson, {w:1}, function(err, results) {
		if (err) {
			console.log("Insert Failed...");
			module.connect();
			module.insertSql(tableName, objJson);
		} 
	});
}

DBBean.prototype.removeAllSql = function(tableName) {
	var collection = this.database.collection(tableName); 
	var module = this;
	//console.log("Remove all rows from " + tableName);
	collection.remove(function(err) {
		if (err) {
			console.log("Remove Failed...");
			module.connect();
			module.removeAllSql(tableName);
		}
	});
}

DBBean.prototype.removeOneSql = function(tableName, remove_key, remove_value, callback) {
	var collection = this.database.collection(tableName);
	var module = this;
	
	console.log("Remove " + remove_key + ":" + remove_value + " from " + tableName);
	collection.remove({remove_key:remove_value}, {w:1}, function(err, results) {
		if (err) {
			console.log("Remove Failed...");
			module.connect();
			module.removeOneSql(tableName, remove_key, remove_value, function(value) {
				callback(value);
			});
		} 
		
		callback(results);
	});
}

DBBean.prototype.removeOneQuerySql = function(tableName, query, callback) {
	var collection = this.database.collection(tableName);
	var module = this;
	
	//console.log("Remove " + JSON.stringify(query) + " from " + tableName);
	collection.remove(query, {w:1}, function(err, results) {
		if (err) {
			console.log("Remove Failed...");
			module.connect();
			module.removeOneQuerySql(tableName, query, function(value) {
				callback(value);
			});
		} 
		
		callback(results);
	});
}

DBBean.prototype.countSql = function(tableName, query, callback) {
	
	var module = this;
	var collection = this.database.collection(tableName);
	console.log("Count with " + JSON.stringify(query) + " from " + tableName);
	
	collection.count(query, function(err, countval) {
		if (err) {
			console.log("Count Failed...");	
			module.connect();
			module.countSql(tableName, query, function(value) {
				callback(value);
			});
			
		}
		callback(countval);
    });
}

module.exports.DBBean = DBBean;