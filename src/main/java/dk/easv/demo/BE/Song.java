package dk.easv.demo.BE;

import javafx.beans.property.*;

/**
 * Represents a music track with metadata and file info
 */
public class Song {
    private final IntegerProperty id = new SimpleIntegerProperty();
    private final StringProperty title = new SimpleStringProperty();
    private final StringProperty artist = new SimpleStringProperty();
    private final StringProperty category = new SimpleStringProperty();
    private final IntegerProperty duration = new SimpleIntegerProperty(); // Duration in seconds
    private final StringProperty filePath = new SimpleStringProperty();
    private final StringProperty formattedDuration = new SimpleStringProperty(); // New property for formatted time

    // For creating new songs (no ID yet)
    public Song(String title, String artist, String category, int duration, String filePath) {
        setTitle(title);
        setArtist(artist);
        setCategory(category);
        setDuration(duration);
        setFilePath(filePath);
        updateFormattedDuration();
    }

    // For songs loaded from database (with ID)
    public Song(int id, String title, String artist, String category, int duration, String filePath) {
        setId(id);
        setTitle(title);
        setArtist(artist);
        setCategory(category);
        setDuration(duration);
        setFilePath(filePath);
        updateFormattedDuration();
    }

    // Default constructor
    public Song() {
    }

    // Property getters
    public IntegerProperty idProperty() { return id; }
    public StringProperty titleProperty() { return title; }
    public StringProperty artistProperty() { return artist; }
    public StringProperty categoryProperty() { return category; }
    public IntegerProperty durationProperty() { return duration; }
    public StringProperty filePathProperty() { return filePath; }
    public StringProperty formattedDurationProperty() { return formattedDuration; }

    // Getters and setters
    public int getId() { return id.get(); }
    public void setId(int id) {
        this.id.set(id);
    }

    public String getTitle() { return title.get(); }
    public void setTitle(String title) { this.title.set(title); }

    public String getArtist() { return artist.get(); }
    public void setArtist(String artist) { this.artist.set(artist); }

    public String getCategory() { return category.get(); }
    public void setCategory(String category) { this.category.set(category); }

    public int getDuration() { return duration.get(); }
    public void setDuration(int duration) {
        this.duration.set(duration);
        updateFormattedDuration();
    }

    public String getFilePath() { return filePath.get(); }
    public void setFilePath(String filePath) { this.filePath.set(filePath); }

    public String getFormattedDuration() { return formattedDuration.get(); }

    // Update formatted duration when duration changes
    private void updateFormattedDuration() {
        this.formattedDuration.set(getFormattedDurationFromSeconds());
    }

    // Convert seconds to MM:SS format
    public String getFormattedDurationFromSeconds() {
        int minutes = getDuration() / 60;
        int seconds = getDuration() % 60;
        return String.format("%d:%02d", minutes, seconds);
    }

    // Convert seconds to HH:MM:SS format if needed
    public String getFormattedDurationWithHours() {
        int hours = getDuration() / 3600;
        int minutes = (getDuration() % 3600) / 60;
        int seconds = getDuration() % 60;

        if (hours > 0) {
            return String.format("%d:%02d:%02d", hours, minutes, seconds);
        } else {
            return String.format("%d:%02d", minutes, seconds);
        }
    }

    // Display as "Artist - Duration" in lists
    @Override
    public String toString() {
        return getArtist() + " - " + getFormattedDurationFromSeconds();
    }

    // Compare songs by ID
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Song song = (Song) o;
        return getId() == song.getId();
    }

    // Hash by ID for collections
    @Override
    public int hashCode() {
        return getId();
    }
}