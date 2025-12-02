package dk.easv.demo;

// Java standard
import java.net.URL;

/**
 * Debugging utility for finding FXML files
 * Helps troubleshoot FXML loading issues
 */
public class TestFXMLFinder {

    // Test different FXML file paths
    public static void main(String[] args) {
        System.out.println("=== Looking for FXML files ===");

        // Try common FXML paths
        String[] possiblePaths = {
                "/dk/easv/demo/GUI/MainView.fxml",
                "dk/easv/demo/GUI/MainView.fxml",
                "/GUI/MainView.fxml",
                "dk/easv/demo/GUI/MainView.fxml",
                "/dk/easv/demo/GUI/MainView.fxml"
        };

        for (String path : possiblePaths) {
            URL url = TestFXMLFinder.class.getResource(path);
            System.out.println("Path: '" + path + "' -> " + (url != null ? "FOUND" : "NOT FOUND"));
            if (url != null) {
                System.out.println("   Full URL: " + url);
            }
        }

        // Show classpath for debugging
        System.out.println("\n=== Classpath Info ===");
        String classpath = System.getProperty("java.class.path");
        System.out.println("Classpath: " + classpath);
    }
}