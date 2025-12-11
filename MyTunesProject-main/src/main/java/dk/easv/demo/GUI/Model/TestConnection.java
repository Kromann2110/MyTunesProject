package dk.easv.demo.GUI.Model;

// Data access
import dk.easv.demo.DAL.db.DBConnector;

// Java standard
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
/**
 * Utility to test database connectivity
 * Run this to verify database configuration
 */
public class TestConnection {

    // Test database connection
    public static void main(String[] args) {
        System.out.println("=== TESTING CONFIGURATION-BASED CONNECTION ===");

        try {
            Connection conn = DBConnector.getConnection();
            System.out.println("SUCCESS: Connected to database!");

            // Test query
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) as count FROM songs");
            if (rs.next()) {
                System.out.println("Songs in database: " + rs.getInt("count"));
            }

            conn.close();
            System.out.println("=== TEST COMPLETE ===");

        } catch (Exception e) {
            System.out.println("FAILED: " + e.getMessage());
            e.printStackTrace();
        }
    }
}