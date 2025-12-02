package dk.easv.demo.BLL;

// Business entities
import dk.easv.demo.BE.Song;

// Data access
import dk.easv.demo.DAL.ISongDataAccess;
import dk.easv.demo.DAL.db.SongDAO_DB;

// Java standard
import java.sql.SQLException;
import java.util.List;

/**
 * Handles song business logic and database operations
 */
public class MusicManager {
    private ISongDataAccess songDAO;

    // Initialize with database implementation
    public MusicManager() {
        songDAO = new SongDAO_DB();
    }

    // Get all songs from database
    public List<Song> getAllSongs() throws SQLException {
        return songDAO.getAllSongs();
    }

    // Create new song and return with generated ID
    public Song createSong(String title, String artist, String category, int duration, String filePath) throws SQLException {
        return songDAO.createSong(title, artist, category, duration, filePath);
    }

    // Update existing song
    public void updateSong(Song song) throws SQLException {
        songDAO.updateSong(song);
    }

    // Delete song from database
    public void deleteSong(Song song) throws SQLException {
        songDAO.deleteSong(song);
    }

    // Find song by ID
    public Song getSongById(int songId) throws SQLException {
        return songDAO.getSongById(songId);
    }
}