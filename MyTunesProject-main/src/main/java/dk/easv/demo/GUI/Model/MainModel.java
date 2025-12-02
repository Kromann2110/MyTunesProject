package dk.easv.demo.GUI.Model;

// Business entities
import dk.easv.demo.BE.Playlist;
import dk.easv.demo.BE.Song;

// Business logic
import dk.easv.demo.BLL.MusicManager;
import dk.easv.demo.BLL.PlaylistManager;

// Java standard
import java.sql.SQLException;
import java.util.List;

/**
 * Main model class - Facade pattern for business logic
 * Simplifies interface to multiple manager classes
 */
public class MainModel {
    private MusicManager musicManager;
    private PlaylistManager playlistManager;

    // Initialize managers
    public MainModel() {
        musicManager = new MusicManager();
        playlistManager = new PlaylistManager();
    }

    // Get all songs
    public List<Song> getAllSongs() throws SQLException {
        return musicManager.getAllSongs();
    }

    // Get all playlists
    public List<Playlist> getAllPlaylists() throws SQLException {
        return playlistManager.getAllPlaylists();
    }

    // Create new playlist
    public Playlist createPlaylist(String name) throws SQLException {
        return playlistManager.createPlaylist(name);
    }

    // Add song to playlist
    public void addSongToPlaylist(Playlist playlist, Song song) throws SQLException {
        playlistManager.addSongToPlaylist(playlist, song);
    }

    // Get songs in playlist
    public List<Song> getSongsInPlaylist(Playlist playlist) throws SQLException {
        return playlistManager.getSongsInPlaylist(playlist);
    }
}