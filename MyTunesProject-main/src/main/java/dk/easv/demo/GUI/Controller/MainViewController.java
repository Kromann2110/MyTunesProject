package dk.easv.demo.GUI.Controller;

import dk.easv.demo.BLL.MusicManager;
import dk.easv.demo.BE.Song;
import dk.easv.demo.BE.Playlist;
import dk.easv.demo.DAL.db.SongDAO_DB;
import dk.easv.demo.DAL.db.PlaylistDAO_DB;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import java.util.List;

public class MainViewController {

    // FXML controls for songs table
    @FXML private TableView<Song> songsTable;
    @FXML private TableColumn<Song, String> titleColumn;
    @FXML private TableColumn<Song, String> artistColumn;
    @FXML private TableColumn<Song, String> categoryColumn;
    @FXML private TableColumn<Song, String> durationColumn;

    // FXML controls for playlists table
    @FXML private TableView<Playlist> playlistsTable;
    @FXML private TableColumn<Playlist, String> playlistNameColumn;
    @FXML private TableColumn<Playlist, Integer> songCountColumn;
    @FXML private TableColumn<Playlist, String> totalTimeColumn;

    // FXML controls for playback
    @FXML private Label nowPlayingLabel;
    @FXML private Slider volumeSlider;

    // FXML buttons
    @FXML private Button btnPlay;
    @FXML private Button btnPause;
    @FXML private Button btnStop;
    @FXML private Button btnAddToPlaylist;
    @FXML private Button btnCreatePlaylist;
    @FXML private Button btnDeleteSong;
    @FXML private Button btnDeletePlaylist;

    // Managers
    private MusicManager musicManager;
    private PlaylistDAO_DB playlistDAO;

