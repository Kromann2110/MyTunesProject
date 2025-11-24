package dk.easv.demo.GUI.Controller;

// Business entities
import dk.easv.demo.BE.Playlist;
import dk.easv.demo.BE.Song;

// Business logic
import dk.easv.demo.BLL.SongManager;
import dk.easv.demo.BLL.PlaylistManager;

// Java standard
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;
import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
/**
 * Main application controller - handles UI interactions and media playback
 */
public class MainController implements Initializable {

    @FXML private TableView<Song> songsTableView;
    @FXML private TableView<Playlist> playlistsTableView;
    @FXML private ListView<Song> playlistSongsListView;
    @FXML private TextField filterField;
    @FXML private Button filterButton;
    @FXML private Button clearFilterButton;
    @FXML private Label nowPlayingLabel;
    @FXML private Button playButton, pauseButton, stopButton;
    @FXML private Slider volumeSlider;
    @FXML private Slider progressSlider;
    @FXML private Label elapsedTimeLabel;
    @FXML private Label totalTimeLabel;

    private ObservableList<Song> allSongs;
    private ObservableList<Playlist> allPlaylists;
    private ObservableList<Song> currentPlaylistSongs;
    private FilteredList<Song> filteredSongs;

    private MediaPlayer mediaPlayer;
    private SongManager songManager;
    private PlaylistManager playlistManager;
    private Playlist selectedPlaylist;
    private boolean isSeeking = false;

    // Initialize controller and load data
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            songManager = new SongManager();
            playlistManager = new PlaylistManager();

            allSongs = FXCollections.observableArrayList();
            allPlaylists = FXCollections.observableArrayList();
            currentPlaylistSongs = FXCollections.observableArrayList();

            setupMediaPlayer();
            setupTableViews();
            setupEventHandlers();
            loadDataFromDatabase();

