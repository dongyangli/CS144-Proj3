
CREATE TABLE IF NOT EXISTS ItemLocation (
	ItemID 			INT(11) NOT NULL,
	Location 			POINT NOT NULL,
	PRIMARY KEY(ItemID),
	SPATIAL INDEX(Location)
) ENGINE = MYISAM;

INSERT INTO ItemLocation(ItemID, Location) SELECT ItemID, POINT(Latitude, Longitude) FROM Items WHERE Longitude IS NOT NULL AND Latitude IS NOT NULL;

#ALTER TABLE ItemLocation ADD SPATIAL INDEX(Location);
#CREATE SPATIAL INDEX sp_index ON ItemLocation (Location);

