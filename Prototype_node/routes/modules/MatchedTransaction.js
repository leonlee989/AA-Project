function MatchedTransaction(bid, ask, date, price) {
	this.bid = bid;
	this.ask = ask;
	this.date = date;
	this.price = price;
	this.stock = bid.getStock();
}

MatchedTransaction.prototype.getStock = function() {
	return this.stock;
}

MatchedTransaction.prototype.getPrice = function() {
	return this.price;
}

MatchedTransaction.prototype.getDate = function() {
	return this.date;
}

MatchedTransaction.prototype.getBuyerId = function() {
	return this.bid.getUserId();
}

MatchedTransaction.prototype.getSellerId = function() {
	return this.ask.getUserId();
}

MatchedTransaction.prototype.toString = function() {
	var dateString = [[AddZero(this.date.getDate()), AddZero(this.date.getMonth() + 1), this.date.getFullYear()].join("/"), [AddZero(this.date.getHours()), AddZero(this.date.getMinutes())].join(":"), this.date.getHours() >= 12 ? "PM" : "AM"].join(" ");
	return "stock: " + this.stock + ", amt: " + this.price + ", bidder userId: " + this.bid.getUserId() + ", seller userId: " + this.ask.getUserId() + ", date: " + dateString;
}

//Pad given value to the left with "0"
function AddZero(num) {
    return (num >= 0 && num < 10) ? "0" + num : num + "";
}

module.exports.MatchedTransaction = MatchedTransaction;