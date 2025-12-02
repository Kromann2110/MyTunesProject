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

    // Get all playlists with calculated song counts and durations
    public List<Playlist> getAllPlaylists() {
        try {
            return daoPlaylist.getAllPlaylists();
        } catch (SQLException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    // Get songs in specific playlist and update playlist duration
    public List<Song> getSongsInPlaylist(Playlist playlist) {
        try {
            List<Song> songs = daoPlaylist.getSongsInPlaylist(playlist.getId());

            // Update playlist duration and song count
            if (playlist != null) {
                playlist.calculateAndSetDuration(songs);
            }

            return songs;
        } catch (SQLException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    // Get songs in specific playlist without updating playlist object
    public List<Song> getSongsInPlaylist(int playlistId) {
        try {
            return daoPlaylist.getSongsInPlaylist(playlistId);
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
            List<Song> currentSongs = getSongsInPlaylist(playlist.getId());
            int position = currentSongs.size(); // Add at end
            daoPlaylist.addSongToPlaylist(playlist.getId(), song.getId(), position);

            // Update playlist duration after adding song
            currentSongs.add(song);
            playlist.calculateAndSetDuration(currentSongs);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Remove song from playlist
    public void removeSongFromPlaylist(Playlist playlist, Song song) {
        try {
            daoPlaylist.removeSongFromPlaylist(playlist.getId(), song.getId());

            // Update playlist duration after removing song
            List<Song> currentSongs = getSongsInPlaylist(playlist.getId());
            playlist.calculateAndSetDuration(currentSongs);
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
}