    /**
     * Initialize the controller
     */
    @FXML
    public void initialize() {
        try {
            // Create managers
            musicManager = new MusicManager();
            playlistDAO = new PlaylistDAO_DB();

            // Set up table columns
            setupTableColumns();

            // Load data
            loadSongs();
            loadPlaylists();

            // Set up event handlers
            setupEventHandlers();

            System.out.println("MainViewController initialized successfully");

        } catch (Exception e) {
            showError("Initialization Error", "Failed to initialize application: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Set up table column bindings
     */
    private void setupTableColumns() {
        // Songs table
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        artistColumn.setCellValueFactory(new PropertyValueFactory<>("artist"));
        categoryColumn.setCellValueFactory(new PropertyValueFactory<>("category"));
        durationColumn.setCellValueFactory(new PropertyValueFactory<>("formattedDuration"));

        // Playlists table
        playlistNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        songCountColumn.setCellValueFactory(new PropertyValueFactory<>("songCount"));
        totalTimeColumn.setCellValueFactory(new PropertyValueFactory<>("formattedTotalTime"));
    }

    /**
     * Load songs from database
     */
    private void loadSongs() {
        try {
            List<Song> songs = musicManager.getAllSongs();
            ObservableList<Song> observableSongs = FXCollections.observableArrayList(songs);
            songsTable.setItems(observableSongs);

            System.out.println("Loaded " + songs.size() + " songs from database");

        } catch (Exception e) {
            showError("Load Error", "Failed to load songs: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Load playlists from database
     */
    private void loadPlaylists() {
        try {
            List<Playlist> playlists = playlistDAO.getAllPlaylists();

            // Calculate song count and total time for each playlist
            for (Playlist playlist : playlists) {
                List<Song> playlistSongs = playlistDAO.getSongsInPlaylist(playlist.getId());
                playlist.setSongCount(playlistSongs.size());

                int totalSeconds = 0;
                for (Song song : playlistSongs) {
                    totalSeconds += song.getDuration();
                }
                playlist.setTotalTime(totalSeconds);
            }

            ObservableList<Playlist> observablePlaylists = FXCollections.observableArrayList(playlists);
            playlistsTable.setItems(observablePlaylists);

            System.out.println("Loaded " + playlists.size() + " playlists from database");

        } catch (Exception e) {  // Changed from SQLException to Exception
            showError("Load Error", "Failed to load playlists: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Set up button event handlers
     */
    private void setupEventHandlers() {
        // Play button
        if (btnPlay != null) {
            btnPlay.setOnAction(event -> handlePlay());
        }

        // Pause button
        if (btnPause != null) {
            btnPause.setOnAction(event -> handlePause());
        }

        // Stop button
        if (btnStop != null) {
            btnStop.setOnAction(event -> handleStop());
        }

        // Add to playlist button
        if (btnAddToPlaylist != null) {
            btnAddToPlaylist.setOnAction(event -> handleAddToPlaylist());
        }

        // Create playlist button
        if (btnCreatePlaylist != null) {
            btnCreatePlaylist.setOnAction(event -> handleCreatePlaylist());
        }

        // Delete song button
        if (btnDeleteSong != null) {
            btnDeleteSong.setOnAction(event -> handleDeleteSong());
        }

        // Delete playlist button
        if (btnDeletePlaylist != null) {
            btnDeletePlaylist.setOnAction(event -> handleDeletePlaylist());
        }

        // Double-click song to play
        songsTable.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                handlePlay();
            }
        });

        // Volume slider
        if (volumeSlider != null) {
            volumeSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
                setVolume(newValue.doubleValue() / 100);
            });
        }
    }

    /**
     * Play selected song
     */
    @FXML
    private void handlePlay() {
        Song selectedSong = songsTable.getSelectionModel().getSelectedItem();
        if (selectedSong != null) {
            nowPlayingLabel.setText("Now Playing: " + selectedSong.getTitle() + " - " + selectedSong.getArtist());

            // TODO: Implement actual audio playback
            System.out.println("Playing: " + selectedSong.getFilePath());
            System.out.println("Duration: " + selectedSong.getFormattedDuration());

        } else {
            showInfo("No Selection", "Please select a song to play.");
        }
    }

    /**
     * Pause playback
     */
    @FXML
    private void handlePause() {
        System.out.println("Playback paused");
        // TODO: Implement pause functionality
    }

    /**
     * Stop playback
     */
    @FXML
    private void handleStop() {
        System.out.println("Playback stopped");
        nowPlayingLabel.setText("Stopped");
        // TODO: Implement stop functionality
    }

    /**
     * Add selected song to selected playlist
     */
    @FXML
    private void handleAddToPlaylist() {
        Song selectedSong = songsTable.getSelectionModel().getSelectedItem();
        Playlist selectedPlaylist = playlistsTable.getSelectionModel().getSelectedItem();

        if (selectedSong == null) {
            showError("No Song Selected", "Please select a song first.");
            return;
        }

        if (selectedPlaylist == null) {
            showError("No Playlist Selected", "Please select a playlist first.");
            return;
        }

        try {
            // Get current position (add to end)
            List<Song> currentSongs = playlistDAO.getSongsInPlaylist(selectedPlaylist.getId());
            int position = currentSongs.size();

            playlistDAO.addSongToPlaylist(selectedPlaylist.getId(), selectedSong.getId(), position);

            showInfo("Success", "Added '" + selectedSong.getTitle() + "' to '" + selectedPlaylist.getName() + "'");

            // Refresh playlist display
            loadPlaylists();

        } catch (Exception e) {  // Changed from SQLException to Exception
            showError("Database Error", "Failed to add song to playlist: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Create a new playlist
     */
    @FXML
    private void handleCreatePlaylist() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Create New Playlist");
        dialog.setHeaderText("Enter playlist name:");
        dialog.setContentText("Name:");

        dialog.showAndWait().ifPresent(name -> {
            if (!name.trim().isEmpty()) {
                try {
                    Playlist newPlaylist = playlistDAO.createPlaylist(name.trim());

                    showInfo("Success", "Created playlist: " + name);

                    // Refresh playlist display
                    loadPlaylists();

                } catch (Exception e) {  // Changed from SQLException to Exception
                    showError("Error", "Failed to create playlist: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * Delete selected song
     */
    @FXML
    private void handleDeleteSong() {
        Song selectedSong = songsTable.getSelectionModel().getSelectedItem();
        if (selectedSong != null) {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Delete Song");
            confirm.setHeaderText("Delete '" + selectedSong.getTitle() + "'?");
            confirm.setContentText("This action cannot be undone.");

            if (confirm.showAndWait().get() == ButtonType.OK) {
                try {
                    musicManager.deleteSong(selectedSong);
                    loadSongs(); // Refresh the list
                    showInfo("Success", "Song deleted successfully.");
                } catch (Exception e) {
                    showError("Error", "Failed to delete song: " + e.getMessage());
                }
            }
        } else {
            showInfo("No Selection", "Please select a song to delete.");
        }
    }

    /**
     * Delete selected playlist
     */
    @FXML
    private void handleDeletePlaylist() {
        Playlist selectedPlaylist = playlistsTable.getSelectionModel().getSelectedItem();
        if (selectedPlaylist != null) {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Delete Playlist");
            confirm.setHeaderText("Delete playlist '" + selectedPlaylist.getName() + "'?");
            confirm.setContentText("This will remove all songs from the playlist. This action cannot be undone.");

            if (confirm.showAndWait().get() == ButtonType.OK) {
                try {
                    playlistDAO.deletePlaylist(selectedPlaylist);
                    loadPlaylists(); // Refresh the list
                    showInfo("Success", "Playlist deleted successfully.");
                } catch (Exception e) {  // Changed from SQLException to Exception
                    showError("Error", "Failed to delete playlist: " + e.getMessage());
                }
            }
        } else {
            showInfo("No Selection", "Please select a playlist to delete.");
        }
    }

    /**
     * Set volume level
     * @param volume Volume level (0.0 to 1.0)
     */
    private void setVolume(double volume) {
        System.out.println("Setting volume to: " + volume);
        // TODO: Implement volume control
    }

    /**
     * Show error dialog
     */
    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Show info dialog
     */
    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}