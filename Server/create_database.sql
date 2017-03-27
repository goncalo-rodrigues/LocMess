DROP DATABASE IF EXISTS cmu_locmess;

CREATE DATABASE cmu_locmess;
USE cmu_locmess;

CREATE TABLE Users (
	Username CHARACTER(100) NOT NULL PRIMARY KEY,
	Password CHARACTER(100) NOT NULL
);

CREATE TABLE Sessions (
	SessionID CHARACTER(128) NOT NULL PRIMARY KEY,
	Username CHARACTER(100) NOT NULL,
	FOREIGN KEY (Username) REFERENCES Users(Username) ON DELETE CASCADE
);

CREATE TABLE Users(
	Username CHARACTER(100) NOT NULL PRIMARY KEY,
	Password CHARACTER(100) NOT NULL
);


/*
TODO: Table from SEC project
*/
CREATE TABLE passwords(
	userID int ,
	username CHARACTER(50) ,
	domain CHARACTER(100),
	password CHARACTER(50) NOT NULL,
	PRIMARY KEY (userID,username,domain),
	FOREIGN KEY(userID)
		REFERENCES users(id)
		ON DELETE CASCADE
);

CREATE USER 'locmess_account'@'localhost' IDENTIFIED BY 'FDvlalaland129&&';

/* TODO: Check if this is needed for concurrent updates
GRANT SELECT,INSERT,UPDATE ON  sec_dpm.users TO 'dpm_account'@'localhost';
GRANT SELECT,INSERT,UPDATE ON  sec_dpm.passwords TO 'dpm_account'@'localhost';

-- Will only work on tables with SELECT privilege
GRANT LOCK TABLES ON * TO 'dpm_account'@'localhost';
*/
