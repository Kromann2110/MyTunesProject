package dk.easv.demo;

import dk.easv.demo.GUI.Controller.MainController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainApp extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/dk/easv/demo/GUI/MainView.fxml"));
        Parent root = loader.load();

        // Get the controller to setup shutdown hook - FIXED: Use MainController instead of MainViewController
        MainController controller = loader.getController();
        primaryStage.setOnHidden(e -> controller.shutdown());

        primaryStage.setTitle("MyTunes Music Player");
        primaryStage.setScene(new Scene(root));
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}