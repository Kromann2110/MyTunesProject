package dk.easv.demo.DAL.db;

// Business entities
import dk.easv.demo.BE.Song;

// Data access
import dk.easv.demo.DAL.ISongDataAccess;

// Java standard
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
/**
 * Database operations for songs
 */
public class SongDAO_DB implements ISongDataAccess {
    private DBConnector dbConnector;

    public SongDAO_DB() {
        dbConnector = new DBConnector();
    }

    // Get all songs from database
    @Override
    public List<Song> getAllSongs() throws SQLException {
        List<Song> allSongs = new ArrayList<>();
        String sql = "SELECT * FROM songs";

        try (Connection conn = dbConnector.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Song song = mapResultSetToSong(rs);
                allSongs.add(song);
            }
        }
        return allSongs;
    }

    // Create new song and return with generated ID
    @Override
    public Song createSong(String title, String artist, String category, int duration, String filePath) throws SQLException {
        String sql = "INSERT INTO songs (title, artist, category, duration, file_path) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = dbConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, title);
            stmt.setString(2, artist);
            stmt.setString(3, category);
            stmt.setInt(4, duration);
            stmt.setString(5, filePath);
            stmt.executeUpdate();

            // Get auto-generated ID
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    int id = rs.getInt(1);
                    return new Song(id, title, artist, category, duration, filePath);
                }
            }
        }
        return null;
    }

    // Update existing song
    @Override
    public void updateSong(Song song) throws SQLException {
        String sql = "UPDATE songs SET title = ?, artist = ?, category = ?, duration = ?, file_path = ? WHERE id = ?";

        try (Connection conn = dbConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, song.getTitle());
            stmt.setString(2, song.getArtist());
            stmt.setString(3, song.getCategory());
            stmt.setInt(4, song.getDuration());
            stmt.setString(5, song.getFilePath());
            stmt.setInt(6, song.getId());
            stmt.executeUpdate();
        }
    }

    // Delete song from database
    @Override
    public void deleteSong(Song song) throws SQLException {
        String sql = "DELETE FROM songs WHERE id = ?";

        try (Connection conn = dbConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, song.getId());
            stmt.executeUpdate();
        }
    }

    // Find song by ID
    @Override
    public Song getSongById(int id) throws SQLException {
        String sql = "SELECT * FROM songs WHERE id = ?";

        try (Connection conn = dbConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToSong(rs);
                }
            }
        }
        return null;
    }

    // Convert database result set to Song object
    private Song mapResultSetToSong(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        String title = rs.getString("title");
        String artist = rs.getString("artist");
        String category = rs.getString("category");
        int duration = rs.getInt("duration");
        String filePath = rs.getString("file_path");

        return new Song(id, title, artist, category, duration, filePath);
    }
}