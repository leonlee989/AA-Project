CREATE USER 'root'@'192.168.1.2' IDENTIFIED BY '';
GRANT EXECUTE,SELECT,INSERT,UPDATE,DELETE,CREATE,DROP ON exchange.* TO 'root'@'192.168.1.2';

CREATE USER 'root'@'192.168.1.8' IDENTIFIED BY '';
GRANT EXECUTE,SELECT,INSERT,UPDATE,DELETE,CREATE,DROP ON exchange.* TO 'root'@'192.168.1.8';

CREATE USER 'root'@'192.168.1.20' IDENTIFIED BY '';
GRANT EXECUTE,SELECT,INSERT,UPDATE,DELETE,CREATE,DROP ON exchange.* TO 'root'@'192.168.1.20';