package dk.easv.demo.BE;

import javafx.beans.property.*;
import java.util.List; // ADD THIS IMPORT

/**
 * Represents a music playlist with basic info and calculated properties
 */
public class Playlist {
    private final IntegerProperty id = new SimpleIntegerProperty();
    private final StringProperty name = new SimpleStringProperty();
    private final IntegerProperty songCount = new SimpleIntegerProperty();
    private final StringProperty totalDuration = new SimpleStringProperty();

    // Creates new playlist with default values
    public Playlist(int id, String name) {
        setId(id);
        setName(name);
        setSongCount(0);
        setTotalDuration("00:00");
    }

    // Creates playlist with all properties
    public Playlist(int id, String name, int songCount, String totalDuration) {
        setId(id);
        setName(name);
        setSongCount(songCount);
        setTotalDuration(totalDuration);
    }

    // Default constructor
    public Playlist() {
        this(0, "New Playlist", 0, "00:00");
    }

    // ===== Property Getters (for JavaFX binding) =====
    public IntegerProperty idProperty() { return id; }
    public StringProperty nameProperty() { return name; }
    public IntegerProperty songCountProperty() { return songCount; }
    public StringProperty totalDurationProperty() { return totalDuration; }

    // ===== Regular Getters and Setters =====
    public int getId() { return id.get(); }
    public void setId(int id) { this.id.set(id); }

    public String getName() { return name.get(); }
    public void setName(String name) { this.name.set(name); }

    public int getSongCount() { return songCount.get(); }
    public void setSongCount(int songCount) { this.songCount.set(songCount); }

    public String getTotalDuration() { return totalDuration.get(); }
    public void setTotalDuration(String totalDuration) { this.totalDuration.set(totalDuration); }

    /**
     * Calculate and set duration based on list of songs
     */
    public void calculateAndSetDuration(List<Song> songs) {
        int totalSeconds = 0;
        for (Song song : songs) {
            totalSeconds += song.getDuration(); // Song duration is in seconds
        }

        // Convert seconds to HH:MM:SS or MM:SS format
        int hours = totalSeconds / 3600;
        int minutes = (totalSeconds % 3600) / 60;
        int seconds = totalSeconds % 60;

        if (hours > 0) {
            setTotalDuration(String.format("%d:%02d:%02d", hours, minutes, seconds));
        } else {
            setTotalDuration(String.format("%d:%02d", minutes, seconds));
        }

        // Update song count
        setSongCount(songs.size());
    }

    // Convert seconds to formatted time string
    public static String formatDurationFromSeconds(int totalSeconds) {
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

    // Display playlist name in UI components
    @Override
    public String toString() {
        return getName() + " (" + getSongCount() + " songs)";
    }
}