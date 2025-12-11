package dk.easv.demo.BE;

import javafx.beans.property.*;

/**
 * Represents a music track with metadata and file information.
 * Uses JavaFX properties for automatic UI binding.
 */
public class Song {
    private final IntegerProperty id = new SimpleIntegerProperty();
    private final StringProperty title = new SimpleStringProperty();
    private final StringProperty artist = new SimpleStringProperty();
    private final StringProperty category = new SimpleStringProperty();
    private final IntegerProperty duration = new SimpleIntegerProperty();
    private final StringProperty filePath = new SimpleStringProperty();
    private final StringProperty formattedDuration = new SimpleStringProperty();

    public Song() {}

    public Song(String title, String artist, String category, int duration, String filePath) {
        setTitle(title);
        setArtist(artist);
        setCategory(category);
        setDuration(duration);
        setFilePath(filePath);
        updateFormattedDuration();
    }

    public Song(int id, String title, String artist, String category, int duration, String filePath) {
        setId(id);
        setTitle(title);
        setArtist(artist);
        setCategory(category);
        setDuration(duration);
        setFilePath(filePath);
        updateFormattedDuration();
    }

    public IntegerProperty idProperty() { return id; }
    public StringProperty titleProperty() { return title; }
    public StringProperty artistProperty() { return artist; }
    public StringProperty categoryProperty() { return category; }
    public IntegerProperty durationProperty() { return duration; }
    public StringProperty filePathProperty() { return filePath; }
    public StringProperty formattedDurationProperty() { return formattedDuration; }

    public int getId() { return id.get(); }
    public String getTitle() { return title.get(); }
    public String getArtist() { return artist.get(); }
    public String getCategory() { return category.get(); }
    public int getDuration() { return duration.get(); }
    public String getFilePath() { return filePath.get(); }
    public String getFormattedDuration() { return formattedDuration.get(); }

    public void setId(int id) { this.id.set(id); }
    public void setTitle(String title) { this.title.set(title); }
    public void setArtist(String artist) { this.artist.set(artist); }
    public void setCategory(String category) { this.category.set(category); }
    public void setFilePath(String filePath) { this.filePath.set(filePath); }

    public void setDuration(int duration) {
        this.duration.set(duration);
        updateFormattedDuration();
    }

    private void updateFormattedDuration() {
        this.formattedDuration.set(getFormattedDurationFromSeconds());
    }

    public String getFormattedDurationFromSeconds() {
        int minutes = getDuration() / 60;
        int seconds = getDuration() % 60;
        return String.format("%d:%02d", minutes, seconds);
    }

    public String getFormattedDurationWithHours() {
        int hours = getDuration() / 3600;
        int minutes = (getDuration() % 3600) / 60;
        int seconds = getDuration() % 60;

        return hours > 0
                ? String.format("%d:%02d:%02d", hours, minutes, seconds)
                : String.format("%d:%02d", minutes, seconds);
    }

    @Override
    public String toString() {
        return getArtist() + " - " + getFormattedDurationFromSeconds();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Song song = (Song) o;
        return getId() == song.getId();
    }

    @Override
    public int hashCode() {
        return getId();
    }
}