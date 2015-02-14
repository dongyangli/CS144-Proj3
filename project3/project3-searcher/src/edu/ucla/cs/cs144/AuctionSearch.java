package edu.ucla.cs.cs144;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.File;
import java.util.Date;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.List;
import java.text.SimpleDateFormat;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.PreparedStatement;

import org.apache.lucene.document.Document;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

import edu.ucla.cs.cs144.DbManager;
import edu.ucla.cs.cs144.SearchRegion;
import edu.ucla.cs.cs144.SearchResult;

import java.sql.Timestamp;
import java.text.StringCharacterIterator;
import java.text.CharacterIterator;
import java.util.HashMap;


public class AuctionSearch implements IAuctionSearch {

	/* 
         * You will probably have to use JDBC to access MySQL data
         * Lucene IndexSearcher class to lookup Lucene index.
         * Read the corresponding tutorial to learn about how to use these.
         *
	 * You may create helper functions or classes to simplify writing these
	 * methods. Make sure that your helper functions are not public,
         * so that they are not exposed to outside of this class.
         *
         * Any new classes that you create should be part of
         * edu.ucla.cs.cs144 package and their source files should be
         * placed at src/edu/ucla/cs/cs144.
         *
         */
	private IndexSearcher searcher = null;
    private QueryParser parser = null;
    private Connection conn = null;

    private void initLuceneIndex() throws IOException{
		searcher = new IndexSearcher(DirectoryReader.open(FSDirectory.open(new File("/var/lib/lucene/"))));
        parser = new QueryParser("content", new StandardAnalyzer());
    }

	public SearchResult[] basicSearch(String query, int numResultsToSkip, 
			int numResultsToReturn) {
		// TODO: Your code here!
		ArrayList<SearchResult> resultList = new ArrayList<SearchResult>();
		try {
			initLuceneIndex();
		} catch (IOException ex) {
			System.out.println(ex);
		}
		
		Query parsedQuery = null;
		try {
			parsedQuery = parser.parse(query);
		}catch (ParseException ex) {
			System.out.println(ex);
		}

		TopDocs topDocs = null;
		try {
			topDocs = searcher.search(parsedQuery, numResultsToSkip + numResultsToReturn);
			ScoreDoc[] hits = topDocs.scoreDocs;
			for (int i = numResultsToSkip; i < hits.length; i++) {
				Document doc = searcher.doc(hits[i].doc);;
    			String name = doc.get("Name");
    			String itemId = doc.get("ItemID");
    			resultList.add(new SearchResult(itemId, name));
			}

		}catch (IOException ex) {
			System.out.println(ex);
		}
		
		// put results into array
		SearchResult[] searchResults = new SearchResult[resultList.size()];
		for ( int i = 0; i < resultList.size() ;i++ ) {
			searchResults[i] = resultList.get(i);
		}
		return searchResults;
	}

