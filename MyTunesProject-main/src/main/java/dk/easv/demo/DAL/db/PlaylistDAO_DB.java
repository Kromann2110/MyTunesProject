package dk.easv.demo.DAL.db;

// Business entities
import dk.easv.demo.BE.Playlist;
import dk.easv.demo.BE.Song;

// Data access
import dk.easv.demo.DAL.IPlaylistDataAccess;

// Java standard
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Database operations for playlists and playlist-song relationships
 */
public class PlaylistDAO_DB implements IPlaylistDataAccess {

    private DBConnector dbConnector;

    public PlaylistDAO_DB() {
        dbConnector = new DBConnector();
    }

    // Get all playlists from database with calculated song count and duration
    @Override
    public List<Playlist> getAllPlaylists() throws SQLException {
        List<Playlist> allPlaylists = new ArrayList<>();

        String sql = "SELECT p.* FROM playlists p ORDER BY p.name";

        try (Connection conn = DBConnector.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                int id = rs.getInt("id");
                String name = rs.getString("name");

                // Get song count and duration
                int songCount = getPlaylistSongCount(id);
                String totalDuration = calculatePlaylistDuration(id);

                Playlist playlist = new Playlist(id, name, songCount, totalDuration);
                allPlaylists.add(playlist);
            }
        }
        return allPlaylists;
    }

    // Get song count for a playlist
    private int getPlaylistSongCount(int playlistId) throws SQLException {
        String sql = "SELECT COUNT(*) as song_count FROM playlist_songs WHERE playlist_id = ?";

        try (Connection conn = dbConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, playlistId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("song_count");
                }
            }
        }
        return 0;
    }

    // Calculate total duration for a playlist
    public String calculatePlaylistDuration(int playlistId) throws SQLException {
        String sql = "SELECT SUM(s.duration) as total_seconds FROM songs s " +
                "JOIN playlist_songs ps ON s.id = ps.song_id " +
                "WHERE ps.playlist_id = ?";

        try (Connection conn = dbConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, playlistId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    int totalSeconds = rs.getInt("total_seconds");
                    if (rs.wasNull()) {
                        return "00:00";
                    }

                    // Convert to formatted time
                    return Playlist.formatDurationFromSeconds(totalSeconds);
                }
            }
        }
        return "00:00";
    }

    // Create new playlist and return with generated ID
    @Override
    public Playlist createPlaylist(String name) throws SQLException {
        String sql = "INSERT INTO playlists (name) VALUES (?)";

        try (Connection conn = DBConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, name);
            stmt.executeUpdate();

            // Get auto-generated ID
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    int id = rs.getInt(1);
                    return new Playlist(id, name, 0, "00:00");
                }
            }
        }
        return null;
    }

    // Update playlist name
    @Override
    public void updatePlaylist(Playlist playlist) throws SQLException {
        String sql = "UPDATE playlists SET name = ? WHERE id = ?";

        try (Connection conn = DBConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, playlist.getName());
            stmt.setInt(2, playlist.getId());
            stmt.executeUpdate();
        }
    }

    // Delete playlist and its songs
    @Override
    public void deletePlaylist(Playlist playlist) throws SQLException {
        // First delete song relationships
        String deleteSongsSql = "DELETE FROM playlist_songs WHERE playlist_id = ?";
        try (Connection conn = DBConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(deleteSongsSql)) {
            stmt.setInt(1, playlist.getId());
            stmt.executeUpdate();
        }

        // Then delete playlist
        String deletePlaylistSql = "DELETE FROM playlists WHERE id = ?";
        try (Connection conn = DBConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(deletePlaylistSql)) {
            stmt.setInt(1, playlist.getId());
            stmt.executeUpdate();
        }
    }

    // Add song to playlist at specific position
    @Override
    public void addSongToPlaylist(int playlistId, int songId, int position) throws SQLException {
        String sql = "INSERT INTO playlist_songs (playlist_id, song_id, position) VALUES (?, ?, ?)";

        try (Connection conn = DBConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, playlistId);
            stmt.setInt(2, songId);
            stmt.setInt(3, position);
            stmt.executeUpdate();
        }
    }

    // Remove song from playlist
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

    // Get all songs in playlist ordered by position
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
                    int duration = rs.getInt("duration");
                    String filePath = rs.getString("file_path");

                    Song song = new Song(id, title, artist, category, duration, filePath);
                    songs.add(song);
                }
            }
        }
        return songs;
    }

    // Get position of song in playlist
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
        return -1; // Song not found
    }

    // Update song position in playlist
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

    // Swap positions of two songs
    public void swapSongPositions(int playlistId, int songId1, int songId2) throws SQLException {
        int position1 = getSongPosition(playlistId, songId1);
        int position2 = getSongPosition(playlistId, songId2);

        if (position1 != -1 && position2 != -1) {
            // Swap positions
            updateSongPosition(playlistId, songId1, position2);
            updateSongPosition(playlistId, songId2, position1);
        }
    }

    // Move song up in playlist
    @Override
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

    // Move song down in playlist
    @Override
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