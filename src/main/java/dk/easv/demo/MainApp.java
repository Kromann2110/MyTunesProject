package dk.easv.demo;

// Java standard
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
/**
 * Main JavaFX application class
 * Entry point for MyTunes Music Player
 */
public class MainApp extends Application {

    // Start JavaFX application
    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/dk/easv/demo/GUI/MainView.fxml"));
        Parent root = loader.load();

        primaryStage.setTitle("MyTunes Music Player");
        primaryStage.setScene(new Scene(root));
        primaryStage.show();
    }

    // Main method
    public static void main(String[] args) {
        launch(args);
    }
}