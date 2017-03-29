DROP DATABASE IF EXISTS cmu_locmess;

CREATE DATABASE cmu_locmess;
USE cmu_locmess;

CREATE TABLE Users (
	Username CHARACTER(100) NOT NULL PRIMARY KEY,
	Password CHARACTER(100) NOT NULL
);

-- This ID should be randomly generated
CREATE TABLE Sessions (
	SessionID CHARACTER(128) NOT NULL PRIMARY KEY,
	Username CHARACTER(100) NOT NULL,
	FOREIGN KEY (Username) REFERENCES Users(Username) ON DELETE CASCADE
);

CREATE TABLE Locations (
	Name VARCHAR(256) NOT NULL PRIMARY KEY
);

-- This ID comes from the Android device
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

-- This ID is composed by: Username || User counter.
CREATE TABLE Messages (
	MessageID CHARACTER(255) NOT NULL PRIMARY KEY,
	Username CHARACTER(100) NOT NULL,
	Location VARCHAR(256) NOT NULL,
	StartDate INT NOT NULL,
	EndDate INT NOT NULL,
	Content VARCHAR(512) NOT NULL,
	FOREIGN KEY (Username) REFERENCES Users(Username) ON DELETE CASCADE,
	FOREIGN KEY (Location) REFERENCES Locations(Name) ON DELETE CASCADE
);

-- This ID is internal to the Server side
CREATE TABLE Filters (
	FilterID INT NOT NULL PRIMARY KEY AUTO_INCREMENT,
	FilterKey VARCHAR(100) NOT NULL,
	FilterValue VARCHAR(100) NOT NULL,
	UNIQUE (FilterKey, FilterValue)
);

CREATE TABLE MessageFilters (
	MessageID CHARACTER(255) NOT NULL,
	FilterID INT NOT NULL AUTO_INCREMENT,
	Whitelist BIT(1) NOT NULL,
	PRIMARY KEY (MessageID, FilterID),
	FOREIGN KEY (MessageID) REFERENCES Messages(MessageID) ON DELETE CASCADE,
	FOREIGN KEY (FilterID) REFERENCES Filters(FilterID) ON DELETE CASCADE
);

CREATE TABLE UserFilters (
	Username CHARACTER(100) NOT NULL,
	FilterID INT NOT NULL AUTO_INCREMENT,
	PRIMARY KEY (Username, FilterID),
	FOREIGN KEY (Username) REFERENCES Users(Username) ON DELETE CASCADE,
	FOREIGN KEY (FilterID) REFERENCES Filters(FilterID) ON DELETE CASCADE
);

CREATE USER 'locmess_account'@'localhost' IDENTIFIED BY 'FDvlalaland129&&';

GRANT SELECT,INSERT,UPDATE ON cmu_locmess.* TO 'locmess_account'@'localhost';
