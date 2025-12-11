package dk.easv.demo.BLL;

import dk.easv.demo.BE.Playlist;
import dk.easv.demo.BE.Song;
import dk.easv.demo.DAL.db.PlaylistDAO_DB;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Manages playlist operations and song ordering.
 * Handles all playlist-related business logic.
 */
public class PlaylistManager {
    private final PlaylistDAO_DB daoPlaylist;

    public PlaylistManager() {
        this.daoPlaylist = new PlaylistDAO_DB();
    }

    public List<Playlist> getAllPlaylists() {
        try {
            return daoPlaylist.getAllPlaylists();
        } catch (SQLException e) {
            System.err.println("Failed to retrieve playlists: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public List<Song> getSongsInPlaylist(Playlist playlist) {
        try {
            return daoPlaylist.getSongsInPlaylist(playlist.getId());
        } catch (SQLException e) {
            System.err.println("Failed to get songs for playlist '" + playlist.getName() + "': " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public void updatePlaylist(Playlist playlist) {
        try {
            daoPlaylist.updatePlaylist(playlist);
        } catch (SQLException e) {
            System.err.println("Failed to update playlist '" + playlist.getName() + "': " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Database error while updating playlist", e);
        }
    }

    public Playlist createPlaylist(String name) {
        try {
            return daoPlaylist.createPlaylist(name);
        } catch (SQLException e) {
            System.err.println("Failed to create playlist '" + name + "': " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Database error while creating playlist", e);
        }
    }

    public void addSongToPlaylist(Playlist playlist, Song song) {
        try {
            List<Song> currentSongs = getSongsInPlaylist(playlist);
            int position = currentSongs.size();
            daoPlaylist.addSongToPlaylist(playlist.getId(), song.getId(), position);
        } catch (SQLException e) {
            System.err.println("Failed to add song to playlist: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Database error while adding song to playlist", e);
        }
    }

    public void removeSongFromPlaylist(Playlist playlist, Song song) {
        try {
            daoPlaylist.removeSongFromPlaylist(playlist.getId(), song.getId());
        } catch (SQLException e) {
            System.err.println("Failed to remove song from playlist: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Database error while removing song from playlist", e);
        }
    }

    public void deletePlaylist(Playlist playlist) {
        try {
            daoPlaylist.deletePlaylist(playlist);
        } catch (SQLException e) {
            System.err.println("Failed to delete playlist '" + playlist.getName() + "': " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Database error while deleting playlist", e);
        }
    }

    public void moveSongUp(Playlist playlist, Song song) {
        try {
            daoPlaylist.moveSongUp(playlist.getId(), song.getId());
        } catch (SQLException e) {
            System.err.println("Failed to move song up: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Database error while reordering songs", e);
        }
    }

    public void moveSongDown(Playlist playlist, Song song) {
        try {
            daoPlaylist.moveSongDown(playlist.getId(), song.getId());
        } catch (SQLException e) {
            System.err.println("Failed to move song down: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Database error while reordering songs", e);
        }
    }

    public String calculatePlaylistTotalDuration(Playlist playlist) {
        try {
            List<Song> songs = getSongsInPlaylist(playlist);
            if (songs == null || songs.isEmpty()) {
                return "00:00";
            }

            int totalSeconds = songs.stream()
                    .mapToInt(Song::getDuration)
                    .sum();

            return formatSecondsToDuration(totalSeconds);
        } catch (Exception e) {
            System.err.println("Failed to calculate playlist duration: " + e.getMessage());
            e.printStackTrace();
            return "00:00";
        }
    }

    private String formatSecondsToDuration(int totalSeconds) {
        if (totalSeconds <= 0) {
            return "00:00";
        }

        int hours = totalSeconds / 3600;
        int minutes = (totalSeconds % 3600) / 60;
        int seconds = totalSeconds % 60;

        return hours > 0
                ? String.format("%d:%02d:%02d", hours, minutes, seconds)
                : String.format("%d:%02d", minutes, seconds);
    }
}