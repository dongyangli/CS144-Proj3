
CREATE TABLE IF NOT EXISTS ItemLocation (
	ItemID 			INT(11) NOT NULL,
	Location 			POINT NOT NULL,
	PRIMARY KEY(ItemID)
) ENGINE = MYISAM;

INSERT INTO TABLE ItemRegion(ItemID, Location) (SELECT ItemID, POINT(Latitude, Longitude) AS Location FROM Items WHERE Longitude IS NOT NUL
L AND Latitude IS NOT NULL);