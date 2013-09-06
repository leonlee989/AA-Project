function Bid (stock, price, userId) {
	this.stock = stock;
	this.price = price;	
	this.userId = userId;
	this.date = new Date();
}

Bid.prototype.getStock = function() {
	return this.stock;
}

Bid.prototype.getPrice = function() {
	return this.price;
}

Bid.prototype.getUserId = function() {
	return this.userId;
}

Bid.prototype.getDate = function() {
	return this.date.getMonth() + 1 + "-" + this.date.getDate() + "-" + this.date.getFullYear();
}

Bid.prototype.toString = function() {
	return "stock: " + this.stock + ", price: " + this.price + ", userId: " + this.userId + ", date: " + this.date;
}

module.exports.Bid = Bid;