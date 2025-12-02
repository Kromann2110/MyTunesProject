package dk.easv.demo.GUI.Model;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * Test database connection
 */
public class TestConnection {
    public static void main(String[] args) {
        String url = "jdbc:sqlserver://10.176.111.34:1433;databaseName=MyTunesDB;user=CS2025a_e_24;password=CS2025aE24#23;trustServerCertificate=true";

        System.out.println("Testing connection to MyTunesDB...");
        System.out.println("URL: " + url.replace("CS2025aE24#23", "******"));

        try {
            // Load JDBC driver
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            System.out.println("JDBC Driver loaded successfully");

            // Connect to database
            Connection conn = DriverManager.getConnection(url);
            System.out.println("SUCCESS: Connected to MyTunesDB!");

            // Test query
            Statement stmt = conn.createStatement();

            // Get table count
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) as table_count FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_TYPE='BASE TABLE'");
            if (rs.next()) {
                System.out.println("Total tables in database: " + rs.getInt("table_count"));
            }

            // List all tables
            rs = stmt.executeQuery("SELECT TABLE_NAME FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_TYPE='BASE TABLE' ORDER BY TABLE_NAME");
            System.out.println("\nTables found:");
            while (rs.next()) {
                System.out.println("  - " + rs.getString("TABLE_NAME"));
            }

            // Count songs
            rs = stmt.executeQuery("SELECT COUNT(*) as song_count FROM songs");
            if (rs.next()) {
                System.out.println("\nTotal songs: " + rs.getInt("song_count"));
            }

            // List songs
            rs = stmt.executeQuery("SELECT TOP 5 id, title, artist, duration FROM songs ORDER BY title");
            System.out.println("\nSample songs:");
            while (rs.next()) {
                System.out.println(String.format("  %d. %s - %s (%d seconds)",
                        rs.getInt("id"),
                        rs.getString("title"),
                        rs.getString("artist"),
                        rs.getInt("duration")));
            }

            // Close connection
            conn.close();
            System.out.println("\nTest completed successfully!");

        } catch (ClassNotFoundException e) {
            System.out.println("ERROR: SQL Server JDBC Driver not found!");
            System.out.println("Make sure you have this dependency in pom.xml:");
            System.out.println("<dependency>");
            System.out.println("    <groupId>com.microsoft.sqlserver</groupId>");
            System.out.println("    <artifactId>mssql-jdbc</artifactId>");
            System.out.println("    <version>9.4.1.jre11</version>");
            System.out.println("</dependency>");
        } catch (Exception e) {
            System.out.println("ERROR: " + e.getMessage());
            e.printStackTrace();

            // Provide helpful suggestions
            if (e.getMessage().contains("The server principal")) {
                System.out.println("\n=== SOLUTION ===");
                System.out.println("Your login needs to be mapped to MyTunesDB.");
                System.out.println("Run this SQL in DataGrip:");
                System.out.println("USE MyTunesDB;");
                System.out.println("CREATE USER CS2025a_e_24 FOR LOGIN CS2025a_e_24;");
                System.out.println("ALTER ROLE db_owner ADD MEMBER CS2025a_e_24;");
            }
        }
    }
}