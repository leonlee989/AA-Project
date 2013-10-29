DROP DATABASE IF EXISTS exchange;
CREATE DATABASE exchange;
USE exchange;

DROP TABLE IF EXISTS credit;
CREATE TABLE credit (
	userid varchar(20) not null,
	credit_limit int not null,
	
  PRIMARY KEY (userid)
); 

DROP TABLE IF EXISTS stock;
CREATE TABLE stock (
	stockName varchar(10) not null,
	price int not null,
	
  PRIMARY KEY (stockName)
); 

DROP TABLE IF EXISTS ask;
CREATE TABLE ask (
	stockName varchar(10) not null,
	price int not null,
	userID varchar(50) not null,
	askDate TimeStamp not null,
	
  PRIMARY KEY (stockName,price,userID,askDate)
); 

DROP TABLE IF EXISTS bid;
CREATE TABLE bid (
	stockName varchar(10) not null,
	price int not null,
	userID varchar(50) not null,
	bidDate TimeStamp not null,
	
  PRIMARY KEY (stockName,price,userID,bidDate)
); 

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
); 

DROP TABLE IF EXISTS rejectedLog;
CREATE TABLE rejectedLog (
	id bigint not null auto_increment,
	logStatement varchar(500),
	
	PRIMARY KEY(id)
);

DROP TABLE IF EXISTS matchedLog;
CREATE TABLE matchedLog (
	id bigint not null auto_increment,
	logStatement varchar(500),
	
	PRIMARY KEY(id)
);

INSERT INTO stock (stockName,price) VALUES ('smu',-1);
INSERT INTO stock (stockName,price) VALUES ('nus',-1);
INSERT INTO stock (stockName,price) VALUES ('ntu',-1);
