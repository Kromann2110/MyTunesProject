package dk.easv.demo.DAL;

// Business entities
import dk.easv.demo.BE.Playlist;
import dk.easv.demo.BE.Song;

// Java standard
import java.sql.SQLException;
import java.util.List;
/**
 * Interface for playlist data access operations
 */
public interface IPlaylistDataAccess {
    // Get all playlists
    List<Playlist> getAllPlaylists() throws SQLException;

    // Create new playlist
    Playlist createPlaylist(String name) throws SQLException;

    // Update playlist name
    void updatePlaylist(Playlist playlist) throws SQLException;

    // Delete playlist and its songs
    void deletePlaylist(Playlist playlist) throws SQLException;

    // Add song to playlist at position
    void addSongToPlaylist(int playlistId, int songId, int position) throws SQLException;

    // Remove song from playlist
    void removeSongFromPlaylist(int playlistId, int songId) throws SQLException;

    // Get songs in playlist ordered by position
    List<Song> getSongsInPlaylist(int playlistId) throws SQLException;

    // Move song up in playlist
    void moveSongUp(int playlistId, int songId) throws SQLException;

    // Move song down in playlist
    void moveSongDown(int playlistId, int songId) throws SQLException;
}