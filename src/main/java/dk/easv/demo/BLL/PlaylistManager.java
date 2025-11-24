package dk.easv.demo.BLL;

// Business entities
import dk.easv.demo.BE.Playlist;
import dk.easv.demo.BE.Song;

// Data access
import dk.easv.demo.DAL.db.PlaylistDAO_DB;

// Java standard
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Manages playlist operations and song ordering
 */
public class PlaylistManager {
    private PlaylistDAO_DB daoPlaylist;

    public PlaylistManager() {
        daoPlaylist = new PlaylistDAO_DB();
    }

    // Get all playlists
    public List<Playlist> getAllPlaylists() {
        try {
            return daoPlaylist.getAllPlaylists();
        } catch (SQLException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    // Get songs in specific playlist
    public List<Song> getSongsInPlaylist(Playlist playlist) {
        try {
            return daoPlaylist.getSongsInPlaylist(playlist.getId());
        } catch (SQLException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    // Update playlist name
    public void updatePlaylist(Playlist playlist) {
        try {
            daoPlaylist.updatePlaylist(playlist);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Create new playlist
    public Playlist createPlaylist(String name) {
        try {
            return daoPlaylist.createPlaylist(name);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    // Add song to end of playlist
    public void addSongToPlaylist(Playlist playlist, Song song) {
        try {
            List<Song> currentSongs = getSongsInPlaylist(playlist);
            int position = currentSongs.size(); // Add at end
            daoPlaylist.addSongToPlaylist(playlist.getId(), song.getId(), position);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Remove song from playlist
    public void removeSongFromPlaylist(Playlist playlist, Song song) {
        try {
            daoPlaylist.removeSongFromPlaylist(playlist.getId(), song.getId());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Delete playlist and its songs
    public void deletePlaylist(Playlist playlist) {
        try {
            daoPlaylist.deletePlaylist(playlist);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Move song up in playlist order
    public void moveSongUp(Playlist playlist, Song song) {
        try {
            daoPlaylist.moveSongUp(playlist.getId(), song.getId());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Move song down in playlist order
    public void moveSongDown(Playlist playlist, Song song) {
        try {
            daoPlaylist.moveSongDown(playlist.getId(), song.getId());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Calculate total duration of playlist
    public String calculatePlaylistTotalDuration(Playlist playlist) {
        try {
            List<Song> songs = getSongsInPlaylist(playlist);
            if (songs == null || songs.isEmpty()) {
                return "00:00";
            }

            int totalSeconds = 0;
            for (Song song : songs) {
                totalSeconds += song.getDuration();
            }

            return formatSecondsToDuration(totalSeconds);
        } catch (Exception e) {
            e.printStackTrace();
            return "00:00";
        }
    }

    // Format seconds to readable time
    private String formatSecondsToDuration(int totalSeconds) {
        if (totalSeconds <= 0) {
            return "00:00";
        }

        int hours = totalSeconds / 3600;
        int minutes = (totalSeconds % 3600) / 60;
        int seconds = totalSeconds % 60;

        if (hours > 0) {
            return String.format("%d:%02d:%02d", hours, minutes, seconds);
        } else {
            return String.format("%d:%02d", minutes, seconds);
        }
    }
}