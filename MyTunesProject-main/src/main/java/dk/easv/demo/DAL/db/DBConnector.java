    package dk.easv.demo.DAL.db;

    import java.io.File;
    import java.io.IOException;
    import java.io.InputStream;
    import java.nio.file.Files;
    import java.sql.Connection;
    import java.sql.DriverManager;
    import java.sql.SQLException;
    import java.util.Properties;

    /**
     * Manages database connections and configuration
     */
    public class DBConnector {
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
            System.out.println("=== DBConnector Debug ===");
            System.out.println("Current directory: " + new File(".").getAbsolutePath());

            Properties databaseProperties = new Properties();

            // Try multiple locations
            String[] possiblePaths = {
                    "src/main/resources/database.properties",
                    "database.properties",
                    "target/classes/database.properties",
                    System.getProperty("user.dir") + "/src/main/resources/database.properties",
                    System.getProperty("user.dir") + "/database.properties"
            };

            File propFile = null;

            for (String path : possiblePaths) {
                File testFile = new File(path);
                System.out.println("Checking: " + path);
                System.out.println("  Full path: " + testFile.getAbsolutePath());
                System.out.println("  Exists: " + testFile.exists());

                if (testFile.exists()) {
                    propFile = testFile;
                    System.out.println("✓ Found at: " + propFile.getAbsolutePath());
                    break;
                }
            }

            if (propFile == null) {
                // Try classpath
                System.out.println("Trying classpath...");
                InputStream input = DBConnector.class.getClassLoader().getResourceAsStream("database.properties");
                if (input != null) {
                    System.out.println("✓ Found in classpath!");
                    databaseProperties.load(input);
                    input.close();
                } else {
                    System.err.println("✗ database.properties not found anywhere!");
                    throw new IOException("database.properties not found. Tried:\n" +
                            "- src/main/resources/database.properties\n" +
                            "- database.properties\n" +
                            "- target/classes/database.properties\n" +
                            "- classpath:/database.properties");
                }
            } else {
                databaseProperties.load(Files.newInputStream(propFile.toPath()));
            }

            String server = databaseProperties.getProperty("Server");
            String database = databaseProperties.getProperty("Database");
            user = databaseProperties.getProperty("User");
            password = databaseProperties.getProperty("Password");

            if (server == null || database == null || user == null || password == null) {
                throw new IOException("Missing required database properties");
            }

            // Build SQL Server connection URL WITH USER AND PASSWORD
            url = String.format("jdbc:sqlserver://%s:1433;databaseName=%s;user=%s;password=%s;encrypt=true;trustServerCertificate=true;loginTimeout=5",
                    server, database, user, password);

            // Load JDBC driver
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");

            System.out.println("Database configuration loaded successfully");
            System.out.println("Server: " + server);
            System.out.println("Database: " + database);
            System.out.println("User: " + user);
            System.out.println("URL: " + url.replace(password, "******"));

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
            // Use the URL that already contains user/password
            Connection conn = DriverManager.getConnection(url);
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