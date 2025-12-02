    package dk.easv.demo.DAL;

    // Business entities
    import dk.easv.demo.BE.Song;

    // Java standard
    import java.sql.SQLException;
    import java.util.List;
    /**
     * Interface for song data access operations
     */
    public interface ISongDataAccess {
        // Get all songs
        List<Song> getAllSongs() throws SQLException;

        // Create new song
        Song createSong(String title, String artist, String category, int duration, String filePath) throws SQLException;

        // Update existing song
        void updateSong(Song song) throws SQLException;

        // Delete song
        void deleteSong(Song song) throws SQLException;

        // Find song by ID
        Song getSongById(int id) throws SQLException;
    }