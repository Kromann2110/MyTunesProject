package dk.easv.demo.DAL.db;

// Java standard
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
/**
 * Manages database connections and configuration
 */
public class DBConnector {
    private static final String PROP_FILE = "src/main/resources/database.properties";
    private static String url;
    private static String user;
    private static String password;

    // Load config when class first used
    static {
        try {
            initializeConnection();
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Failed to initialize database connection: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Read database config from properties file
    private static void initializeConnection() throws IOException, ClassNotFoundException {
        Properties databaseProperties = new Properties();

        File propFile = new File(PROP_FILE);
        if (!propFile.exists()) {
            throw new IOException("Database properties file not found: " + PROP_FILE);
        }

        databaseProperties.load(Files.newInputStream(propFile.toPath()));

        String server = databaseProperties.getProperty("Server");
        String database = databaseProperties.getProperty("Database");
        user = databaseProperties.getProperty("User");
        password = databaseProperties.getProperty("Password");

        if (server == null || database == null || user == null || password == null) {
            throw new IOException("Missing required database properties in " + PROP_FILE);
        }

        // Build SQL Server connection URL
        url = String.format("jdbc:sqlserver://%s:1433;databaseName=%s;encrypt=true;trustServerCertificate=true;loginTimeout=5",
                server, database);

        // Load JDBC driver
        Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");

        System.out.println("Database configuration loaded successfully");

        // Test connection on startup
        if (testConnection()) {
            System.out.println("Database connection test: SUCCESS");
        } else {
            System.out.println("Database connection test: FAILED");
        }
    }

    // Get database connection
    public static Connection getConnection() throws SQLException {
        if (url == null) {
            throw new SQLException("Database not initialized. Check database configuration.");
        }

        System.out.println("Attempting to connect to database...");
        Connection conn = DriverManager.getConnection(url, user, password);
        System.out.println("Database connection established successfully");
        return conn;
    }

    // Test if database is accessible
    public static boolean testConnection() {
        try (Connection conn = getConnection()) {
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            System.err.println("Connection test failed: " + e.getMessage());
            return false;
        }
    }
}