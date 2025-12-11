package dk.easv.demo.DAL.db;

import dk.easv.demo.BE.Playlist;
import dk.easv.demo.BE.Song;
import dk.easv.demo.DAL.IPlaylistDataAccess;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Database operations for playlists and playlist-song relationships
 * This class handles all database interactions for playlists including
 * creating, reading, updating, deleting playlists and managing songs within them.
 */
public class PlaylistDAO_DB implements IPlaylistDataAccess {

    private DBConnector dbConnector;

    /**
     * Constructor - initializes database connection
     */
    public PlaylistDAO_DB() {
        dbConnector = new DBConnector();
    }

    /**
     * Retrieves all playlists from the database
     * @return List of all playlists
     * @throws SQLException if database error occurs
     */
    @Override
    public List<Playlist> getAllPlaylists() throws SQLException {
        List<Playlist> allPlaylists = new ArrayList<>();
        String sql = "SELECT * FROM playlists ORDER BY name";

        try (Connection conn = dbConnector.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                int id = rs.getInt("id");
                String name = rs.getString("name");
                Playlist playlist = new Playlist(id, name);
                allPlaylists.add(playlist);
            }
        }
        return allPlaylists;
    }

    /**
     * Creates a new playlist in the database
     * @param name The name of the new playlist
     * @return The created playlist with generated ID
     * @throws SQLException if database error occurs
     */
    @Override
    public Playlist createPlaylist(String name) throws SQLException {
        String sql = "INSERT INTO playlists (name) VALUES (?)";

        try (Connection conn = dbConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, name);
            stmt.executeUpdate();

            // Retrieve the auto-generated ID
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    int id = rs.getInt(1);
                    return new Playlist(id, name);
                }
            }
        }
        return null;
    }

    /**
     * Updates the name of an existing playlist
     * @param playlist The playlist to update
     * @throws SQLException if database error occurs
     */
    @Override
    public void updatePlaylist(Playlist playlist) throws SQLException {
        String sql = "UPDATE playlists SET name = ? WHERE id = ?";

        try (Connection conn = dbConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, playlist.getName());
            stmt.setInt(2, playlist.getId());
            stmt.executeUpdate();
        }
    }

    /**
     * Deletes a playlist and all its song associations
     * @param playlist The playlist to delete
     * @throws SQLException if database error occurs
     */
    @Override
    public void deletePlaylist(Playlist playlist) throws SQLException {
        // First delete all song relationships (cascade would handle this, but being explicit)
        String deleteSongsSql = "DELETE FROM playlist_songs WHERE playlist_id = ?";
        try (Connection conn = dbConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(deleteSongsSql)) {
            stmt.setInt(1, playlist.getId());
            stmt.executeUpdate();
        }

        // Then delete the playlist itself
        String deletePlaylistSql = "DELETE FROM playlists WHERE id = ?";
        try (Connection conn = dbConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(deletePlaylistSql)) {
            stmt.setInt(1, playlist.getId());
            stmt.executeUpdate();
        }
    }

    /**
     * Adds a song to a playlist at a specific position
     * @param playlistId ID of the playlist
     * @param songId ID of the song to add
     * @param position Position in the playlist (0-based)
     * @throws SQLException if database error occurs
     */
    @Override
    public void addSongToPlaylist(int playlistId, int songId, int position) throws SQLException {
        String sql = "INSERT INTO playlist_songs (playlist_id, song_id, position) VALUES (?, ?, ?)";

        try (Connection conn = dbConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, playlistId);
            stmt.setInt(2, songId);
            stmt.setInt(3, position);
            stmt.executeUpdate();
        }
    }

    /**
     * Removes a song from a playlist
     * @param playlistId ID of the playlist
     * @param songId ID of the song to remove
     * @throws SQLException if database error occurs
     */
    @Override
    public void removeSongFromPlaylist(int playlistId, int songId) throws SQLException {
        String sql = "DELETE FROM playlist_songs WHERE playlist_id = ? AND song_id = ?";

        try (Connection conn = dbConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, playlistId);
            stmt.setInt(2, songId);
            stmt.executeUpdate();
        }
    }

    /**
     * Retrieves all songs in a specific playlist ordered by their position
     * @param playlistId ID of the playlist
     * @return List of songs in the playlist
     * @throws SQLException if database error occurs
     */
    @Override
    public List<Song> getSongsInPlaylist(int playlistId) throws SQLException {
        List<Song> songs = new ArrayList<>();

        String sql = "SELECT s.* FROM songs s " +
                "JOIN playlist_songs ps ON s.id = ps.song_id " +
                "WHERE ps.playlist_id = ? " +
                "ORDER BY ps.position";

        try (Connection conn = dbConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, playlistId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    int id = rs.getInt("id");
                    String title = rs.getString("title");
                    String artist = rs.getString("artist");
                    String category = rs.getString("category");

                    // IMPORTANT: Duration is stored as string "MM:SS" in database
                    // Convert to seconds for the Song object
                    String durationStr = rs.getString("duration");
                    int durationSeconds = convertDurationToSeconds(durationStr);

                    String filePath = rs.getString("file_path");

                    Song song = new Song(id, title, artist, category, durationSeconds, filePath);
                    songs.add(song);
                }
            }
        }
        return songs;
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
     * Gets the position of a specific song in a playlist
     * @param playlistId ID of the playlist
     * @param songId ID of the song
     * @return Position of the song, or -1 if not found
     * @throws SQLException if database error occurs
     */
    public int getSongPosition(int playlistId, int songId) throws SQLException {
        String sql = "SELECT position FROM playlist_songs WHERE playlist_id = ? AND song_id = ?";

        try (Connection conn = dbConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, playlistId);
            stmt.setInt(2, songId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("position");
                }
            }
        }
        return -1; // Song not found in playlist
    }

    /**
     * Updates the position of a song within a playlist
     * @param playlistId ID of the playlist
     * @param songId ID of the song to move
     * @param newPosition New position for the song
     * @throws SQLException if database error occurs
     */
    public void updateSongPosition(int playlistId, int songId, int newPosition) throws SQLException {
        String sql = "UPDATE playlist_songs SET position = ? WHERE playlist_id = ? AND song_id = ?";

        try (Connection conn = dbConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, newPosition);
            stmt.setInt(2, playlistId);
            stmt.setInt(3, songId);
            stmt.executeUpdate();
        }
    }

    /**
     * Swaps the positions of two songs in a playlist
     * @param playlistId ID of the playlist
     * @param songId1 First song ID
     * @param songId2 Second song ID
     * @throws SQLException if database error occurs
     */
    public void swapSongPositions(int playlistId, int songId1, int songId2) throws SQLException {
        int position1 = getSongPosition(playlistId, songId1);
        int position2 = getSongPosition(playlistId, songId2);

        if (position1 != -1 && position2 != -1) {
            updateSongPosition(playlistId, songId1, position2);
            updateSongPosition(playlistId, songId2, position1);
        }
    }

    /**
     * Moves a song up one position in the playlist
     * @param playlistId ID of the playlist
     * @param songId ID of the song to move up
     * @throws SQLException if database error occurs
     */
    public void moveSongUp(int playlistId, int songId) throws SQLException {
        List<Song> songs = getSongsInPlaylist(playlistId);

        // Find current song position
        int currentIndex = -1;
        for (int i = 0; i < songs.size(); i++) {
            if (songs.get(i).getId() == songId) {
                currentIndex = i;
                break;
            }
        }

        // Swap with song above if not at top
        if (currentIndex > 0) {
            int songIdAbove = songs.get(currentIndex - 1).getId();
            swapSongPositions(playlistId, songId, songIdAbove);
        }
    }

    /**
     * Moves a song down one position in the playlist
     * @param playlistId ID of the playlist
     * @param songId ID of the song to move down
     * @throws SQLException if database error occurs
     */
    public void moveSongDown(int playlistId, int songId) throws SQLException {
        List<Song> songs = getSongsInPlaylist(playlistId);

        // Find current song position
        int currentIndex = -1;
        for (int i = 0; i < songs.size(); i++) {
            if (songs.get(i).getId() == songId) {
                currentIndex = i;
                break;
            }
        }

        // Swap with song below if not at bottom
        if (currentIndex >= 0 && currentIndex < songs.size() - 1) {
            int songIdBelow = songs.get(currentIndex + 1).getId();
            swapSongPositions(playlistId, songId, songIdBelow);
        }
    }
}