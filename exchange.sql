DROP DATABASE IF EXISTS exchange;
CREATE DATABASE exchange;
USE exchange;

DROP TABLE IF EXISTS credit;
CREATE TABLE credit (
	userid varchar(20) not null,
	credit_limit int not null,
	
  PRIMARY KEY (userid)
) ; 

DROP TABLE IF EXISTS stock;
CREATE TABLE stock (
	stockName varchar(10) not null,
	price int not null,
	
  PRIMARY KEY (stockName)
) ; 

DROP TABLE IF EXISTS ask;
CREATE TABLE ask (
	stockName varchar(10) not null,
	price int not null,
	userID varchar(50) not null,
	askDate TimeStamp not null,
	
  PRIMARY KEY (stockName,price,userID,askDate)
) ; 

DROP TABLE IF EXISTS bid;
CREATE TABLE bid (
	stockName varchar(10) not null,
	price int not null,
	userID varchar(50) not null,
	bidDate TimeStamp not null,
	
  PRIMARY KEY (stockName,price,userID,bidDate)
) ; 

DROP TABLE IF EXISTS matchedTransactionDB;
CREATE TABLE matchedTransactionDB (
	id bigint not null auto_increment,
	bidPrice int not null,
	bidUserID varchar(50) not null,
	bidDate TimeStamp not null,
	askPrice int not null,
	askUserID varchar(50) not null,
	askDate TimeStamp not null,
	matchDate TimeStamp not null,
	price int not null,
	stockName varchar(10) not null,
	
  PRIMARY KEY (id)
) ; 

DROP TABLE IF EXISTS rejectedLog;
CREATE TABLE rejectedLog (
	id bigint not null auto_increment,
	logStatement varchar(500),
	
	PRIMARY KEY(id)
) ;

DROP TABLE IF EXISTS matchedLog;
CREATE TABLE matchedLog (
	id bigint not null auto_increment,
	logStatement varchar(500),
	
	PRIMARY KEY(id)
) ;

ALTER TABLE ask ADD INDEX askPriceDate(price,askDate);
ALTER TABLE bid ADD INDEX bidPriceDate(price,bidDate);

INSERT INTO stock (stockName,price) VALUES ('smu',-1);
INSERT INTO stock (stockName,price) VALUES ('nus',-1);
INSERT INTO stock (stockName,price) VALUES ('ntu',-1);

DROP PROCEDURE IF EXISTS GET_HIGHEST_BID;
DROP PROCEDURE IF EXISTS GET_LOWEST_ASK;
DROP PROCEDURE IF EXISTS UPDATE_CREDIT_LIMIT;
DROP PROCEDURE IF EXISTS DELETE_BID;
DROP PROCEDURE IF EXISTS DELETE_ASK;
DROP PROCEDURE IF EXISTS INSERT_BID;
DROP PROCEDURE IF EXISTS INSERT_ASK;
DROP PROCEDURE IF EXISTS INSERT_MATCHED_LOG;
DROP PROCEDURE IF EXISTS INSERT_REJECTED_LOG;
DROP PROCEDURE IF EXISTS INSERT_PRICE;
DROP PROCEDURE IF EXISTS GET_USER_CREDIT_LIMIT;
DROP PROCEDURE IF EXISTS INSERT_USER_CREDIT;
DROP PROCEDURE IF EXISTS INSERT_MATCHED_TRANSACTION;
DROP PROCEDURE IF EXISTS UPDATE_STOCK_PRICE;

