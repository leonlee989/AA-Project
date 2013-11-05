function Bid (idvalue, stock, price, userId, date) {
	this.idvalue = idvalue;
	this.stock = stock;
	this.price = price;	
	this.userId = userId;
	this.date = date;
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
	return this.date;
	//return this.date.getMonth() + 1 + "-" + this.date.getDate() + "-" + this.date.getFullYear();
}

Bid.prototype.getId = function() {
	return this.idvalue;
}

Bid.prototype.setId = function(idvalue) {
	this.idvalue = idvalue;
}

Bid.prototype.getDateString = function() {
	var dateString = [[AddZero(this.date.getDate()), AddZero(this.date.getMonth() + 1), this.date.getFullYear()].join("/"), [AddZero(this.date.getHours()), AddZero(this.date.getMinutes())].join(":"), this.date.getHours() >= 12 ? "PM" : "AM"].join(" ");
	return dateString;
}

Bid.prototype.toString = function() {
	var dateString = [[AddZero(this.date.getDate()), AddZero(this.date.getMonth() + 1), this.date.getFullYear()].join("/"), [AddZero(this.date.getHours()), AddZero(this.date.getMinutes())].join(":"), this.date.getHours() >= 12 ? "PM" : "AM"].join(" ");
	return "stock: " + this.stock + ", price: " + this.price + ", userId: " + this.userId + ", date: " + dateString;
}

//Pad given value to the left with "0"
function AddZero(num) {
    return (num >= 0 && num < 10) ? "0" + num : num + "";
}

module.exports.Bid = Bid;