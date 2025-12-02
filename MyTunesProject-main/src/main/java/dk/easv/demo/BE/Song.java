package dk.easv.demo.BE;

import javafx.beans.binding.Bindings;
import javafx.beans.property.*;

/**
 * Represents a music track with metadata and file info.
 */
public class Song {
    private final IntegerProperty id = new SimpleIntegerProperty();
    private final StringProperty title = new SimpleStringProperty("");
    private final StringProperty artist = new SimpleStringProperty("");
    private final StringProperty category = new SimpleStringProperty("");
    private final IntegerProperty duration = new SimpleIntegerProperty(0); // Duration in seconds
    private final StringProperty filePath = new SimpleStringProperty("");
    private final StringProperty formattedDuration = new SimpleStringProperty("0:00");

    /**
     * Creates a new song (no ID yet).
     */
    public Song(String title, String artist, String category, int duration, String filePath) {
        this(0, title, artist, category, duration, filePath);
    }

    /**
     * Creates a song with all properties (e.g. loaded from database).
     */
    public Song(int id, String title, String artist, String category, int duration, String filePath) {
        setId(id);
        setTitle(title);
        setArtist(artist);
        setCategory(category);
        setDuration(duration);
        setFilePath(filePath);

        // Bind formattedDuration automatically to duration
        formattedDuration.bind(Bindings.createStringBinding(
                this::getFormattedDurationWithHours,
                this.duration));
    }

    // Default constructor with safe defaults
    public Song() {
        this(0, "", "", "", 0, "");
    }

    // Property getters (for JavaFX binding)
    public IntegerProperty idProperty() { return id; }
    public StringProperty titleProperty() { return title; }
    public StringProperty artistProperty() { return artist; }
    public StringProperty categoryProperty() { return category; }
    public IntegerProperty durationProperty() { return duration; }
    public StringProperty filePathProperty() { return filePath; }
    public StringProperty formattedDurationProperty() { return formattedDuration; }

    // Standard getters and setters
    public int getId() { return id.get(); }
    public void setId(int id) { this.id.set(id); }

    public String getTitle() { return title.get(); }
    public void setTitle(String title) { this.title.set(title); }

    public String getArtist() { return artist.get(); }
    public void setArtist(String artist) { this.artist.set(artist); }

    public String getCategory() { return category.get(); }
    public void setCategory(String category) { this.category.set(category); }

    public int getDuration() { return duration.get(); }
    public void setDuration(int duration) { this.duration.set(duration); }

    public String getFilePath() { return filePath.get(); }
    public void setFilePath(String filePath) { this.filePath.set(filePath); }

    public String getFormattedDuration() { return formattedDuration.get(); }

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

        return (hours > 0)
                ? String.format("%d:%02d:%02d", hours, minutes, seconds)
                : String.format("%d:%02d", minutes, seconds);
    }

    @Override
    public String toString() {
        return String.format("%s - %s (%s)", getTitle(), getArtist(), getFormattedDuration());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Song)) return false;
        Song song = (Song) o;

        // If ID is set, compare by ID; otherwise fallback to title+artist
        if (getId() > 0 && song.getId() > 0) {
            return getId() == song.getId();
        }
        return getTitle().equalsIgnoreCase(song.getTitle())
                && getArtist().equalsIgnoreCase(song.getArtist());
    }

    @Override
    public int hashCode() {
        return (getId() > 0)
                ? Integer.hashCode(getId())
                : (getTitle() + getArtist()).toLowerCase().hashCode();
    }
}
