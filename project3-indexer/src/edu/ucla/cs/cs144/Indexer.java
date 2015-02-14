package edu.ucla.cs.cs144;

import java.io.IOException;
import java.io.StringReader;
import java.io.File;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

public class Indexer {
    
    /** Creates a new instance of Indexer */
    public Indexer() {
    }
 
    public void rebuildIndexes() {

        Connection conn = null;

        // create a connection to the database to retrieve Items from MySQL
        try {
            conn = DbManager.getConnection(true);
        } catch (SQLException ex) {
            System.out.println(ex);
        }

	/*
	 * Add your code here to retrieve Items using the connection
	 * and add corresponding entries to your Lucene inverted indexes.
         *
         * You will have to use JDBC API to retrieve MySQL data from Java.
         * Read our tutorial on JDBC if you do not know how to use JDBC.
         *
         * You will also have to use Lucene IndexWriter and Document
         * classes to create an index and populate it with Items data.
         * Read our tutorial on Lucene as well if you don't know how.
         *
         * As part of this development, you may want to add 
         * new methods and create additional Java classes. 
         * If you create new classes, make sure that
         * the classes become part of "edu.ucla.cs.cs144" package
         * and place your class source files at src/edu/ucla/cs/cs144/.
	 * 
	 */
        //IndexWriter indexWriter = new IndexWriter(FSDirectory.open(new File("index-directory")), new IndexWriterConfig(Version.LUCENE_4_10_2, new StandardAnalyzer()));
        //IndexWriter indexWriter = new IndexWriter(System.getenv("LUCENE_INDEX"), new StandardAnalyzer(), true);
        IndexWriter indexWriter = null;
        try {
            Directory indexDir = FSDirectory.open(new File("/var/lib/lucene/"));
            IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_4_10_2, new StandardAnalyzer());
            config.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
            indexWriter = new IndexWriter(indexDir, config);
        } catch (IOException ex){
            System.out.println("SQLException: " + ex.getMessage());
        }

        //retrieve itemID, name, category, description from db
        Statement stmt = null;
        ResultSet rs = null;
        try {
            stmt = conn.createStatement();
            rs = stmt.executeQuery("SELECT ItemID, Name, Description FROM Items");
        } catch (SQLException ex){
            System.out.println("SQLException: " + ex.getMessage());
        }
        try {
            while(rs.next()){
                //retrieve category from db
                ResultSet rs_cate = null;
                String selectSQL = "SELECT GROUP_CONCAT(Category SEPARATOR ' ') AS ItemCate FROM ItemCategory WHERE ItemID = ?";
                PreparedStatement prepareSelectCate = conn.prepareStatement(selectSQL);
                prepareSelectCate.setInt(1, rs.getInt("ItemID"));
                rs_cate = prepareSelectCate.executeQuery();
                rs_cate.next();

                Document doc = new Document();
                doc.add(new StringField("ItemID", rs.getInt("ItemID") + "", Field.Store.YES));
                doc.add(new StringField("Name", rs.getString("Name"), Field.Store.YES));
                doc.add(new StringField("Description", rs.getString("Description"), Field.Store.NO));
                doc.add(new StringField("Category", rs_cate.getString("ItemCate"), Field.Store.NO));
                String fullSearchableText = rs.getInt("ItemID") + " " + rs.getString("Name") + " " + rs.getString("Description") + " " + rs_cate.getString("ItemCate");
                doc.add(new TextField("content", fullSearchableText, Field.Store.NO));

                try { 
                    indexWriter.addDocument(doc);
                } catch (IOException ex){
                    System.out.println("IOException: " + ex.getMessage());
                }

                prepareSelectCate.close();
                rs_cate.close();
            }

        } catch (SQLException ex) {
            System.out.println("SQLException: " + ex.getMessage());
        }   

        try { 
            indexWriter.close();
        } catch (IOException ex){
            System.out.println("IOException: " + ex.getMessage());
        }

        // close the database connection
        try {
            stmt.close();
            rs.close();

            conn.close();
        } catch (SQLException ex) {
            System.out.println(ex);
        }

    }// end rebuildIndexes

    public static void main(String args[]) {
        Indexer idx = new Indexer();
        idx.rebuildIndexes();
    }   
}
