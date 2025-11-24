package dk.easv.demo.BLL;

// Business entities
import dk.easv.demo.BE.Song;

// Data access
import dk.easv.demo.DAL.db.SongDAO_DB;

// Java standard
import java.sql.SQLException;
import java.util.List;
/**
 * Alternative song manager with runtime exceptions
 */
public class SongManager {
    private SongDAO_DB songDAO;

    public SongManager() {
        songDAO = new SongDAO_DB();
    }

    // Get all songs (throws runtime exception)
    public List<Song> getAllSongs() {
        try {
            return songDAO.getAllSongs();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Error loading songs from database", e);
        }
    }

    // Create new song (throws runtime exception)
    public Song createSong(String title, String artist, String category, int duration, String filePath) {
        try {
            return songDAO.createSong(title, artist, category, duration, filePath);
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Error creating song", e);
        }
    }

    // Update song (throws runtime exception)
    public void updateSong(Song song) {
        try {
            songDAO.updateSong(song);
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Error updating song", e);
        }
    }

    // Delete song (throws runtime exception)
    public void deleteSong(Song song) {
        try {
            songDAO.deleteSong(song);
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Error deleting song", e);
        }
    }

    // Get song by ID (throws runtime exception)
    public Song getSongById(int id) {
        try {
            return songDAO.getSongById(id);
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Error getting song by ID", e);
        }
    }
}