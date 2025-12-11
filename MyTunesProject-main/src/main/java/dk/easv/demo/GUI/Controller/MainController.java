package dk.easv.demo.GUI.Controller;

import dk.easv.demo.BE.Playlist;
import dk.easv.demo.BE.Song;
import dk.easv.demo.BLL.MusicManager;
import dk.easv.demo.BLL.PlaylistManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class MainController implements Initializable {

    @FXML private Label nowPlayingLabel;
    @FXML private Slider volumeSlider;
    @FXML private Slider progressSlider;
    @FXML private Label elapsedTimeLabel;
    @FXML private Label totalTimeLabel;

    @FXML private Button playButton;
    @FXML private Button pauseButton;
    @FXML private Button stopButton;

    @FXML private TableView<Song> songsTableView;
    @FXML private TableColumn<Song, String> songTitleColumn;
    @FXML private TableColumn<Song, String> songArtistColumn;
    @FXML private TableColumn<Song, String> songCategoryColumn;
    @FXML private TableColumn<Song, String> songTimeColumn;

    @FXML private TableView<Playlist> playlistsTableView;
    @FXML private TableColumn<Playlist, String> playlistNameColumn;
    @FXML private TableColumn<Playlist, Integer> playlistSongsColumn;
    @FXML private TableColumn<Playlist, String> playlistTimeColumn;

    @FXML private ListView<Song> playlistSongsListView;

    @FXML private Button btnAddToPlaylist;
    @FXML private Button btnCreatePlaylist;
    @FXML private Button btnDeleteSong;
    @FXML private Button btnDeletePlaylist;
    @FXML private Button btnMoveUp;
    @FXML private Button btnMoveDown;
    @FXML private Button btnRemoveFromPlaylist;
    @FXML private Button btnClose;
    @FXML private Button btnImportSongs;

    @FXML private Button newPlaylistButton;
    @FXML private Button editPlaylistButton;
    @FXML private Button newSongButton;
    @FXML private Button editSongButton;
    @FXML private Button closeButton;

    private MusicManager musicManager;
    private PlaylistManager playlistManager;
    private MediaPlayer mediaPlayer;

    private ObservableList<Song> allSongs;
    private ObservableList<Playlist> allPlaylists;
    private Playlist selectedPlaylist;

    private boolean isSeeking = false;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            musicManager = new MusicManager();
            playlistManager = new PlaylistManager();

            allSongs = FXCollections.observableArrayList();
            allPlaylists = FXCollections.observableArrayList();

            setupMediaControls();
            setupTableColumns();
            setupEventHandlers();
            setupPlaylistSongsDisplay(); // Custom display for playlist songs

            loadDataFromDatabase();

            pauseButton.setDisable(true);
            stopButton.setDisable(true);
            nowPlayingLabel.setText("No song playing");

        } catch (Exception e) {
            showError("Initialization Error", "Failed to initialize: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Setup volume and progress slider controls
    private void setupMediaControls() {
        volumeSlider.setValue(50);

        volumeSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (mediaPlayer != null) {
                mediaPlayer.setVolume(newValue.doubleValue() / 100.0);
            }
        });

        // When dragging starts, stop auto-updates
        progressSlider.setOnMousePressed(e -> isSeeking = true);

        // When dragging stops or user clicks, seek to that position
        progressSlider.setOnMouseReleased(e -> {
            if (mediaPlayer != null) {
                Duration totalDuration = mediaPlayer.getTotalDuration();
                if (totalDuration != null && !totalDuration.isUnknown()) {
                    double seekPosition = progressSlider.getValue() / 100.0;
                    Duration seekTime = totalDuration.multiply(seekPosition);
                    mediaPlayer.seek(seekTime);
                }
            }
            isSeeking = false;
        });
    }

    // Setup table columns for songs and playlists
    private void setupTableColumns() {
        songTitleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        songArtistColumn.setCellValueFactory(new PropertyValueFactory<>("artist"));
        songCategoryColumn.setCellValueFactory(new PropertyValueFactory<>("category"));
        songTimeColumn.setCellValueFactory(new PropertyValueFactory<>("formattedDuration"));

        playlistNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        playlistSongsColumn.setCellValueFactory(new PropertyValueFactory<>("songCount"));
        playlistTimeColumn.setCellValueFactory(new PropertyValueFactory<>("totalDuration"));

        // Double-click on song to play it
        songsTableView.setRowFactory(tv -> {
            TableRow<Song> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    playSelectedSong(row.getItem());
                }
            });
            return row;
        });
    }

    // Setup custom display for songs in playlist (Title - Artist instead of Artist - Duration)
    private void setupPlaylistSongsDisplay() {
        playlistSongsListView.setCellFactory(param -> new ListCell<Song>() {
            @Override
            protected void updateItem(Song song, boolean empty) {
                super.updateItem(song, empty);
                if (empty || song == null) {
                    setText(null);
                } else {
                    setText(song.getTitle() + " - " + song.getArtist());
                }
            }
        });
    }

    // Setup listeners for playlist and song selection
    private void setupEventHandlers() {
        playlistsTableView.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> {
                    selectedPlaylist = newValue;
                    loadPlaylistSongs(newValue);
                });

        // Double-click on playlist song to play it
        playlistSongsListView.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                Song selectedSong = playlistSongsListView.getSelectionModel().getSelectedItem();
                if (selectedSong != null) {
                    playSelectedSong(selectedSong);
                }
            }
        });
    }

    // Load all songs and playlists from database
    private void loadDataFromDatabase() {
        try {
            List<Song> songs = musicManager.getAllSongs();
            allSongs.setAll(songs);
            songsTableView.setItems(allSongs);

            List<Playlist> playlists = playlistManager.getAllPlaylists();

            for (Playlist playlist : playlists) {
                try {
                    List<Song> playlistSongs = playlistManager.getSongsInPlaylist(playlist);
                    playlist.setSongs(playlistSongs);

                    int totalSeconds = 0;
                    for (Song song : playlistSongs) {
                        totalSeconds += song.getDuration();
                    }
                    playlist.setTotalTime(totalSeconds);

                } catch (Exception e) {
                    System.out.println("Error loading songs for playlist " + playlist.getName());
                }
            }

            allPlaylists.setAll(playlists);
            playlistsTableView.setItems(allPlaylists);

            if (!playlists.isEmpty()) {
                playlistsTableView.getSelectionModel().selectFirst();
            }

        } catch (Exception e) {
            showError("Load Error", "Failed to load data: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Load songs for selected playlist
    private void loadPlaylistSongs(Playlist playlist) {
        if (playlist == null) {
            playlistSongsListView.getItems().clear();
            return;
        }

        try {
            List<Song> songs = playlistManager.getSongsInPlaylist(playlist);
            playlistSongsListView.getItems().setAll(songs);
        } catch (Exception e) {
            showError("Load Error", "Failed to load playlist songs: " + e.getMessage());
        }
    }

    // Play the selected song
    private void playSelectedSong(Song song) {
        if (song == null) {
            showInfo("No Selection", "Please select a song to play.");
            return;
        }

        try {
            // Stop and clean up previous player
            if (mediaPlayer != null) {
                mediaPlayer.stop();
                mediaPlayer.dispose();
                mediaPlayer = null;
            }

            String filePath = song.getFilePath();
            File file = new File(filePath);

            if (!file.exists()) {
                showError("File Not Found", "Song file not found:\n" + filePath);
                return;
            }

            Media media = new Media(file.toURI().toString());
            mediaPlayer = new MediaPlayer(media);
            mediaPlayer.setVolume(volumeSlider.getValue() / 100.0);

            mediaPlayer.setOnReady(() -> {
                nowPlayingLabel.setText("Now Playing: " + song.getTitle() + " - " + song.getArtist());
                setupProgressTracking();
                playMusic();
            });

            mediaPlayer.setOnError(() -> {
                showError("Playback Error", "Could not play: " + song.getTitle());
            });

            mediaPlayer.setOnEndOfMedia(() -> {
                stopMusic();
            });

        } catch (Exception e) {
            showError("Playback Error", "Error playing song: " + e.getMessage());
        }
    }

    // Setup progress tracking for currently playing song
    private void setupProgressTracking() {
        if (mediaPlayer == null) return;

        progressSlider.setValue(0);
        elapsedTimeLabel.setText("0:00");

        mediaPlayer.setOnReady(() -> {
            try {
                Duration totalDuration = mediaPlayer.getTotalDuration();
                if (totalDuration != null && !totalDuration.isUnknown()) {
                    totalTimeLabel.setText(formatTime(totalDuration));
                }
            } catch (Exception e) {
                System.out.println("Error setting total duration");
            }
        });

        // Update progress slider and time labels as song plays
        mediaPlayer.currentTimeProperty().addListener((observable, oldValue, newValue) -> {
            try {
                if (!isSeeking && mediaPlayer != null) {
                    Duration current = newValue;
                    Duration total = mediaPlayer.getTotalDuration();

                    if (total != null && total.greaterThan(Duration.ZERO) && !total.isUnknown()) {
                        double progress = current.toSeconds() / total.toSeconds();
                        progressSlider.setValue(progress * 100);
                        elapsedTimeLabel.setText(formatTime(current));
                        totalTimeLabel.setText(formatTime(total));
                    } else {
                        elapsedTimeLabel.setText(formatTime(current));
                    }
                }
            } catch (Exception e) {
                System.out.println("Error updating progress");
            }
        });
    }

    // Format duration to MM:SS
    private String formatTime(Duration duration) {
        if (duration == null || duration.isUnknown()) {
            return "0:00";
        }

        int totalSeconds = (int) Math.floor(duration.toSeconds());
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;

        return String.format("%d:%02d", minutes, seconds);
    }

    // Play button action
    @FXML
    private void playMusic() {
        try {
            if (mediaPlayer != null) {
                mediaPlayer.play();
                playButton.setDisable(true);
                pauseButton.setDisable(false);
                stopButton.setDisable(false);
            } else {
                handlePlay();
            }
        } catch (Exception e) {
            showError("Playback Error", "Could not play music: " + e.getMessage());
        }
    }

    // Pause button action
    @FXML
    private void pauseMusic() {
        if (mediaPlayer != null) {
            mediaPlayer.pause();
            playButton.setDisable(false);
            pauseButton.setDisable(true);
        }
    }

    // Stop button action
    @FXML
    private void stopMusic() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            playButton.setDisable(false);
            pauseButton.setDisable(true);
            stopButton.setDisable(true);
            progressSlider.setValue(0);
            elapsedTimeLabel.setText("0:00");
            nowPlayingLabel.setText("No song playing");
        }
    }

    // Create new playlist dialog
    @FXML
    private void createNewPlaylist() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Create New Playlist");
        dialog.setHeaderText("Enter playlist name:");
        dialog.setContentText("Name:");

        dialog.showAndWait().ifPresent(name -> {
            if (!name.trim().isEmpty()) {
                try {
                    Playlist newPlaylist = playlistManager.createPlaylist(name.trim());
                    allPlaylists.add(newPlaylist);
                    playlistsTableView.getSelectionModel().select(newPlaylist);
                    showInfo("Success", "Created playlist: " + name);
                } catch (Exception e) {
                    showError("Error", "Failed to create playlist: " + e.getMessage());
                }
            }
        });
    }

    // Edit selected playlist name
    @FXML
    private void editPlaylist() {
        Playlist selectedPlaylist = playlistsTableView.getSelectionModel().getSelectedItem();
        if (selectedPlaylist == null) {
            showInfo("No Selection", "Please select a playlist to edit.");
            return;
        }

        TextInputDialog dialog = new TextInputDialog(selectedPlaylist.getName());
        dialog.setTitle("Edit Playlist");
        dialog.setHeaderText("Edit playlist name:");
        dialog.setContentText("Name:");

        dialog.showAndWait().ifPresent(newName -> {
            if (!newName.trim().isEmpty() && !newName.equals(selectedPlaylist.getName())) {
                try {
                    selectedPlaylist.setName(newName.trim());
                    playlistManager.updatePlaylist(selectedPlaylist);
                    playlistsTableView.refresh();
                    showInfo("Success", "Playlist updated to: " + newName);
                } catch (Exception e) {
                    showError("Error", "Failed to update playlist: " + e.getMessage());
                }
            }
        });
    }

    // Delete selected playlist
    @FXML
    private void deletePlaylist() {
        Playlist selectedPlaylist = playlistsTableView.getSelectionModel().getSelectedItem();
        if (selectedPlaylist == null) {
            showInfo("No Selection", "Please select a playlist to delete.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Delete Playlist");
        confirm.setHeaderText("Delete playlist '" + selectedPlaylist.getName() + "'?");
        confirm.setContentText("This will remove all songs from the playlist.");

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    playlistManager.deletePlaylist(selectedPlaylist);
                    allPlaylists.remove(selectedPlaylist);

                    if (this.selectedPlaylist != null && this.selectedPlaylist.getId() == selectedPlaylist.getId()) {
                        this.selectedPlaylist = null;
                        playlistSongsListView.getItems().clear();
                    }

                    showInfo("Success", "Playlist deleted.");

                } catch (Exception e) {
                    showError("Error", "Failed to delete playlist: " + e.getMessage());
                }
            }
        });
    }

    // Create new song (placeholder)
    @FXML
    private void createNewSong() {
        showInfo("Create New Song", "This feature would open a dialog to create a new song.");
    }

    // Edit selected song (placeholder)
    @FXML
    private void editSong() {
        Song selectedSong = songsTableView.getSelectionModel().getSelectedItem();
        if (selectedSong == null) {
            showInfo("No Selection", "Please select a song to edit.");
            return;
        }
        showInfo("Edit Song", "This feature would open a dialog to edit the selected song.");
    }

    // Delete selected song
    @FXML
    private void deleteSong() {
        Song selectedSong = songsTableView.getSelectionModel().getSelectedItem();
        if (selectedSong == null) {
            showInfo("No Selection", "Please select a song to delete.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Delete Song");
        confirm.setHeaderText("Delete '" + selectedSong.getTitle() + "'?");
        confirm.setContentText("This action cannot be undone.");

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    musicManager.deleteSong(selectedSong);
                    allSongs.remove(selectedSong);
                    playlistSongsListView.getItems().remove(selectedSong);

                    // Remove from all playlists and update their totals
                    for (Playlist playlist : allPlaylists) {
                        playlist.getSongs().remove(selectedSong);
                        playlist.setSongCount(playlist.getSongs().size());
                        playlist.calculateTotalTime();
                    }
                    playlistsTableView.refresh();

                    showInfo("Success", "Song deleted.");

                } catch (Exception e) {
                    showError("Error", "Failed to delete song: " + e.getMessage());
                }
            }
        });
    }

    // Add selected song to selected playlist
    @FXML
    private void addSongToPlaylist() {
        Song selectedSong = songsTableView.getSelectionModel().getSelectedItem();
        Playlist selectedPlaylist = playlistsTableView.getSelectionModel().getSelectedItem();

        if (selectedSong == null) {
            showError("No Song Selected", "Please select a song first.");
            return;
        }

        if (selectedPlaylist == null) {
            showError("No Playlist Selected", "Please select a playlist first.");
            return;
        }

        try {
            playlistManager.addSongToPlaylist(selectedPlaylist, selectedSong);
            loadPlaylistSongs(selectedPlaylist);

            // Update playlist total time
            List<Song> playlistSongs = playlistManager.getSongsInPlaylist(selectedPlaylist);
            int totalSeconds = 0;
            for (Song song : playlistSongs) {
                totalSeconds += song.getDuration();
            }
            selectedPlaylist.setTotalTime(totalSeconds);
            playlistsTableView.refresh();

            showInfo("Success", "Added song to playlist");

        } catch (Exception e) {
            showError("Database Error", "Failed to add song to playlist: " + e.getMessage());
        }
    }

    // Move selected song up in playlist
    @FXML
    private void moveSongUp() {
        Song selectedSong = playlistSongsListView.getSelectionModel().getSelectedItem();
        if (selectedSong == null || selectedPlaylist == null) {
            showInfo("No Selection", "Please select a song from the playlist to move.");
            return;
        }

        try {
            playlistManager.moveSongUp(selectedPlaylist, selectedSong);
            loadPlaylistSongs(selectedPlaylist);

            // Keep selection on the moved song
            int currentIndex = playlistSongsListView.getItems().indexOf(selectedSong);
            if (currentIndex > 0) {
                playlistSongsListView.getSelectionModel().select(currentIndex - 1);
            }

        } catch (Exception e) {
            showError("Error", "Failed to move song up: " + e.getMessage());
        }
    }

    // Move selected song down in playlist
    @FXML
    private void moveSongDown() {
        Song selectedSong = playlistSongsListView.getSelectionModel().getSelectedItem();
        if (selectedSong == null || selectedPlaylist == null) {
            showInfo("No Selection", "Please select a song from the playlist to move.");
            return;
        }

        try {
            playlistManager.moveSongDown(selectedPlaylist, selectedSong);
            loadPlaylistSongs(selectedPlaylist);

            // Keep selection on the moved song
            int currentIndex = playlistSongsListView.getItems().indexOf(selectedSong);
            if (currentIndex < playlistSongsListView.getItems().size() - 1) {
                playlistSongsListView.getSelectionModel().select(currentIndex + 1);
            }

        } catch (Exception e) {
            showError("Error", "Failed to move song down: " + e.getMessage());
        }
    }

    // Remove selected song from playlist
    @FXML
    private void removeSongFromPlaylist() {
        Song selectedSong = playlistSongsListView.getSelectionModel().getSelectedItem();
        if (selectedSong == null || selectedPlaylist == null) {
            showInfo("No Selection", "Please select a song from the playlist to remove.");
            return;
        }

        try {
            playlistManager.removeSongFromPlaylist(selectedPlaylist, selectedSong);
            playlistSongsListView.getItems().remove(selectedSong);
            selectedPlaylist.removeSong(selectedSong);
            playlistsTableView.refresh();
            showInfo("Success", "Song removed from playlist.");

        } catch (Exception e) {
            showError("Error", "Failed to remove song from playlist: " + e.getMessage());
        }
    }

    // Import songs (placeholder)
    @FXML
    private void importSongs() {
        showInfo("Import Songs", "Import feature would open file browser to select songs.");
    }

    // Close application
    @FXML
    private void closeApplication() {
        shutdown();
        Stage stage = (Stage) closeButton.getScene().getWindow();
        stage.close();
    }

    // Handle play when no media player exists yet
    private void handlePlay() {
        if (mediaPlayer != null) {
            mediaPlayer.play();
            playButton.setDisable(true);
            pauseButton.setDisable(false);
            stopButton.setDisable(false);
        } else {
            Song selectedSong = songsTableView.getSelectionModel().getSelectedItem();
            if (selectedSong == null) {
                selectedSong = playlistSongsListView.getSelectionModel().getSelectedItem();
            }

            if (selectedSong != null) {
                playSelectedSong(selectedSong);
            } else {
                showInfo("No Selection", "Please select a song to play.");
            }
        }
    }

    // Show error dialog
    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Show info dialog
    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Cleanup when application closes
    public void shutdown() {
        if (mediaPlayer != null) {
            try {
                mediaPlayer.stop();
                mediaPlayer.dispose();
            } catch (Exception e) {
                System.out.println("Error shutting down media player");
            }
        }
    }
}