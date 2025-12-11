package dk.easv.demo.GUI.Controller;

import dk.easv.demo.BE.Playlist;
import dk.easv.demo.BE.Song;
import dk.easv.demo.BLL.MusicManager;
import dk.easv.demo.BLL.PlaylistManager;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class MainController implements Initializable {

    @FXML private Button playButton, pauseButton, stopButton;
    @FXML private Slider volumeSlider, progressSlider;
    @FXML private Label elapsedTimeLabel, totalTimeLabel, nowPlayingLabel;
    @FXML private TableView<Playlist> playlistsTableView;
    @FXML private TableColumn<Playlist, String> playlistNameColumn;
    @FXML private TableColumn<Playlist, Integer> playlistSongsColumn;
    @FXML private TableColumn<Playlist, String> playlistTimeColumn;
    @FXML private TableView<Song> songsTableView;
    @FXML private TableColumn<Song, String> songTitleColumn;
    @FXML private TableColumn<Song, String> songArtistColumn;
    @FXML private TableColumn<Song, String> songCategoryColumn;
    @FXML private TableColumn<Song, String> songTimeColumn;
    @FXML private ListView<Song> playlistSongsListView;
    @FXML private Button newPlaylistButton, editPlaylistButton, deletePlaylistButton;
    @FXML private Button newSongButton, editSongButton, deleteSongButton, addToPlaylistButton;
    @FXML private Button moveUpButton, moveDownButton, removeFromPlaylistButton;
    @FXML private Button closeButton;

    private MusicManager musicManager;
    private PlaylistManager playlistManager;
    private MediaPlayer mediaPlayer;
    private boolean userIsSeeking = false;
    private Song currentPlayingSong = null;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            musicManager = new MusicManager();
            playlistManager = new PlaylistManager();
            setupMediaPlayer();
            setupTableColumns();
            setupEventHandlers();
            loadDataFromDatabase();
            pauseButton.setDisable(true);
            stopButton.setDisable(true);

            // Remove blue color from Now Playing label
            nowPlayingLabel.setStyle("-fx-text-fill: black;");

        } catch (Exception e) {
            showErrorDialog("Error initializing application: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void setupMediaPlayer() {
        volumeSlider.setValue(50);
        volumeSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (mediaPlayer != null) {
                mediaPlayer.setVolume(newValue.doubleValue() / 100.0);
            }
        });
    }

    private void setupTableColumns() {
        songTitleColumn.setCellValueFactory(cellData -> cellData.getValue().titleProperty());
        songArtistColumn.setCellValueFactory(cellData -> cellData.getValue().artistProperty());
        songCategoryColumn.setCellValueFactory(cellData -> cellData.getValue().categoryProperty());
        songTimeColumn.setCellValueFactory(cellData -> cellData.getValue().formattedDurationProperty());

        playlistNameColumn.setCellValueFactory(cellData -> cellData.getValue().nameProperty());
        playlistSongsColumn.setCellValueFactory(cellData -> cellData.getValue().songCountProperty().asObject());
        playlistTimeColumn.setCellValueFactory(cellData -> cellData.getValue().totalDurationProperty());

        songsTableView.setRowFactory(tv -> {
            TableRow<Song> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    Song selectedSong = row.getItem();
                    playSong(selectedSong);
                }
            });
            return row;
        });

        playlistSongsListView.setCellFactory(lv -> {
            ListCell<Song> cell = new ListCell<Song>() {
                @Override
                protected void updateItem(Song song, boolean empty) {
                    super.updateItem(song, empty);
                    if (empty || song == null) {
                        setText(null);
                    } else {
                        setText(song.getTitle() + " - " + song.getArtist());
                    }
                }
            };

            cell.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !cell.isEmpty()) {
                    Song selectedSong = cell.getItem();
                    playSong(selectedSong);
                }
            });

            return cell;
        });

        playlistsTableView.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newPlaylist) -> {
                    if (newPlaylist != null) {
                        try {
                            List<Song> playlistSongs = playlistManager.getSongsInPlaylist(newPlaylist);
                            playlistSongsListView.getItems().setAll(playlistSongs);
                        } catch (Exception e) {
                            showErrorDialog("Error loading playlist songs: " + e.getMessage());
                        }
                    }
                });
    }

    private void setupEventHandlers() {
        progressSlider.setOnMousePressed(event -> userIsSeeking = true);

        progressSlider.setOnMouseDragged(event -> {
            if (mediaPlayer != null && userIsSeeking) {
                mediaPlayer.seek(Duration.seconds(progressSlider.getValue()));
                elapsedTimeLabel.setText(formatTime(Duration.seconds(progressSlider.getValue())));
            }
        });

        progressSlider.setOnMouseReleased(event -> {
            if (mediaPlayer != null && userIsSeeking) {
                mediaPlayer.seek(Duration.seconds(progressSlider.getValue()));
                elapsedTimeLabel.setText(formatTime(Duration.seconds(progressSlider.getValue())));
                userIsSeeking = false;
            }
        });
    }

    private void loadDataFromDatabase() {
        try {
            List<Song> songs = musicManager.getAllSongs();
            songsTableView.getItems().setAll(songs);
            List<Playlist> playlists = playlistManager.getAllPlaylists();
            playlistsTableView.getItems().setAll(playlists);
            if (!playlists.isEmpty()) {
                playlistsTableView.getSelectionModel().selectFirst();
            }
        } catch (Exception e) {
            showErrorDialog("Database error loading data: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void playSong(Song song) {
        if (song == null) return;

        try {
            if (mediaPlayer != null) {
                mediaPlayer.stop();
                mediaPlayer.dispose();
            }

            currentPlayingSong = song;
            nowPlayingLabel.setText("Now Playing: " + song.getTitle() + " - " + song.getArtist());
            String filePath = song.getFilePath();

            if (filePath == null || filePath.isEmpty()) {
                showErrorDialog("No file path specified for: " + song.getTitle());
                return;
            }

            java.io.File file = new java.io.File(filePath);
            if (!file.exists()) {
                showErrorDialog("File does not exist: " + filePath);
                return;
            }

            Media media = new Media(file.toURI().toString());
            mediaPlayer = new MediaPlayer(media);

            mediaPlayer.setOnReady(() -> {
                Duration totalDuration = mediaPlayer.getMedia().getDuration();
                if (!totalDuration.isUnknown()) {
                    progressSlider.setMax(totalDuration.toSeconds());
                    totalTimeLabel.setText(formatTime(totalDuration));
                    mediaPlayer.currentTimeProperty().addListener((observable, oldValue, newValue) -> {
                        if (!userIsSeeking && !progressSlider.isValueChanging()) {
                            progressSlider.setValue(newValue.toSeconds());
                            elapsedTimeLabel.setText(formatTime(newValue));
                        }
                    });
                    playMusic();
                }
            });

            mediaPlayer.setOnError(() -> {
                showErrorDialog("Error playing: " + song.getTitle());
            });

            mediaPlayer.setOnEndOfMedia(() -> {
                playNextSong();
            });

        } catch (Exception e) {
            showErrorDialog("Error playing song: " + e.getMessage());
        }
    }

    private void playNextSong() {
        if (currentPlayingSong == null) return;
        List<Song> allSongs = songsTableView.getItems();
        int currentIndex = allSongs.indexOf(currentPlayingSong);
        if (currentIndex >= 0 && currentIndex < allSongs.size() - 1) {
            playSong(allSongs.get(currentIndex + 1));
        } else if (!allSongs.isEmpty()) {
            playSong(allSongs.get(0));
        }
    }

    private String formatTime(Duration duration) {
        if (duration == null || duration.isUnknown()) {
            return "00:00";
        }
        int totalSeconds = (int) Math.floor(duration.toSeconds());
        int hours = totalSeconds / 3600;
        int minutes = (totalSeconds % 3600) / 60;
        int seconds = totalSeconds % 60;
        if (hours > 0) {
            return String.format("%d:%02d:%02d", hours, minutes, seconds);
        } else {
            return String.format("%02d:%02d", minutes, seconds);
        }
    }

    @FXML private void playMusic() {
        if (mediaPlayer != null) {
            mediaPlayer.play();
            playButton.setDisable(true);
            pauseButton.setDisable(false);
            stopButton.setDisable(false);
        }
    }

    @FXML private void pauseMusic() {
        if (mediaPlayer != null) {
            mediaPlayer.pause();
            playButton.setDisable(false);
            pauseButton.setDisable(true);
        }
    }

    @FXML private void stopMusic() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            playButton.setDisable(false);
            pauseButton.setDisable(true);
            stopButton.setDisable(true);
            progressSlider.setValue(0);
            elapsedTimeLabel.setText("00:00");
            nowPlayingLabel.setText("No song playing");
            currentPlayingSong = null;
        }
    }

    @FXML private void createNewPlaylist() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("New Playlist");
        dialog.setHeaderText("Create a new playlist");
        dialog.setContentText("Enter playlist name:");
        dialog.showAndWait().ifPresent(playlistName -> {
            if (!playlistName.trim().isEmpty()) {
                try {
                    Playlist newPlaylist = playlistManager.createPlaylist(playlistName.trim());
                    playlistsTableView.getItems().add(newPlaylist);
                    playlistsTableView.getSelectionModel().select(newPlaylist);
                    showInfoDialog("Success", "Playlist created");
                } catch (Exception e) {
                    showErrorDialog("Error creating playlist: " + e.getMessage());
                }
            }
        });
    }

    @FXML private void editPlaylist() {
        Playlist selected = playlistsTableView.getSelectionModel().getSelectedItem();
        if (selected != null) {
            TextInputDialog dialog = new TextInputDialog(selected.getName());
            dialog.setTitle("Edit Playlist");
            dialog.setContentText("Enter new name:");
            dialog.showAndWait().ifPresent(newName -> {
                if (!newName.trim().isEmpty() && !newName.equals(selected.getName())) {
                    try {
                        selected.setName(newName.trim());
                        playlistManager.updatePlaylist(selected);
                        playlistsTableView.refresh();
                        showInfoDialog("Success", "Playlist renamed");
                    } catch (Exception e) {
                        showErrorDialog("Error updating playlist: " + e.getMessage());
                    }
                }
            });
        }
    }

    @FXML private void deletePlaylist() {
        Playlist selected = playlistsTableView.getSelectionModel().getSelectedItem();
        if (selected != null) {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Delete Playlist");
            confirm.setHeaderText("Delete '" + selected.getName() + "'?");
            confirm.setContentText("This will remove the playlist and all its songs.");
            confirm.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    try {
                        playlistManager.deletePlaylist(selected);
                        playlistsTableView.getItems().remove(selected);
                        playlistSongsListView.getItems().clear();
                        showInfoDialog("Success", "Playlist deleted");
                    } catch (Exception e) {
                        showErrorDialog("Error deleting playlist: " + e.getMessage());
                    }
                }
            });
        }
    }

    @FXML private void createNewSong() {
        showInfoDialog("New Song", "Feature to be implemented");
    }

    @FXML private void editSong() {
        Song selected = songsTableView.getSelectionModel().getSelectedItem();
        if (selected != null) {
            showInfoDialog("Edit Song", "Editing: " + selected.getTitle());
        }
    }

    @FXML private void deleteSong() {
        Song selected = songsTableView.getSelectionModel().getSelectedItem();
        if (selected != null) {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Delete Song");
            confirm.setHeaderText("Delete '" + selected.getTitle() + "'?");
            confirm.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    try {
                        musicManager.deleteSong(selected);
                        songsTableView.getItems().remove(selected);
                        playlistSongsListView.getItems().remove(selected);
                        showInfoDialog("Success", "Song deleted");
                    } catch (Exception e) {
                        showErrorDialog("Error deleting song: " + e.getMessage());
                    }
                }
            });
        }
    }

    @FXML private void addSongToPlaylist() {
        Song selectedSong = songsTableView.getSelectionModel().getSelectedItem();
        Playlist selectedPlaylist = playlistsTableView.getSelectionModel().getSelectedItem();
        if (selectedSong == null) {
            showErrorDialog("Please select a song first");
            return;
        }
        if (selectedPlaylist == null) {
            showErrorDialog("Please select a playlist first");
            return;
        }
        try {
            playlistManager.addSongToPlaylist(selectedPlaylist, selectedSong);
            List<Song> playlistSongs = playlistManager.getSongsInPlaylist(selectedPlaylist);
            playlistSongsListView.getItems().setAll(playlistSongs);
            playlistsTableView.refresh();
            showInfoDialog("Success", "Song added to playlist");
        } catch (Exception e) {
            showErrorDialog("Error adding song to playlist: " + e.getMessage());
        }
    }

    @FXML private void removeSongFromPlaylist() {
        Song selectedSong = playlistSongsListView.getSelectionModel().getSelectedItem();
        Playlist selectedPlaylist = playlistsTableView.getSelectionModel().getSelectedItem();
        if (selectedSong == null || selectedPlaylist == null) {
            showErrorDialog("Please select both a song from the playlist and a playlist");
            return;
        }
        try {
            playlistManager.removeSongFromPlaylist(selectedPlaylist, selectedSong);
            playlistSongsListView.getItems().remove(selectedSong);
            playlistsTableView.refresh();
            showInfoDialog("Success", "Song removed from playlist");
        } catch (Exception e) {
            showErrorDialog("Error removing song from playlist: " + e.getMessage());
        }
    }

    @FXML private void moveSongUp() {
        int selectedIndex = playlistSongsListView.getSelectionModel().getSelectedIndex();
        if (selectedIndex > 0) {
            Song song = playlistSongsListView.getItems().remove(selectedIndex);
            playlistSongsListView.getItems().add(selectedIndex - 1, song);
            playlistSongsListView.getSelectionModel().select(selectedIndex - 1);
        }
    }

    @FXML private void moveSongDown() {
        int selectedIndex = playlistSongsListView.getSelectionModel().getSelectedIndex();
        List<Song> items = playlistSongsListView.getItems();
        if (selectedIndex >= 0 && selectedIndex < items.size() - 1) {
            Song song = items.remove(selectedIndex);
            items.add(selectedIndex + 1, song);
            playlistSongsListView.getSelectionModel().select(selectedIndex + 1);
        }
    }

    @FXML private void closeApplication() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.dispose();
        }
        System.exit(0);
    }

    private void showErrorDialog(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showInfoDialog(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public void shutdown() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.dispose();
        }
    }
}