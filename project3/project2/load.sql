LOAD DATA LOCAL INFILE 'users.dat' INTO table Users
	FIELDS TERMINATED BY '|*|' 
	LINES TERMINATED BY '\n' STARTING BY '';

LOAD DATA LOCAL INFILE 'bidders.dat' INTO table Bidders
	FIELDS TERMINATED BY '|*|' 
	LINES TERMINATED BY '\n' STARTING BY '';

LOAD DATA LOCAL INFILE 'sellers.dat' INTO table Sellers
	FIELDS TERMINATED BY '|*|' 
	LINES TERMINATED BY '\n' STARTING BY '';

LOAD DATA LOCAL INFILE 'items.dat' INTO table Items
	FIELDS TERMINATED BY '|*|' 
	LINES TERMINATED BY '\n' STARTING BY '';

LOAD DATA LOCAL INFILE 'categories.dat' INTO table Categories
	FIELDS TERMINATED BY '|*|' 
	LINES TERMINATED BY '\n' STARTING BY '';

LOAD DATA LOCAL INFILE 'itemCategory.dat' INTO table ItemCategory
	FIELDS TERMINATED BY '|*|' 
	LINES TERMINATED BY '\n' STARTING BY '';

LOAD DATA LOCAL INFILE 'bids.dat' INTO table Bids
	FIELDS TERMINATED BY '|*|'
	LINES TERMINATED BY '\n' STARTING BY '';
