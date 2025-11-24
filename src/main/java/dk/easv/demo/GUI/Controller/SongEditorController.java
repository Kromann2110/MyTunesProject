package dk.easv.demo.GUI.Controller;

// Business entities
import dk.easv.demo.BE.Song;

// Business logic
import dk.easv.demo.BLL.SongManager;

// Java standard
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;
/**
 * Controller for creating/editing songs
 */
public class SongEditorController implements Initializable {

    @FXML private TextField titleField;
    @FXML private TextField artistField;
    @FXML private TextField genreField;
    @FXML private TextField durationField;
    @FXML private TextField filePathField;

    private SongManager songManager;
    private Song currentSong;
    private boolean isEditMode = false;

    // Initialize controller
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            songManager = new SongManager();
        } catch (Exception e) {
            showErrorDialog("Error initializing song editor: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Load song for editing
    public void setSong(Song song) {
        this.currentSong = song;
        this.isEditMode = true;

        if (song != null) {
            titleField.setText(song.getTitle());
            artistField.setText(song.getArtist());
            genreField.setText(song.getCategory());
            durationField.setText(formatDurationForDisplay(song.getDuration()));
            filePathField.setText(song.getFilePath());
        }
    }

    // Browse for music file
    @FXML
    private void browseFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Music File");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Audio Files", "*.mp3", "*.wav", "*.aac", "*.ogg"),
                new FileChooser.ExtensionFilter("All Files", "*.*")
        );

        File selectedFile = fileChooser.showOpenDialog(filePathField.getScene().getWindow());
        if (selectedFile != null) {
            filePathField.setText(selectedFile.getAbsolutePath());
        }
    }

    // Save song changes
    @FXML
    private void saveSong() {
        try {
            String title = titleField.getText().trim();
            String artist = artistField.getText().trim();
            String genre = genreField.getText().trim();
            String durationText = durationField.getText().trim();
            String filePath = filePathField.getText().trim();

            if (title.isEmpty() || artist.isEmpty() || durationText.isEmpty() || filePath.isEmpty()) {
                showErrorDialog("Please fill in all required fields");
                return;
            }

            // Parse duration from text to seconds
            int duration = parseDurationToSeconds(durationText);
            if (duration <= 0) {
                showErrorDialog("Please enter a valid duration in format MM:SS or HH:MM:SS");
                return;
            }

            if (isEditMode && currentSong != null) {
                // Update existing song
                currentSong.setTitle(title);
                currentSong.setArtist(artist);
                currentSong.setCategory(genre);
                currentSong.setDuration(duration);
                currentSong.setFilePath(filePath);

                songManager.updateSong(currentSong);
                showInfoDialog("Success", "Song '" + title + "' updated successfully");
            } else {
                // Create new song
                songManager.createSong(title, artist, genre, duration, filePath);
                showInfoDialog("Success", "Song '" + title + "' created successfully");
            }

            closeWindow();

        } catch (Exception e) {
            showErrorDialog("Error saving song: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Cancel editing
    @FXML
    private void cancel() {
        closeWindow();
    }

    // Close editor window
    private void closeWindow() {
        Stage stage = (Stage) titleField.getScene().getWindow();
        stage.close();
    }

    // Parse duration string to seconds
    private int parseDurationToSeconds(String duration) {
        if (duration == null || duration.isEmpty()) {
            return 0;
        }

        try {
            String[] parts = duration.split(":");
            if (parts.length == 2) {
                // MM:SS format
                int minutes = Integer.parseInt(parts[0]);
                int seconds = Integer.parseInt(parts[1]);
                return minutes * 60 + seconds;
            } else if (parts.length == 3) {
                // HH:MM:SS format
                int hours = Integer.parseInt(parts[0]);
                int minutes = Integer.parseInt(parts[1]);
                int seconds = Integer.parseInt(parts[2]);
                return hours * 3600 + minutes * 60 + seconds;
            } else if (parts.length == 1) {
                // Just seconds
                return Integer.parseInt(parts[0]);
            }
        } catch (NumberFormatException e) {
            showErrorDialog("Invalid duration format. Please use MM:SS or HH:MM:SS");
        }
        return 0;
    }

    // Format seconds to display format
    private String formatDurationForDisplay(int seconds) {
        if (seconds <= 0) {
            return "0:00";
        }

        int minutes = seconds / 60;
        int remainingSeconds = seconds % 60;

        return String.format("%d:%02d", minutes, remainingSeconds);
    }

    // Show error dialog
    private void showErrorDialog(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Show info dialog
    private void showInfoDialog(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}