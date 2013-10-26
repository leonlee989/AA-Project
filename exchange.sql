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
	stock varchar(10) not null,
	price int not null,
	
  PRIMARY KEY (stock)
); 

DROP TABLE IF EXISTS ask;
CREATE TABLE ask (
	stock varchar(10) not null,
	price int not null,
	userID varchar(50) not null,
	askDate TimeStamp not null,
	
  PRIMARY KEY (stock,price,userID,askDate),
  CONSTRAINT fk_askStock FOREIGN KEY (stock) REFERENCES stock(stock)
); 

DROP TABLE IF EXISTS bid;
CREATE TABLE bid (
	stock varchar(10) not null,
	price int not null,
	userID varchar(50) not null,
	bidDate TimeStamp not null,
	
  PRIMARY KEY (stock,price,userID,bidDate),
  CONSTRAINT fk_bidStock FOREIGN KEY (stock) REFERENCES stock(stock)
); 

DROP TABLE IF EXISTS matches;
CREATE TABLE matches (
	id bigint not null auto_increment,
	bidPrice int not null,
	bidUserID varchar(50) not null,
	bidDate TimeStamp not null,
	askPrice int not null,
	askUserID varchar(50) not null,
	askDate TimeStamp not null,
	matchDate TimeStamp not null,
	price int not null,
	stock varchar(10) not null,
	
  PRIMARY KEY (id),
  CONSTRAINT fk_askDetails FOREIGN KEY (stock,askPrice,askUserID,askDate) REFERENCES ask(stock,price,userID,askDate),
  CONSTRAINT fk_bidDetails FOREIGN KEY (stock,bidPrice,bidUserID,bidDate) REFERENCES bid(stock,price,userID,bidDate)
); 