package edu.ucla.cs.cs144;

import java.util.Calendar;
import java.util.Date;

import edu.ucla.cs.cs144.AuctionSearch;
import edu.ucla.cs.cs144.SearchRegion;
import edu.ucla.cs.cs144.SearchResult;

public class AuctionSearchTest {
	public static void main(String[] args1)
	{
		AuctionSearch as = new AuctionSearch();

		String message = "Test message";
		String reply = as.echo(message);
		System.out.println("Reply: " + reply);
		
		//String query = "superman";
		String query = "camera";
		SearchResult[] basicResults = as.basicSearch(query, 0, 20);
		System.out.println("Basic Seacrh Query: " + query);
		System.out.println("Received " + basicResults.length + " results");
		for(SearchResult result : basicResults) {
			if ( result == null) {
				System.out.println("null result found");
			} else
				System.out.println(result.getItemId() + ": " + result.getName());
		}
		// verify
		String query1 = "superman";
		SearchResult[] basicResults1 = as.basicSearch(query1, 0, 100);
		System.out.println("Basic Seacrh Query: " + query1);
		System.out.println("Received " + basicResults1.length + " results");

		String query2 = "kitchenware";
		SearchResult[] basicResults2 = as.basicSearch(query2, 0, 1500);
		System.out.println("Basic Seacrh Query: " + query2);
		System.out.println("Received " + basicResults2.length + " results");

		String query3 = "star trek";
		SearchResult[] basicResults3 = as.basicSearch(query3, 0, 1000);
		System.out.println("Basic Seacrh Query: " + query3);
		System.out.println("Received " + basicResults3.length + " results");

		
		SearchRegion region = new SearchRegion(33.774, -118.63, 34.201, -117.38); 
		SearchResult[] spatialResults = as.spatialSearch("camera", region, 0, 20);
		System.out.println("Spatial Seacrh");
		System.out.println("Received " + spatialResults.length + " results");
		for(SearchResult result : spatialResults) {
			if ( result == null) {
				System.out.println("null result found");
			} else
				System.out.println(result.getItemId() + ": " + result.getName());
		}
		
		String itemId = "1497595357";
		String item = as.getXMLDataForItemId(itemId);
		System.out.println("XML data for ItemId: " + itemId);
		System.out.println(item);

		// Add your own test here
		String itemId2 = "1043374545";
		String item2 = as.getXMLDataForItemId(itemId2);
		System.out.println("XML data for ItemId: " + itemId2);
		//System.out.println(item2);
		
	}
}
