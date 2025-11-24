package dk.easv.demo.BE;

/**
 * Represents a music track with metadata and file info
 */
public class Song {
    private int id;
    private String title;
    private String artist;
    private String category;
    private int duration; // Duration in seconds
    private String filePath;

    // For creating new songs (no ID yet)
    public Song(String title, String artist, String category, int duration, String filePath) {
        this.title = title;
        this.artist = artist;
        this.category = category;
        this.duration = duration;
        this.filePath = filePath;
    }

    // For songs loaded from database (with ID)
    public Song(int id, String title, String artist, String category, int duration, String filePath) {
        this.id = id;
        this.title = title;
        this.artist = artist;
        this.category = category;
        this.duration = duration;
        this.filePath = filePath;
    }

    // Default constructor
    public Song() {
    }

    // Getters and setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getArtist() { return artist; }
    public void setArtist(String artist) { this.artist = artist; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public int getDuration() { return duration; }
    public void setDuration(int duration) { this.duration = duration; }

    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }

    // Convert seconds to MM:SS format
    public String getFormattedDuration() {
        int minutes = duration / 60;
        int seconds = duration % 60;
        return String.format("%d:%02d", minutes, seconds);
    }

    // Convert seconds to HH:MM:SS format if needed
    public String getFormattedDurationWithHours() {
        int hours = duration / 3600;
        int minutes = (duration % 3600) / 60;
        int seconds = duration % 60;

        if (hours > 0) {
            return String.format("%d:%02d:%02d", hours, minutes, seconds);
        } else {
            return String.format("%d:%02d", minutes, seconds);
        }
    }

    // Display as "Artist - Duration" in lists
    @Override
    public String toString() {
        return artist + " - " + getFormattedDuration();
    }

    // Compare songs by ID
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Song song = (Song) o;
        return id == song.id;
    }

    // Hash by ID for collections
    @Override
    public int hashCode() {
        return id;
    }
}