package dk.easv.demo.DAL.db;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DBConnector {
    private static String url;
    private static String user;
    private static String password;

    static {
        try {
            initializeConnection();
        } catch (Exception e) {
            System.err.println("Failed to initialize database connection: " + e.getMessage());
            e.printStackTrace();
            // Try to create a mock connection for testing
            createMockConnection();
        }
    }

    private static void initializeConnection() throws IOException, ClassNotFoundException, SQLException {
        Properties props = new Properties();

        // Try multiple ways to load properties
        File propFile = null;
        String[] locations = {
                "database.properties",
                "src/main/resources/database.properties",
                "../database.properties",
                System.getProperty("user.dir") + "/database.properties"
        };

        for (String location : locations) {
            propFile = new File(location);
            if (propFile.exists()) {
                System.out.println("Found properties at: " + propFile.getAbsolutePath());
                break;
            }
            propFile = null;
        }

        if (propFile == null) {
            // Try classpath
            try (var input = DBConnector.class.getClassLoader().getResourceAsStream("database.properties")) {
                if (input != null) {
                    System.out.println("Loading from classpath");
                    props.load(input);
                } else {
                    throw new IOException("database.properties not found in any location");
                }
            }
        } else {
            props.load(Files.newInputStream(propFile.toPath()));
        }

        String server = props.getProperty("Server", ".");
        String database = props.getProperty("Database", "MyTunesDB");
        user = props.getProperty("User", "");
        password = props.getProperty("Password", "");
        String integrated = props.getProperty("integratedSecurity", "false");

        // Build connection string
        StringBuilder urlBuilder = new StringBuilder();
        urlBuilder.append("jdbc:sqlserver://").append(server).append(":1433");
        urlBuilder.append(";databaseName=").append(database);
        urlBuilder.append(";trustServerCertificate=true");

        if ("true".equalsIgnoreCase(integrated) || (user.isEmpty() && password.isEmpty())) {
            urlBuilder.append(";integratedSecurity=true");
        } else {
            url = urlBuilder.toString();
            // We'll add user/password in getConnection()
        }

        url = urlBuilder.toString();

        Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
        System.out.println("Database URL: " + url);

        // Test connection
        try (Connection conn = getConnection()) {
            System.out.println("✓ Database connection successful!");
        }
    }

    private static void createMockConnection() {
        System.out.println("Creating mock connection for testing...");
        url = "jdbc:sqlserver://localhost:1433;databaseName=MyTunesDB;trustServerCertificate=true";
        user = "";
        password = "";
    }

    public static Connection getConnection() throws SQLException {
        if (url == null) {
            throw new SQLException("Database not initialized");
        }

        if (user != null && !user.isEmpty()) {
            return DriverManager.getConnection(url, user, password);
        } else {
            return DriverManager.getConnection(url);
        }
    }
}