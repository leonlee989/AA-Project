function Ask(stock, price, userId) {
	this.stock = stock;
	this.price = price;
	this.userId = userId;
	this.date = new Date();
}

Ask.prototype.getStock = function() {
	return this.stock;
}

Ask.prototype.getPrice = function() {
	return this.price;
}

Ask.prototype.getUserId = function() {
	return this.userId;
}

Ask.prototype.getDate = function() {
	return this.date.getMonth() + 1 + "-" + this.date.getDate() + "-" + this.date.getFullYear();
}

Ask.prototype.toString = function() {
	return "stock: " + this.stock + ", price: " + this.price + ", userId: " + this.userId + ", date: " + this.date;
}

module.exports.Ask = Ask;