	private String prepareSpatialSQLQuery(SearchRegion region) {
		//SET @g1 = GeomFromText('Polygon((33.774 -118.63, 33.774 -117.38, 34.201 -117.38, 34.201 -118.63, 33.774 -118.63))');
		//SELECT ItemID, AsText(Location) FROM ItemLocation WHERE MBRContains(@g1, Location);
		String polygonVar = "GeomFromText(' Polygon((" + Double.toString(region.getLx()) + " " + Double.toString(region.getLy()) + ", " + 
										Double.toString(region.getLx()) + " " + Double.toString(region.getRy()) + ", " +
										Double.toString(region.getRx()) + " " + Double.toString(region.getRy()) + ", " +
										Double.toString(region.getRx()) + " " + Double.toString(region.getLy()) + ", " +
										Double.toString(region.getLx()) + " " + Double.toString(region.getLy()) + ")) ')";
		String sqlQuery = "SELECT ItemID FROM ItemLocation WHERE MBRContains("+ polygonVar +", Location);";
		return sqlQuery;
	}
	public SearchResult[] spatialSearch(String query, SearchRegion region,
			int numResultsToSkip, int numResultsToReturn) {
		// TODO: Your code here!
		// word requirement
		SearchResult[] basicSearchResults = basicSearch(query, 0, 19532);
		HashMap<String, String> hash = new HashMap<>();
		for (int i = 0; i < basicSearchResults.length ; i++ ) {
			hash.put(basicSearchResults[i].getItemId(), basicSearchResults[i].getName());
		}
		// spatial requirement 
		ArrayList<SearchResult> resultList = new ArrayList<SearchResult>();
		
		//String sqlQuery = prepareSpatialSQLQuery(region);
		String polygonVar = "GeomFromText(' Polygon((" + Double.toString(region.getLx()) + " " + Double.toString(region.getLy()) + ", " + 
										Double.toString(region.getLx()) + " " + Double.toString(region.getRy()) + ", " +
										Double.toString(region.getRx()) + " " + Double.toString(region.getRy()) + ", " +
										Double.toString(region.getRx()) + " " + Double.toString(region.getLy()) + ", " +
										Double.toString(region.getLx()) + " " + Double.toString(region.getLy()) + ")) ')";
		String sqlQuery = "SELECT ItemID FROM ItemLocation WHERE MBRContains(?, Location);";

		try {
			conn = DbManager.getConnection(true);
			//Statement stmt = conn.createStatement();
			//ResultSet rs = stmt.executeQuery(sqlQuery);
			PreparedStatement preparesql = conn.prepareStatement(sqlQuery);
        	preparesql.setString(1, polygonVar);
        	ResultSet rs = preparesql.executeQuery();

			while(rs.next()) {
				String itemId = Integer.toString(rs.getInt("ItemID"));
				if (hash.containsKey(itemId)) {
					resultList.add(new SearchResult(itemId, hash.get(itemId)));
				}
			}

		}catch (SQLException ex ){
			System.out.println(ex);
		}

		// put results into array
		SearchResult[] searchResults = new SearchResult[resultList.size()];
		for ( int i = numResultsToSkip; i < resultList.size() && i < numResultsToReturn + numResultsToSkip; i++ ) {
			searchResults[i] = resultList.get(i);
		}
		return searchResults;
	}

	// helper methods for getXMLDataForItemId
	private String stringToXML(String text){

		StringBuilder result = new StringBuilder();
		StringCharacterIterator iterator = new StringCharacterIterator(text);
		char character =  iterator.current();
		while (character != CharacterIterator.DONE ){
      		if (character == '<') {
        		result.append("&lt;");
      		} else if (character == '>') {
      			result.append("&gt;");
      		} else if (character == '\"') {
      			result.append("&quot;");
      		} else if (character == '\'') {
      			result.append("&apos;");
      		} else if (character == '&') {
      			result.append("&amp;");
      		} else if (character == '\\') {
      			result.append("\\");
      		} else {
      			//the char is not a special one
      			//add it to the result as is
      			result.append(character);
      		}
      		character = iterator.next();
      	}
    	return result.toString();
	}

	private String bidXMLBuildHelper(String xmlString, String bidderID, String dbTime, String amount){
		String rating = "";
		String location = "";
		String country = "";

		try {
			conn = DbManager.getConnection(true);
			//Statement stmt = conn.createStatement();
			//ResultSet rs = stmt.executeQuery("SELECT * FROM Bidders WHERE UserID ='" + bidderID + "'");

			PreparedStatement prepareSelectsql = conn.prepareStatement("SELECT * FROM Bidders WHERE UserID ='?'");
            prepareSelectsql.setString(1, bidderID);
            ResultSet rs = prepareSelectsql.executeQuery();

			while(rs.next()) {
				rating = Integer.toString(rs.getInt("Rating"));
				location = rs.getString("Location");
				country = rs.getString("Country");
			}
			xmlString += "		<Bid>" + "\n";
			xmlString += "			<Bidder " + "UserID=\"" + stringToXML(bidderID) + "\" " + "Rating=\"" + stringToXML(rating) + "\">" + "\n";
			xmlString += "				<Location>" + stringToXML(location) + "</Location>" + "\n";
			xmlString += "				<Country>" + stringToXML(country) + "</Country>" + "\n";
			xmlString += "			</Bidder>" + "\n";
			xmlString += "			<Time>" + stringToXML(convertTime(dbTime)) + "</Time>" + "\n";
			xmlString += "			<Amount>" + "$" + stringToXML(amount) + "</Amount>" + "\n";
			xmlString += "		</Bid>" + "\n";
		} catch(SQLException ex){
			System.out.println(ex);
		}

		return xmlString;
	}

	private String convertTime(String timeString){
		try {
			SimpleDateFormat xmlFormat = new SimpleDateFormat("MMM-dd-yy HH:mm:ss");
			SimpleDateFormat dbFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			timeString = xmlFormat.format(dbFormat.parse(timeString));

		} catch(Exception ex){
			System.out.println(ex);
		}
		return timeString;
	}

