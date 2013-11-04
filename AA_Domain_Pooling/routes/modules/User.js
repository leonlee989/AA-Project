function User = function(userId, credit_limit) {
	this.userId = userId;
	this.credit_limit = credit_limit;
}

User.prototype.getUserId = function() {
	return this.userId;
}

User.prototype.getCredit_Limit = function() {
	return this.credit_limit;
}

User.prototype.gtoString = function() {
	var userString = "User ID" + this.userId + ", Credit Limit: " + this.credit_limit;
	return userString;
}

module.exports.User = User;