DELIMITER $$
			
			CREATE PROCEDURE GET_HIGHEST_BID(IN stockName varchar(10))
				BEGIN
					select * from bid WHERE bid.stockName = stockName order by bid.price DESC, bid.bidDate ASC LIMIT 1;
				END $$
			
			CREATE PROCEDURE GET_LOWEST_ASK(IN stockName varchar(10))
				BEGIN
					select * from ask WHERE ask.stockName = stockName order by ask.price ASC, ask.askDate ASC LIMIT 1;
				END $$
			
			CREATE PROCEDURE UPDATE_CREDIT_LIMIT(IN credit_limit int, IN userid varchar(20))
				BEGIN
					update credit set credit.credit_limit= credit_limit where credit.userid=userid;
				END $$
			
			CREATE PROCEDURE DELETE_BID(IN stockName varchar(10), IN price int, IN userID varchar(50), IN bidDate TimeStamp)
				BEGIN
					delete from bid where bid.stockName = stockName and bid.price = price and bid.userID = userID and bid.bidDate = bidDate;
				END $$
			
			CREATE PROCEDURE DELETE_ASK(IN stockName varchar(10), IN price int, IN userID varchar(50), IN askDate TimeStamp)
				BEGIN
					delete from ask where ask.stockName = stockName and ask.price = price and ask.userID = userID and ask.askDate = askDate;
				END $$
			
			CREATE PROCEDURE INSERT_BID(IN stockName varchar(10), IN price int, IN userID varchar(50), IN bidDate TimeStamp)
				BEGIN
					insert into bid (bid.stockName, bid.price, bid.userID, bid.bidDate) VALUES (stockName, price, userID, bidDate);
				END $$
			
			CREATE PROCEDURE INSERT_ASK(IN stockName varchar(10), IN price int, IN userID varchar(50), IN askDate TimeStamp)
				BEGIN
					insert into ask (ask.stockName, ask.price, ask.userID, ask.askDate) VALUES (stockName, price, userID, askDate);
				END $$
			
			CREATE PROCEDURE INSERT_MATCHED_LOG(IN logStatement varchar(500))
				BEGIN
					insert into matchedLog (matchedLog.logStatement) VALUES (logStatement);
				END $$
			
			CREATE PROCEDURE INSERT_REJECTED_LOG(IN logStatement varchar(500))
				BEGIN
					insert into rejectedLog (rejectedLog.logStatement) VALUES (logStatement);
				END $$
			
			CREATE PROCEDURE INSERT_PRICE(IN price int, IN stockName varchar(10))
				BEGIN
					insert into stock (stock.stockName,stock.price) VALUES (stockName,price);
				END $$
			
			CREATE PROCEDURE GET_USER_CREDIT_LIMIT(IN buyerID varchar(50))
				BEGIN
					select credit.credit_limit from credit where credit.userid = buyerID;
				END $$
			
			CREATE PROCEDURE INSERT_USER_CREDIT(IN userid varchar(20), IN credit_limit int)
				BEGIN
					insert into credit (credit.userid,credit.credit_limit) VALUES (userid,credit_limit);
				END $$
			
			-- Part of this procedure is to handle the locking of the insert statement
			CREATE PROCEDURE INSERT_MATCHED_TRANSACTION(IN bidPrice int, IN bidUserID varchar(50), IN bidDate TimeStamp, IN askPrice int, IN askUserID varchar(50), IN askDate TimeStamp, IN matchDate TimeStamp, IN price int, IN stockName varchar(10))
				BEGIN
				
					insert into matchedtransactiondb (matchedtransactiondb.id, matchedtransactiondb.bidPrice, matchedtransactiondb.bidUserID, matchedtransactiondb.bidDate, matchedtransactiondb.askPrice, matchedtransactiondb.askUserID, matchedtransactiondb.askDate, matchedtransactiondb.matchDate, matchedtransactiondb.price, matchedtransactiondb.stockName)  VALUES (0,bidPrice, bidUserID, bidDate, askPrice, askUserID, askDate, matchDate, price, stockName);
				END $$
			
			CREATE PROCEDURE UPDATE_STOCK_PRICE(IN price int, IN stockName varchar(10))
				BEGIN
					UPDATE stock SET stock.price = price WHERE stock.stockName = stockName;
				END $$
				
DELIMITER ;