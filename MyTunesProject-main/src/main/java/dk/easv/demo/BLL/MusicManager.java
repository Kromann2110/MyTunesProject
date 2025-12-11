package dk.easv.demo.BLL;

import dk.easv.demo.BE.Song;
import dk.easv.demo.DAL.ISongDataAccess;
import dk.easv.demo.DAL.db.SongDAO_DB;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Business logic layer for music operations.
 * Handles all song-related business logic and acts as a bridge
 * between the UI and data access layer.
 */
public class MusicManager {
    private final ISongDataAccess songDAO;

    public MusicManager() {
        this.songDAO = new SongDAO_DB();
    }

    public MusicManager(ISongDataAccess songDAO) {
        this.songDAO = songDAO;
    }

    public List<Song> getAllSongs() {
        try {
            return songDAO.getAllSongs();
        } catch (SQLException e) {
            System.err.println("Failed to retrieve songs: " + e.getMessage());
            throw new RuntimeException("Database error while loading songs", e);
        }
    }

    public Song createSong(String title, String artist, String category, int duration, String filePath) {
        try {
            return songDAO.createSong(title, artist, category, duration, filePath);
        } catch (SQLException e) {
            System.err.println("Failed to create song '" + title + "': " + e.getMessage());
            throw new RuntimeException("Database error while creating song", e);
        }
    }

    public void updateSong(Song song) {
        try {
            songDAO.updateSong(song);
        } catch (SQLException e) {
            System.err.println("Failed to update song ID " + song.getId() + ": " + e.getMessage());
            throw new RuntimeException("Database error while updating song", e);
        }
    }

    public void deleteSong(Song song) {
        try {
            songDAO.deleteSong(song);
        } catch (SQLException e) {
            System.err.println("Failed to delete song ID " + song.getId() + ": " + e.getMessage());
            throw new RuntimeException("Database error while deleting song", e);
        }
    }

    public Song getSongById(int id) {
        try {
            return songDAO.getSongById(id);
        } catch (SQLException e) {
            System.err.println("Failed to retrieve song ID " + id + ": " + e.getMessage());
            throw new RuntimeException("Database error while fetching song", e);
        }
    }

    public List<Song> searchSongs(String query) {
        try {
            if (songDAO instanceof SongDAO_DB) {
                return ((SongDAO_DB) songDAO).searchSongs(query);
            } else {
                return getAllSongs().stream()
                        .filter(song -> matchesQuery(song, query))
                        .collect(Collectors.toList());
            }
        } catch (Exception e) {
            System.err.println("Search failed for query '" + query + "': " + e.getMessage());
            return new ArrayList<>();
        }
    }

    private boolean matchesQuery(Song song, String query) {
        String lowerQuery = query.toLowerCase();
        return song.getTitle().toLowerCase().contains(lowerQuery) ||
                song.getArtist().toLowerCase().contains(lowerQuery);
    }

    public int getSongCount() {
        try {
            return getAllSongs().size();
        } catch (Exception e) {
            System.err.println("Failed to get song count: " + e.getMessage());
            return 0;
        }
    }

    public List<Song> getSongsByCategory(String category) {
        try {
            if (songDAO instanceof SongDAO_DB) {
                return ((SongDAO_DB) songDAO).getSongsByCategory(category);
            } else {
                return getAllSongs().stream()
                        .filter(song -> category.equalsIgnoreCase(song.getCategory()))
                        .collect(Collectors.toList());
            }
        } catch (Exception e) {
            System.err.println("Failed to get songs for category '" + category + "': " + e.getMessage());
            return new ArrayList<>();
        }
    }

    public List<String> getAllCategories() {
        try {
            return getAllSongs().stream()
                    .map(Song::getCategory)
                    .filter(category -> category != null && !category.trim().isEmpty())
                    .distinct()
                    .sorted()
                    .collect(Collectors.toList());
        } catch (Exception e) {
            System.err.println("Failed to retrieve categories: " + e.getMessage());
            return new ArrayList<>();
        }
    }
}
