package dk.easv.demo.BE;

import javafx.beans.property.*;

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

    // Property getters
    public IntegerProperty idProperty() { return id; }
    public StringProperty nameProperty() { return name; }
    public IntegerProperty songCountProperty() { return songCount; }
    public StringProperty totalDurationProperty() { return totalDuration; }

    // Getters and setters
    public int getId() { return id.get(); }
    public void setId(int id) { this.id.set(id); }

    public String getName() { return name.get(); }
    public void setName(String name) { this.name.set(name); }

    public int getSongCount() { return songCount.get(); }
    public void setSongCount(int songCount) { this.songCount.set(songCount); }

    public String getTotalDuration() { return totalDuration.get(); }
    public void setTotalDuration(String totalDuration) { this.totalDuration.set(totalDuration); }

    // Display playlist name in UI components
    @Override
    public String toString() {
        return getName();
    }
}