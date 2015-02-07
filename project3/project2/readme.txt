# CS144-Proj2
#Dongyang Li, 404408946
#Wenting Li, 004434117

Answers to Part B:

1. Relations
	Users(*UserID, Location, Latitude, Longitude, Country)
	Sellers(*UserID, Rating)
	Bidders(*UserID, Rating)
	Items(*ItemID, SellerID, Name, Location, Latitude, Longitude, Country, Description, BuyPrice, FirstBid, Currently, NumberOfBids, Started, Ends)
	Categories(*Category)
	ItemCategory(*ItemID, *Category)
	Bids(*BidderID, *ItemID, *Time, Amount)

2. List all completely nontrivial functional dependencies that hold on each relation, excluding those that effectively specify keys.
	UserID -> Location, Latitude, Longitude, Country
	UserID -> Rating (either in Sellers or Bidders)
	ItemID -> SellerID, Name, Location, Latitude, Longitude, Country, Description, BuyPrice, FirstBid, Currently, NumberOfBids, Started, Ends
	BidderID, ItemID, Time -> Amount

3. Are all of your relations in Boyce-Codd Normal Form (BCNF)?
	Yes
	
4. Are all of your relations in Fourth Normal Form (4NF)?
	Yes