	// end helper methods

	public String getXMLDataForItemId(String itemId) {

		// TODO: Your code here!
		String xmlString = "<Item ItemID=\"" + itemId + "\">" + "\n";
		String name = "";
		//String[] Categories;
		String currently = "";
		String buyPrice = "";
		String firstBid = "";
		String numberOfBids = "";
		// Bids

		// end Bids
		String location = "";
			String latitude = "";
			String longitude = "";
		String country = "";
		
		String started = "";
		String ends = "";

		// Seller
		String sellerID = "";
		String rating = "";
		// Description
		String description = "";

		try {

			conn = DbManager.getConnection(true);
	  		Statement stmt = conn.createStatement();
	  		ResultSet rs = stmt.executeQuery("SELECT * FROM Items " + "WHERE Items.ItemID = " + itemId);
	  		while (rs.next()) {
				sellerID = rs.getString("SellerID");

				name = rs.getString("Name");

				location = rs.getString("Location");
				if (rs.getFloat("Latitude") != 0.00f)
					latitude = "Latitude=\"" + Float.toString(rs.getFloat("Latitude")) + "\" ";
				if (rs.getFloat("Longitude") != 0.00f)
					longitude = "Longitude=\"" + Float.toString(rs.getFloat("Longitude")) + "\"";

				country = rs.getString("Country");

				currently = Float.toString(rs.getFloat("Currently"));

				firstBid = Float.toString(rs.getFloat("FirstBid"));

				buyPrice = String.format("%.2f", rs.getFloat("BuyPrice"));

				numberOfBids = Integer.toString(rs.getInt("NumberOfBids"));
				started = rs.getTimestamp("Started").toString();
				ends = rs.getTimestamp("Ends").toString();
				description = rs.getString("Description").toString();
			}
			// name
			xmlString += "	<Name>" + stringToXML(name) + "</Name>" + "\n";
			// categroies
			rs = stmt.executeQuery("SELECT * FROM ItemCategory WHERE ItemID = " + itemId);
			while (rs.next()) {
				xmlString += "	<Category>" + stringToXML(rs.getString("Category")) + "</Category>" +"\n";
			}
			// currently
			xmlString += "	<Currently>" + "$" + stringToXML(currently) + "</Currently>" + "\n";
			// buyPrice
			if(!buyPrice.equals("0.00"))
				xmlString += "	<Buy_Price>" + "$" + stringToXML(buyPrice) + "</Buy_Price>" + "\n";
			// firstBid
			xmlString += "	<First_Bid>" + "$" + stringToXML(firstBid) + "</First_Bid>" + "\n";
			// numberOfBid
			xmlString += "	<Number_of_Bids>" + stringToXML(numberOfBids) + "</Number_of_Bids>" + "\n";
			// bids
			xmlString += "	<Bids>" + "\n";
			rs = stmt.executeQuery("SELECT * FROM Bids WHERE ItemID=" + itemId);
			while(rs.next()){
				xmlString = bidXMLBuildHelper(xmlString, rs.getString("BidderID"), rs.getTimestamp("Time").toString(), Float.toString(rs.getFloat("Amount")));
			}
			xmlString += "	</Bids>" + "\n";
			// country and location
			xmlString += "	<Location " + latitude + longitude + ">" + stringToXML(location) + "</Location>" + "\n";
			xmlString += "	<Country>" + stringToXML(country) + "</Country>" + "\n";
			// started and ends
			xmlString += "	<Started>" + stringToXML(convertTime(started)) + "</Started>" + "\n";
			xmlString += "	<Ends>" + stringToXML(convertTime(ends)) + "</Ends>" + "\n";

			// seller
			rs = stmt.executeQuery("SELECT * FROM Sellers WHERE UserID ='" + sellerID + "'");
			while (rs.next()) {
				rating = Integer.toString(rs.getInt("Rating"));
			}
			xmlString += "	<Seller " + "UserID=\"" + stringToXML(sellerID) + "\" " + "Rating=\"" + stringToXML(rating) + "\"/>" + "\n";
			// description
			xmlString += "	<Description>" + stringToXML(description) + "</Description>" + "\n";
			// close item
			xmlString += "</Item>";

			// close database
	  		stmt.close();
    		rs.close();
	  		conn.close();
		} catch (SQLException ex){
			System.out.println(ex);
		}
		
		return xmlString;
	}
	
	public String echo(String message) {
		return message;
	}

}
