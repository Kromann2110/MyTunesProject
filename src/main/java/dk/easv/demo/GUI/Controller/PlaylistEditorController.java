package dk.easv.demo.GUI.Controller;

// Business entities
import dk.easv.demo.BE.Playlist;
import dk.easv.demo.BE.Song;

// Business logic
import dk.easv.demo.BLL.PlaylistManager;
import dk.easv.demo.BLL.SongManager;

// Java standard
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
/**
 * Controller for creating/editing playlists
 */
public class PlaylistEditorController implements Initializable {

    @FXML private ListView<Song> availableSongsListView;
    @FXML private ListView<Song> playlistSongsListView;
    @FXML private TextField playlistNameField;
    @FXML private Button cancelButton;

    private PlaylistManager playlistManager;
    private SongManager songManager;
    private ObservableList<Song> allSongs;
    private ObservableList<Song> playlistSongs;
    private Playlist currentPlaylist;
    private boolean isEditMode = false;

    // Initialize controller
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            playlistManager = new PlaylistManager();
            songManager = new SongManager();
            allSongs = FXCollections.observableArrayList();
            playlistSongs = FXCollections.observableArrayList();

            loadAllSongs();

            availableSongsListView.setItems(allSongs);
            playlistSongsListView.setItems(playlistSongs);

        } catch (Exception e) {
            showErrorDialog("Error initializing playlist editor: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Load all songs from database
    private void loadAllSongs() {
        try {
            List<Song> songs = songManager.getAllSongs();
            allSongs.setAll(songs);
        } catch (Exception e) {
            showErrorDialog("Error loading songs: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Load playlist for editing
    public void setPlaylist(Playlist playlist) {
        this.currentPlaylist = playlist;
        this.isEditMode = true;

        if (playlist != null) {
            playlistNameField.setText(playlist.getName());

            try {
                // Load playlist songs and remove from available
                List<Song> songs = playlistManager.getSongsInPlaylist(playlist);
                playlistSongs.setAll(songs);
                allSongs.removeAll(playlistSongs);
            } catch (Exception e) {
                showErrorDialog("Error loading playlist songs: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    // Add song to playlist
    @FXML
    private void addSongToPlaylist() {
        Song selectedSong = availableSongsListView.getSelectionModel().getSelectedItem();
        if (selectedSong != null) {
            playlistSongs.add(selectedSong);
            allSongs.remove(selectedSong);
        } else {
            showErrorDialog("Please select a song to add to the playlist");
        }
    }

    // Remove song from playlist
    @FXML
    private void removeSongFromPlaylist() {
        Song selectedSong = playlistSongsListView.getSelectionModel().getSelectedItem();
        if (selectedSong != null) {
            allSongs.add(selectedSong);
            playlistSongs.remove(selectedSong);
        } else {
            showErrorDialog("Please select a song to remove from the playlist");
        }
    }

    // Save playlist changes
    @FXML
    private void savePlaylist() {
        try {
            String playlistName = playlistNameField.getText().trim();

            if (playlistName.isEmpty()) {
                showErrorDialog("Please enter a playlist name");
                return;
            }

            if (playlistSongs.isEmpty()) {
                showErrorDialog("Please add at least one song to the playlist");
                return;
            }

            if (isEditMode && currentPlaylist != null) {
                // Update existing playlist
                currentPlaylist.setName(playlistName);
                playlistManager.updatePlaylist(currentPlaylist);
                updatePlaylistSongs();
                showInfoDialog("Success", "Playlist '" + playlistName + "' updated successfully");
            } else {
                // Create new playlist
                Playlist newPlaylist = playlistManager.createPlaylist(playlistName);
                if (newPlaylist != null) {
                    for (Song song : playlistSongs) {
                        playlistManager.addSongToPlaylist(newPlaylist, song);
                    }
                }
                showInfoDialog("Success", "Playlist '" + playlistName + "' created successfully");
            }

            closeWindow();

        } catch (Exception e) {
            showErrorDialog("Error saving playlist: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Update playlist songs in database
    private void updatePlaylistSongs() {
        try {
            List<Song> currentSongsInPlaylist = playlistManager.getSongsInPlaylist(currentPlaylist);

            // Remove songs that were deleted
            for (Song song : currentSongsInPlaylist) {
                if (!playlistSongs.contains(song)) {
                    playlistManager.removeSongFromPlaylist(currentPlaylist, song);
                }
            }

            // Add songs that were added
            for (Song song : playlistSongs) {
                if (!currentSongsInPlaylist.contains(song)) {
                    playlistManager.addSongToPlaylist(currentPlaylist, song);
                }
            }

        } catch (Exception e) {
            showErrorDialog("Error updating playlist songs: " + e.getMessage());
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
        Node[] nodes = {playlistNameField, availableSongsListView, playlistSongsListView, cancelButton};

        for (Node node : nodes) {
            if (node != null && node.getScene() != null) {
                Stage stage = (Stage) node.getScene().getWindow();
                stage.close();
                return;
            }
        }

        System.err.println("Could not close window - no valid node found");
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