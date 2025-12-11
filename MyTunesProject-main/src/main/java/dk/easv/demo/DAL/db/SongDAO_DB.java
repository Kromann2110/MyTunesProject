package dk.easv.demo.DAL.db;

import dk.easv.demo.BE.Song;
import dk.easv.demo.DAL.ISongDataAccess;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Database implementation of song data access operations
 * Handles all CRUD operations for songs in the database
 */
public class SongDAO_DB implements ISongDataAccess {

    private DBConnector dbConnector;

    /**
     * Constructor - initializes database connection
     */
    public SongDAO_DB() {
        dbConnector = new DBConnector();
    }

    /**
     * Retrieves all songs from the database
     * @return List of all songs sorted by title
     * @throws SQLException if database error occurs
     */
    @Override
    public List<Song> getAllSongs() throws SQLException {
        List<Song> allSongs = new ArrayList<>();

        String sql = "SELECT * FROM songs ORDER BY title";

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

    /**
     * Creates a new song in the database
     * @param title Song title
     * @param artist Song artist
     * @param category Song category/genre
     * @param duration Duration in seconds
     * @param filePath Path to the song file
     * @return The created song with generated ID
     * @throws SQLException if database error occurs
     */
    @Override
    public Song createSong(String title, String artist, String category, int duration, String filePath) throws SQLException {
        String sql = "INSERT INTO songs (title, artist, category, duration, file_path) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = dbConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, title);
            stmt.setString(2, artist);
            stmt.setString(3, category);

            // Convert seconds to "MM:SS" format for database
            String durationStr = convertSecondsToDuration(duration);
            stmt.setString(4, durationStr);

            stmt.setString(5, filePath);
            stmt.executeUpdate();

            // Retrieve the auto-generated ID
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    int id = rs.getInt(1);
                    return new Song(id, title, artist, category, duration, filePath);
                }
            }
        }

        return null;
    }

    /**
     * Updates an existing song in the database
     * @param song The song to update
     * @throws SQLException if database error occurs
     */
    @Override
    public void updateSong(Song song) throws SQLException {
        String sql = "UPDATE songs SET title = ?, artist = ?, category = ?, duration = ?, file_path = ? WHERE id = ?";

        try (Connection conn = dbConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, song.getTitle());
            stmt.setString(2, song.getArtist());
            stmt.setString(3, song.getCategory());

            // Convert seconds to "MM:SS" format
            String durationStr = convertSecondsToDuration(song.getDuration());
            stmt.setString(4, durationStr);

            stmt.setString(5, song.getFilePath());
            stmt.setInt(6, song.getId());
            stmt.executeUpdate();
        }
    }

    /**
     * Deletes a song from the database
     * @param song The song to delete
     * @throws SQLException if database error occurs
     */
    @Override
    public void deleteSong(Song song) throws SQLException {
        String sql = "DELETE FROM songs WHERE id = ?";

        try (Connection conn = dbConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, song.getId());
            stmt.executeUpdate();
        }
    }

    /**
     * Retrieves a specific song by its ID
     * @param id The ID of the song to retrieve
     * @return The song, or null if not found
     * @throws SQLException if database error occurs
     */
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

    /**
     * Helper method to convert ResultSet row to Song object
     * @param rs ResultSet containing song data
     * @return Song object populated with data
     * @throws SQLException if database error occurs
     */
    private Song mapResultSetToSong(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        String title = rs.getString("title");
        String artist = rs.getString("artist");
        String category = rs.getString("category");
        String filePath = rs.getString("file_path");

        // IMPORTANT: Get duration as String from database
        String durationStr = rs.getString("duration");
        int durationSeconds = convertDurationToSeconds(durationStr);

        return new Song(id, title, artist, category, durationSeconds, filePath);
    }

    /**
     * Helper method to convert duration string (MM:SS) to seconds
     * @param duration String in format "MM:SS" or "H:MM:SS"
     * @return Duration in seconds
     */
    private int convertDurationToSeconds(String duration) {
        if (duration == null || duration.trim().isEmpty()) {
            return 0;
        }

        try {
            duration = duration.trim();
            String[] parts = duration.split(":");

            if (parts.length == 2) {
                // Format: "minutes:seconds" (e.g., "3:45")
                int minutes = Integer.parseInt(parts[0]);
                int seconds = Integer.parseInt(parts[1]);
                return (minutes * 60) + seconds;
            } else if (parts.length == 3) {
                // Format: "hours:minutes:seconds" (e.g., "1:23:45")
                int hours = Integer.parseInt(parts[0]);
                int minutes = Integer.parseInt(parts[1]);
                int seconds = Integer.parseInt(parts[2]);
                return (hours * 3600) + (minutes * 60) + seconds;
            } else {
                // Try to parse as plain seconds
                try {
                    return Integer.parseInt(duration);
                } catch (NumberFormatException e) {
                    return 0;
                }
            }
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * Helper method to convert seconds to "minutes:seconds" format
     * @param seconds Duration in seconds
     * @return Formatted string (e.g., "3:45")
     */
    private String convertSecondsToDuration(int seconds) {
        if (seconds <= 0) {
            return "0:00";
        }

        int minutes = seconds / 60;
        int remainingSeconds = seconds % 60;

        return String.format("%d:%02d", minutes, remainingSeconds);
    }

    /**
     * Searches for songs by title or artist
     * @param query Search query
     * @return List of matching songs
     * @throws SQLException if database error occurs
     */
    public List<Song> searchSongs(String query) throws SQLException {
        List<Song> results = new ArrayList<>();

        String sql = "SELECT * FROM songs WHERE title LIKE ? OR artist LIKE ? ORDER BY title";

        try (Connection conn = dbConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, "%" + query + "%");
            stmt.setString(2, "%" + query + "%");

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Song song = mapResultSetToSong(rs);
                    results.add(song);
                }
            }
        }

        return results;
    }

    /**
     * Gets songs by category/genre
     * @param category The category to filter by
     * @return List of songs in the category
     * @throws SQLException if database error occurs
     */
    public List<Song> getSongsByCategory(String category) throws SQLException {
        List<Song> results = new ArrayList<>();

        String sql = "SELECT * FROM songs WHERE category = ? ORDER BY title";

        try (Connection conn = dbConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, category);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Song song = mapResultSetToSong(rs);
                    results.add(song);
                }
            }
        }

        return results;
    }
}