package dk.easv.demo.GUI.Controller;

// Business entities
import dk.easv.demo.BE.Playlist;
import dk.easv.demo.BE.Song;

// Business logic
import dk.easv.demo.BLL.SongManager;
import dk.easv.demo.BLL.PlaylistManager;

// Java standard
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
/**
 * Alternative main view using ListView instead of TableView
 * Simpler version for basic functionality
 */
public class MainViewController implements Initializable {

    @FXML private ListView<Song> songsListView;
    @FXML private ListView<Playlist> playlistsListView;
    @FXML private Button playButton, pauseButton, stopButton;
    @FXML private Slider volumeSlider, progressSlider;
    @FXML private Label currentTimeLabel, totalTimeLabel, nowPlayingLabel;

    private MediaPlayer mediaPlayer;
    private SongManager songManager;
    private PlaylistManager playlistManager;

    // Initialize controller
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            songManager = new SongManager();
            playlistManager = new PlaylistManager();

            setupMediaPlayer();
            setupEventHandlers();
            loadDataFromDatabase();

        } catch (Exception e) {
            showErrorDialog("Error initializing application: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Setup volume control
    private void setupMediaPlayer() {
        volumeSlider.setValue(50);
        volumeSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (mediaPlayer != null) {
                mediaPlayer.setVolume(newValue.doubleValue() / 100.0);
            }
        });
    }

    // Setup click handlers
    private void setupEventHandlers() {
        // Double click to play song
        songsListView.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                playSelectedSong();
            }
        });

        // Click to show playlist songs
        playlistsListView.setOnMouseClicked(event -> {
            if (event.getClickCount() == 1) {
                showPlaylistSongs();
            }
        });
    }

    // Load data from database
    private void loadDataFromDatabase() {
        try {
            List<Song> songs = songManager.getAllSongs();
            songsListView.getItems().setAll(songs);

            List<Playlist> playlists = playlistManager.getAllPlaylists();
            playlistsListView.getItems().setAll(playlists);

        } catch (Exception e) {
            showErrorDialog("Error loading data from database: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Play selected song
    private void playSelectedSong() {
        Song selectedSong = songsListView.getSelectionModel().getSelectedItem();
        if (selectedSong != null) {
            playSong(selectedSong);
        }
    }

    // Play song with basic file handling
    private void playSong(Song song) {
        try {
            // Stop current playback
            if (mediaPlayer != null) {
                mediaPlayer.stop();
                mediaPlayer.dispose();
            }

            String filePath = song.getFilePath();
            Media media = new Media("file:///" + filePath.replace("\\", "/"));
            mediaPlayer = new MediaPlayer(media);

            // Setup media events
            mediaPlayer.setOnReady(() -> {
                nowPlayingLabel.setText("Now Playing: " + song.getTitle() + " - " + song.getArtist());
                setupProgressTracking();
                playMusic();
            });

            mediaPlayer.setOnError(() -> {
                System.out.println("Media error: " + mediaPlayer.getError());
                showErrorDialog("Could not play: " + song.getTitle() + ". Error: " + mediaPlayer.getError().getMessage());
            });

        } catch (Exception e) {
            showErrorDialog("Error playing song: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Setup progress tracking
    private void setupProgressTracking() {
        // Update progress as song plays
        mediaPlayer.currentTimeProperty().addListener((observable, oldValue, newValue) -> {
            if (!progressSlider.isValueChanging()) {
                progressSlider.setValue(newValue.toSeconds());
            }
            currentTimeLabel.setText(formatTime(newValue));
        });

        // Set total duration
        mediaPlayer.setOnReady(() -> {
            Duration totalDuration = mediaPlayer.getMedia().getDuration();
            progressSlider.setMax(totalDuration.toSeconds());
            totalTimeLabel.setText(formatTime(totalDuration));
        });

        // Allow seeking by clicking progress bar
        progressSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (progressSlider.isValueChanging() && mediaPlayer != null) {
                mediaPlayer.seek(Duration.seconds(newValue.doubleValue()));
            }
        });
    }

    // Format time for display
    private String formatTime(Duration duration) {
        int minutes = (int) duration.toMinutes();
        int seconds = (int) duration.toSeconds() % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }

    // Play button handler
    @FXML
    private void playMusic() {
        if (mediaPlayer != null) {
            mediaPlayer.play();
            playButton.setDisable(true);
            pauseButton.setDisable(false);
        }
    }

    // Pause button handler
    @FXML
    private void pauseMusic() {
        if (mediaPlayer != null) {
            mediaPlayer.pause();
            playButton.setDisable(false);
            pauseButton.setDisable(true);
        }
    }

    // Stop button handler
    @FXML
    private void stopMusic() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            playButton.setDisable(false);
            pauseButton.setDisable(true);
            progressSlider.setValue(0);
            currentTimeLabel.setText("00:00");
        }
    }

    // Create playlist with text input
    @FXML
    private void createNewPlaylist() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("New Playlist");
        dialog.setHeaderText("Create a new playlist");
        dialog.setContentText("Playlist name:");

        dialog.showAndWait().ifPresent(playlistName -> {
            if (!playlistName.trim().isEmpty()) {
                try {
                    Playlist newPlaylist = playlistManager.createPlaylist(playlistName);
                    playlistsListView.getItems().add(newPlaylist);
                    showInfoDialog("Success", "Playlist '" + playlistName + "' created successfully");
                } catch (Exception e) {
                    showErrorDialog("Error creating playlist: " + e.getMessage());
                }
            }
        });
    }

    // Add song to playlist
    @FXML
    private void addSongToPlaylist() {
        Song selectedSong = songsListView.getSelectionModel().getSelectedItem();
        Playlist selectedPlaylist = playlistsListView.getSelectionModel().getSelectedItem();

        if (selectedSong == null || selectedPlaylist == null) {
            showErrorDialog("Please select both a song and a playlist");
            return;
        }

        try {
            playlistManager.addSongToPlaylist(selectedPlaylist, selectedSong);
            showInfoDialog("Success", "Added '" + selectedSong.getTitle() + "' to '" + selectedPlaylist.getName() + "'");
        } catch (Exception e) {
            showErrorDialog("Error adding song to playlist: " + e.getMessage());
        }
    }

    // Show playlist contents in dialog
    private void showPlaylistSongs() {
        Playlist selectedPlaylist = playlistsListView.getSelectionModel().getSelectedItem();
        if (selectedPlaylist != null) {
            try {
                List<Song> playlistSongs = playlistManager.getSongsInPlaylist(selectedPlaylist);
                StringBuilder message = new StringBuilder("Songs in '" + selectedPlaylist.getName() + "':\n\n");

                for (Song song : playlistSongs) {
                    message.append("• ").append(song.getTitle()).append(" - ").append(song.getArtist()).append("\n");
                }

                showInfoDialog("Playlist Contents", message.toString());
            } catch (Exception e) {
                showErrorDialog("Error loading playlist songs: " + e.getMessage());
            }
        }
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

    // Cleanup on shutdown
    public void shutdown() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.dispose();
        }
    }
}