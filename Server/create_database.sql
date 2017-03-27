DROP DATABASE IF EXISTS cmu_locmess;

CREATE DATABASE cmu_locmess;
USE cmu_locmess;

-- TODO: Check if some of the IDs can be autoincremented integers!!!

CREATE TABLE Users (
	Username CHARACTER(100) NOT NULL PRIMARY KEY,
	Password CHARACTER(100) NOT NULL
);

CREATE TABLE Sessions (
	SessionID CHARACTER(128) NOT NULL PRIMARY KEY,
	Username CHARACTER(100) NOT NULL,
	FOREIGN KEY (Username) REFERENCES Users(Username) ON DELETE CASCADE
);

CREATE TABLE Locations (
	Name VARCHAR(256) NOT NULL PRIMARY KEY
);

CREATE TABLE WifiIDs (
	Location VARCHAR(256) NOT NULL,
	WifiID CHARACTER(100) NOT NULL,
	PRIMARY KEY (Location, WifiID),
	FOREIGN KEY (Location) REFERENCES Locations(Name) ON DELETE CASCADE
);

CREATE TABLE GPS (
	Location VARCHAR(256) NOT NULL PRIMARY KEY,
	Latitude DOUBLE NOT NULL,
	Longitude DOUBLE NOT NULL,
	Radius FLOAT NOT NULL,
	FOREIGN KEY (Location) REFERENCES Locations(Name) ON DELETE CASCADE
);

CREATE TABLE Messages (
	MessageID CHARACTER(256) NOT NULL PRIMARY KEY,
	Username CHARACTER(100) NOT NULL,
	Location VARCHAR(256) NOT NULL,
	StartDate INT NOT NULL,
	EndDate INT NOT NULL,
	Content VARCHAR(512) NOT NULL,
	FOREIGN KEY (Username) REFERENCES Users(Username) ON DELETE CASCADE,
	FOREIGN KEY (Location) REFERENCES Locations(Name) ON DELETE CASCADE
);

CREATE TABLE Filters (
	FilterID CHARACTER(256) NOT NULL PRIMARY KEY,
	Key VARCHAR(100) NOT NULL,
	Value VARCHAR(100) NOT NULL,
	UNIQUE KEY (Key, Value)
);

CREATE TABLE MessageFilters (
	MessageID CHARACTER(256) NOT NULL,
	FilterID CHARACTER(256) NOT NULL,
	Whitelist BIT(1) NOT NULL,
	PRIMARY KEY (MessageID, FilterID),
	FOREIGN KEY (MessageID) REFERENCES Messages(MessageID) ON DELETE CASCADE,
	FOREIGN KEY (FilterID) REFERENCES Filters(FilterID) ON DELETE CASCADE
);

CREATE USER 'locmess_account'@'localhost' IDENTIFIED BY 'FDvlalaland129&&';

/* TODO: Check if this is needed for concurrent updates
GRANT SELECT,INSERT,UPDATE ON  sec_dpm.users TO 'dpm_account'@'localhost';
GRANT SELECT,INSERT,UPDATE ON  sec_dpm.passwords TO 'dpm_account'@'localhost';

-- Will only work on tables with SELECT privilege
GRANT LOCK TABLES ON * TO 'dpm_account'@'localhost';
*/