            pauseButton.setDisable(true);
            elapsedTimeLabel.setText("0:00");
            totalTimeLabel.setText("0:00");
            progressSlider.setValue(0);

        } catch (Exception e) {
            showErrorDialog("Error initializing application: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Setup volume and progress controls
    private void setupMediaPlayer() {
        volumeSlider.setValue(50);
        volumeSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (mediaPlayer != null) {
                mediaPlayer.setVolume(newValue.doubleValue() / 100.0);
            }
        });

        progressSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (isSeeking && mediaPlayer != null) {
                double seekTime = newValue.doubleValue() / 100.0 * mediaPlayer.getTotalDuration().toSeconds();
                mediaPlayer.seek(Duration.seconds(seekTime));
            }
        });
    }

    // Setup table views with sorting and filtering
    private void setupTableViews() {
        filteredSongs = new FilteredList<>(allSongs, p -> true);
        SortedList<Song> sortedSongs = new SortedList<>(filteredSongs);
        sortedSongs.comparatorProperty().bind(songsTableView.comparatorProperty());
        songsTableView.setItems(sortedSongs);

        playlistsTableView.setItems(allPlaylists);
        playlistSongsListView.setItems(currentPlaylistSongs);
    }

    // Setup event handlers for user interactions
    private void setupEventHandlers() {
        // Load songs when playlist selected
        playlistsTableView.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> {
                    selectedPlaylist = newValue;
                    loadPlaylistSongs(newValue);
                });

        // Double click to play song from songs table
        songsTableView.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                playSelectedSong(songsTableView.getSelectionModel().getSelectedItem());
            }
        });

        // Double click to play song from playlist
        playlistSongsListView.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                playSelectedSong(playlistSongsListView.getSelectionModel().getSelectedItem());
            }
        });

        // Enable seeking when slider pressed
        progressSlider.setOnMousePressed(event -> {
            if (mediaPlayer != null) {
                isSeeking = true;
            }
        });

        // Disable seeking when slider released
        progressSlider.setOnMouseReleased(event -> {
            if (mediaPlayer != null) {
                isSeeking = false;
            }
        });
    }

    // Load songs and playlists from database
    private void loadDataFromDatabase() {
        try {
            List<Song> songs = songManager.getAllSongs();
            allSongs.setAll(songs);

            List<Playlist> playlists = playlistManager.getAllPlaylists();

            // Calculate duration and count for each playlist
            for (Playlist playlist : playlists) {
                String totalDuration = calculatePlaylistTotalDuration(playlist);
                playlist.setTotalDuration(totalDuration);

                List<Song> playlistSongs = playlistManager.getSongsInPlaylist(playlist);
                playlist.setSongCount(playlistSongs.size());
            }

            allPlaylists.setAll(playlists);

        } catch (Exception e) {
            showErrorDialog("Error loading data from database: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Calculate total duration of playlist
    private String calculatePlaylistTotalDuration(Playlist playlist) {
        try {
            List<Song> songs = playlistManager.getSongsInPlaylist(playlist);
            if (songs == null || songs.isEmpty()) {
                return "00:00";
            }

            int totalSeconds = 0;
            for (Song song : songs) {
                totalSeconds += song.getDuration();
            }

            return formatSecondsToDuration(totalSeconds);
        } catch (Exception e) {
            e.printStackTrace();
            return "00:00";
        }
    }

    // Format seconds to readable time
    private String formatSecondsToDuration(int totalSeconds) {
        if (totalSeconds <= 0) {
            return "00:00";
        }

        int hours = totalSeconds / 3600;
        int minutes = (totalSeconds % 3600) / 60;
        int seconds = totalSeconds % 60;

        if (hours > 0) {
            return String.format("%d:%02d:%02d", hours, minutes, seconds);
        } else {
            return String.format("%d:%02d", minutes, seconds);
        }
    }

    // Load songs for selected playlist
    private void loadPlaylistSongs(Playlist playlist) {
        if (playlist == null) {
            currentPlaylistSongs.clear();
            return;
        }

        try {
            List<Song> songs = playlistManager.getSongsInPlaylist(playlist);
            currentPlaylistSongs.setAll(songs);
        } catch (Exception e) {
            showErrorDialog("Error loading playlist songs: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Refresh playlist display with updated info
    private void refreshPlaylistDisplay() {
        if (selectedPlaylist != null) {
            String totalDuration = calculatePlaylistTotalDuration(selectedPlaylist);
            selectedPlaylist.setTotalDuration(totalDuration);

            List<Song> playlistSongs = playlistManager.getSongsInPlaylist(selectedPlaylist);
            selectedPlaylist.setSongCount(playlistSongs.size());

            playlistsTableView.refresh();
        }
    }

    // Play selected song
    private void playSelectedSong(Song song) {
        if (song != null) {
            playSong(song);
        }
    }

    // Find music file in various locations
    private String findMusicFile(Song song) {
        String originalPath = song.getFilePath();
        System.out.println("Looking for file: " + originalPath);

        String fileName = new File(originalPath).getName();
        System.out.println("Filename: " + fileName);

        // Search in common locations
        String[] possibleLocations = {
                originalPath,
                "music/" + fileName,
                "../music/" + fileName,
                "./music/" + fileName,
                System.getProperty("user.home") + "/MyTunesMusic/" + fileName,
                System.getProperty("user.home") + "/Music/" + fileName,
                System.getProperty("user.dir") + "/music/" + fileName,
                System.getProperty("user.dir") + "/MyTunesMusic/" + fileName,
                fileName
        };

        for (String location : possibleLocations) {
            File file = new File(location);
            System.out.println("Trying: " + location + " -> " + (file.exists() ? "FOUND" : "NOT FOUND"));
            if (file.exists() && file.isFile()) {
                System.out.println("Found file at: " + location);
                return file.toURI().toString();
            }
        }

        System.out.println("Could not find file: " + fileName);
        return null;
    }

    // Play song with media player
    private void playSong(Song song) {
        try {
            // Stop previous playback
            if (mediaPlayer != null) {
                try {
                    mediaPlayer.stop();
                } catch (Exception e) {
                    System.out.println("Warning stopping previous player: " + e.getMessage());
                }
                mediaPlayer.dispose();
                mediaPlayer = null;
            }

            String mediaUri = findMusicFile(song);

            if (mediaUri == null) {
                showErrorDialog("Music file not found: " + new File(song.getFilePath()).getName() +
                        "\n\nPlease make sure the music files are in one of these locations:\n" +
                        "- 'music' folder next to the application\n" +
                        "- Your home directory/MyTunesMusic/\n" +
                        "- Same folder as the application");
                return;
            }

            System.out.println("Playing from: " + mediaUri);

            Media media = new Media(mediaUri);
            mediaPlayer = new MediaPlayer(media);

            mediaPlayer.setVolume(volumeSlider.getValue() / 100.0);

            // Setup media player events
            mediaPlayer.setOnReady(() -> {
                nowPlayingLabel.setText("Now Playing: " + song.getTitle() + " - " + song.getArtist());
                setupProgressTracking();
                playMusic();
            });

            mediaPlayer.setOnError(() -> {
                System.out.println("Media error: " + mediaPlayer.getError());
                showErrorDialog("Could not play: " + song.getTitle() +
                        ". Error: " + (mediaPlayer.getError() != null ? mediaPlayer.getError().getMessage() : "Unknown error"));
            });

            mediaPlayer.setOnEndOfMedia(() -> {
                System.out.println("Song finished playing");
                stopMusic();
            });

        } catch (Exception e) {
            showErrorDialog("Error playing song: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Setup progress tracking for current song
    private void setupProgressTracking() {
        if (mediaPlayer == null) return;

        progressSlider.setValue(0);
        elapsedTimeLabel.setText("0:00");
        isSeeking = false;

        // Set total duration when media is ready
        mediaPlayer.setOnReady(() -> {
            try {
                Duration totalDuration = mediaPlayer.getTotalDuration();
                if (totalDuration != null && !totalDuration.isUnknown()) {
                    totalTimeLabel.setText(formatTimeSimple(totalDuration));
                } else {
                    totalTimeLabel.setText("0:00");
                }
            } catch (Exception e) {
                System.out.println("Error setting total duration: " + e.getMessage());
                totalTimeLabel.setText("0:00");
            }
        });

        // Update progress as song plays
        mediaPlayer.currentTimeProperty().addListener((observable, oldValue, newValue) -> {
            try {
                if (!isSeeking && mediaPlayer != null) {
                    Duration totalDuration = mediaPlayer.getTotalDuration();
                    if (totalDuration != null && totalDuration.greaterThan(Duration.ZERO) && !totalDuration.isUnknown()) {
                        double progress = newValue.toSeconds() / totalDuration.toSeconds();
                        progressSlider.setValue(progress * 100);
                        elapsedTimeLabel.setText(formatTimeSimple(newValue));
                        totalTimeLabel.setText(formatTimeSimple(totalDuration));
                    } else {
                        elapsedTimeLabel.setText(formatTimeSimple(newValue));
                    }
                }
            } catch (Exception e) {
                System.out.println("Error updating progress: " + e.getMessage());
            }
        });
    }

    // Format time for display
    private String formatTimeSimple(Duration duration) {
        if (duration == null || duration.isUnknown()) {
            return "0:00";
        }

        int totalSeconds = (int) Math.floor(duration.toSeconds());
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;

        return String.format("%d:%02d", minutes, seconds);
    }

    // Play button handler
    @FXML
    private void playMusic() {
        if (mediaPlayer != null) {
            try {
                mediaPlayer.play();
                playButton.setDisable(true);
                pauseButton.setDisable(false);
            } catch (Exception e) {
                showErrorDialog("Error playing music: " + e.getMessage());
            }
        } else {
            showErrorDialog("No song selected to play");
        }
    }

    // Pause button handler
    @FXML
    private void pauseMusic() {
        if (mediaPlayer != null) {
            try {
                mediaPlayer.pause();
                playButton.setDisable(false);
                pauseButton.setDisable(true);
            } catch (Exception e) {
                showErrorDialog("Error pausing music: " + e.getMessage());
            }
        }
    }

    // Stop button handler
    @FXML
    private void stopMusic() {
        if (mediaPlayer != null) {
            try {
                mediaPlayer.stop();
                playButton.setDisable(false);
                pauseButton.setDisable(true);
                progressSlider.setValue(0);
                elapsedTimeLabel.setText("0:00");
                isSeeking = false;
            } catch (Exception e) {
                showErrorDialog("Error stopping music: " + e.getMessage());
            }
        }
    }

    // Apply text filter to songs
    @FXML
    private void applyFilter() {
        String filterText = filterField.getText().trim();
        if (filterText.isEmpty()) {
            filteredSongs.setPredicate(song -> true);
        } else {
            filteredSongs.setPredicate(song ->
                    song.getTitle().toLowerCase().contains(filterText.toLowerCase()) ||
                            song.getArtist().toLowerCase().contains(filterText.toLowerCase())
            );
        }
    }

    // Clear search filter
    @FXML
    private void clearFilter() {
        filterField.setText("");
        applyFilter();
    }

    // Open new playlist dialog
    @FXML
    private void createNewPlaylist() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/dk/easv/demo/GUI/PlaylistEditor.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("New Playlist");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

            loadDataFromDatabase();

        } catch (Exception e) {
            showErrorDialog("Error creating playlist: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Open edit playlist dialog
    @FXML
    private void editPlaylist() {
        Playlist selected = playlistsTableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showErrorDialog("Please select a playlist to edit");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/dk/easv/demo/GUI/PlaylistEditor.fxml"));
            Parent root = loader.load();

            PlaylistEditorController controller = loader.getController();
            controller.setPlaylist(selected);

            Stage stage = new Stage();
            stage.setTitle("Edit Playlist: " + selected.getName());
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

            loadDataFromDatabase();

        } catch (Exception e) {
            showErrorDialog("Error editing playlist: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Delete selected playlist
    @FXML
    private void deletePlaylist() {
        Playlist selected = playlistsTableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showErrorDialog("Please select a playlist to delete");
            return;
        }

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Delete Playlist");
        confirmation.setHeaderText("Are you sure you want to delete this playlist?");
        confirmation.setContentText("Playlist: " + selected.getName());

        confirmation.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    playlistManager.deletePlaylist(selected);
                    allPlaylists.remove(selected);
                    showInfoDialog("Success", "Playlist '" + selected.getName() + "' deleted successfully");

                    if (selectedPlaylist != null && selectedPlaylist.getId() == selected.getId()) {
                        selectedPlaylist = null;
                        currentPlaylistSongs.clear();
                    }
                } catch (Exception e) {
                    showErrorDialog("Error deleting playlist: " + e.getMessage());
                }
            }
        });
    }

    // Open new song dialog
    @FXML
    private void createNewSong() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/dk/easv/demo/GUI/SongEditor.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("New Song");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

            loadDataFromDatabase();

        } catch (Exception e) {
            showErrorDialog("Error creating song: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Open edit song dialog
    @FXML
    private void editSong() {
        Song selected = songsTableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showErrorDialog("Please select a song to edit");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/dk/easv/demo/GUI/SongEditor.fxml"));
            Parent root = loader.load();

            SongEditorController controller = loader.getController();
            controller.setSong(selected);

            Stage stage = new Stage();
            stage.setTitle("Edit Song: " + selected.getTitle());
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

            loadDataFromDatabase();

        } catch (Exception e) {
            showErrorDialog("Error editing song: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Delete selected song
    @FXML
    private void deleteSong() {
        Song selected = songsTableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showErrorDialog("Please select a song to delete");
            return;
        }

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Delete Song");
        confirmation.setHeaderText("Are you sure you want to delete this song?");
        confirmation.setContentText("Song: " + selected.getTitle() + " - " + selected.getArtist());

        confirmation.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    songManager.deleteSong(selected);
                    allSongs.remove(selected);
                    showInfoDialog("Success", "Song '" + selected.getTitle() + "' deleted successfully");
                } catch (Exception e) {
                    showErrorDialog("Error deleting song: " + e.getMessage());
                }
            }
        });
    }

    // Add selected song to selected playlist
    @FXML
    private void addSongToPlaylist() {
        Song selectedSong = songsTableView.getSelectionModel().getSelectedItem();
        Playlist selectedPlaylist = playlistsTableView.getSelectionModel().getSelectedItem();

        if (selectedSong == null || selectedPlaylist == null) {
            showErrorDialog("Please select both a song and a playlist");
            return;
        }

        try {
            playlistManager.addSongToPlaylist(selectedPlaylist, selectedSong);
            loadPlaylistSongs(selectedPlaylist);
            refreshPlaylistDisplay();
            showInfoDialog("Success", "Added '" + selectedSong.getTitle() + "' to '" + selectedPlaylist.getName() + "'");
        } catch (Exception e) {
            showErrorDialog("Error adding song to playlist: " + e.getMessage());
        }
    }

    // Move song up in playlist
    @FXML
    private void moveSongUp() {
        Song selectedSong = playlistSongsListView.getSelectionModel().getSelectedItem();
        if (selectedSong == null || selectedPlaylist == null) {
            showErrorDialog("Please select a song from the playlist to move");
            return;
        }

        try {
            playlistManager.moveSongUp(selectedPlaylist, selectedSong);

            // Refresh display and keep selection
            loadPlaylistSongs(selectedPlaylist);
            int newIndex = Math.max(0, playlistSongsListView.getSelectionModel().getSelectedIndex() - 1);
            playlistSongsListView.getSelectionModel().select(newIndex);

        } catch (Exception e) {
            showErrorDialog("Error moving song up: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Move song down in playlist
    @FXML
    private void moveSongDown() {
        Song selectedSong = playlistSongsListView.getSelectionModel().getSelectedItem();
        if (selectedSong == null || selectedPlaylist == null) {
            showErrorDialog("Please select a song from the playlist to move");
            return;
        }

        try {
            playlistManager.moveSongDown(selectedPlaylist, selectedSong);

            // Refresh display and keep selection
            loadPlaylistSongs(selectedPlaylist);
            int newIndex = Math.min(playlistSongsListView.getItems().size() - 1,
                    playlistSongsListView.getSelectionModel().getSelectedIndex() + 1);
            playlistSongsListView.getSelectionModel().select(newIndex);

        } catch (Exception e) {
            showErrorDialog("Error moving song down: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Remove song from playlist
    @FXML
    private void removeSongFromPlaylist() {
        Song selectedSong = playlistSongsListView.getSelectionModel().getSelectedItem();
        if (selectedSong == null || selectedPlaylist == null) {
            showErrorDialog("Please select a song from the playlist to remove");
            return;
        }

        try {
            playlistManager.removeSongFromPlaylist(selectedPlaylist, selectedSong);
            currentPlaylistSongs.remove(selectedSong);
            refreshPlaylistDisplay();
            showInfoDialog("Success", "Removed '" + selectedSong.getTitle() + "' from playlist");
        } catch (Exception e) {
            showErrorDialog("Error removing song from playlist: " + e.getMessage());
        }
    }

    // Close application
    @FXML
    private void closeApplication() {
        shutdown();
        Stage stage = (Stage) songsTableView.getScene().getWindow();
        stage.close();
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

    // Cleanup media player on shutdown
    public void shutdown() {
        if (mediaPlayer != null) {
            try {
                mediaPlayer.stop();
            } catch (Exception e) {
                System.out.println("Error stopping media player on shutdown: " + e.getMessage());
            }
            mediaPlayer.dispose();
            mediaPlayer = null;
        }
    }
}