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
	id bigint not null auto_increment,
	stockName varchar(10) not null,
	price int not null,
	userID varchar(50) not null,
	askDate TimeStamp not null,
	
  PRIMARY KEY (id)
) ; 

DROP TABLE IF EXISTS bid;
CREATE TABLE bid (
	id bigint not null auto_increment,
	stockName varchar(10) not null,
	price int not null,
	userID varchar(50) not null,
	bidDate TimeStamp not null,
	
  PRIMARY KEY (id)
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
	askID int,
	bidID int,
	
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

DROP TABLE IF EXISTS backOfficeLog;
CREATE TABLE backOfficeLog(
	id bigint not null auto_increment,
	logStatement varchar(500),
	
	PRIMARY KEY(id)
);

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
DROP PROCEDURE IF EXISTS CHECK_IF_ASK_EXISTS;
DROP PROCEDURE IF EXISTS CHECK_IF_BID_EXISTS;
DROP PROCEDURE IF EXISTS GET_FILTERED_ASKS;
DROP PROCEDURE IF EXISTS GET_FILTERED_BIDS;
DROP PROCEDURE IF EXISTS INSERT_BACKOFFICE_LOG;
DROP PROCEDURE IF EXISTS CHECK_IF_BACKOFFICEMESSAGE_EXISTS;
DROP PROCEDURE IF EXISTS DUMP_FROM_BACKOFFICE;
DROP PROCEDURE IF EXISTS CLEAR_BACKOFFICE;
DROP PROCEDURE IF EXISTS DELETE_BACKOFFICELOG;
DROP PROCEDURE IF EXISTS GET_ALL_CREDIT;
DROP PROCEDURE IF EXISTS DUMP_FROM_TRANSACTIONDB;
DROP PROCEDURE IF EXISTS LAST_ID_FROM_CLIENT;

DELIMITER $$
			
			CREATE PROCEDURE LAST_ID_FROM_CLIENT()
				BEGIN
					select last_insert_id();
				END $$
			
			CREATE PROCEDURE GET_STOCK_PRICE(IN stockName varchar(10))
				BEGIN
					select stock.price from stock WHERE stock.stockName = stockName;
				END $$
			
			CREATE PROCEDURE DUMP_FROM_TRANSACTIONDB()
				BEGIN
					select * from matchedTransactionDB;
				END $$
			
			CREATE PROCEDURE GET_ALL_CREDIT()
				BEGIN
					select * from credit;
				END $$
			
			CREATE PROCEDURE GET_FILTERED_ASKS(IN stockName varchar(10))
				BEGIN
					select * from ask WHERE ask.stockName = stockName;
				END $$
				
			CREATE PROCEDURE GET_FILTERED_BIDS(IN stockName varchar(10))
				BEGIN
					select * from bid WHERE bid.stockName = stockName;
				END $$
			
			CREATE PROCEDURE CHECK_IF_BACKOFFICEMESSAGE_EXISTS()
				BEGIN
					select 1 from backOfficeLog LIMIT 1;
				END $$
			
			CREATE PROCEDURE DUMP_FROM_BACKOFFICE()
				BEGIN
					select backOfficeLog.logStatement from backOfficeLog;
				END $$
				
			CREATE PROCEDURE CLEAR_BACKOFFICE()
				BEGIN
					truncate table backOfficeLog;
				END $$
			
			CREATE PROCEDURE CHECK_IF_ASK_EXISTS(IN stockName varchar(10))
				BEGIN
					select 1 from ask WHERE ask.stockName = stockName;
				END $$
				
			CREATE PROCEDURE CHECK_IF_BID_EXISTS(IN stockName varchar(10))
				BEGIN
					select 1 from bid WHERE bid.stockName = stockName;
				END $$
			
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
			
			CREATE PROCEDURE DELETE_BACKOFFICELOG(IN logStatement varchar(500))
				BEGIN
					delete from backOfficeLog where backOfficeLog.logstatement  = logStatement ;
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
			
			CREATE PROCEDURE INSERT_BACKOFFICE_LOG(IN logStatement varchar(500))
				BEGIN
					insert into backOfficeLog (backOfficeLog.logStatement) VALUES (logStatement);
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
			CREATE PROCEDURE INSERT_MATCHED_TRANSACTION(IN bidPrice int, IN bidUserID varchar(50), IN bidDate TimeStamp, IN askPrice int, IN askUserID varchar(50), IN askDate TimeStamp, IN matchDate TimeStamp, IN price int, IN stockName varchar(10), IN askID int, IN bidID int)
				BEGIN
					insert into matchedtransactiondb (matchedtransactiondb.bidPrice, matchedtransactiondb.bidUserID, matchedtransactiondb.bidDate, matchedtransactiondb.askPrice, matchedtransactiondb.askUserID, matchedtransactiondb.askDate, matchedtransactiondb.matchDate, matchedtransactiondb.price, matchedtransactiondb.stockName, matchedtransactiondb.askID, matchedtransactiondb.bidID )  VALUES (bidPrice, bidUserID, bidDate, askPrice, askUserID, askDate, matchDate, price, stockName, askID, bidID);
				END $$
			
			CREATE PROCEDURE UPDATE_STOCK_PRICE(IN price int, IN stockName varchar(10))
				BEGIN
					UPDATE stock SET stock.price = price WHERE stock.stockName = stockName;
				END $$
				
DELIMITER ;