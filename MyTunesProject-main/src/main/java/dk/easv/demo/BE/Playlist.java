package dk.easv.demo.BE;

import javafx.beans.property.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a music playlist containing multiple songs.
 * Automatically tracks song count and total duration.
 */
public class Playlist {
    private final IntegerProperty id = new SimpleIntegerProperty();
    private final StringProperty name = new SimpleStringProperty();
    private final List<Song> songs = new ArrayList<>();
    private final IntegerProperty songCount = new SimpleIntegerProperty();
    private final IntegerProperty totalTime = new SimpleIntegerProperty();
    private final StringProperty totalDuration = new SimpleStringProperty();

    public Playlist(int id, String name) {
        setId(id);
        setName(name);
        setSongCount(0);
        setTotalTime(0);
        setTotalDuration("00:00");
    }

    public IntegerProperty idProperty() { return id; }
    public StringProperty nameProperty() { return name; }
    public IntegerProperty songCountProperty() { return songCount; }
    public StringProperty totalDurationProperty() { return totalDuration; }
    public IntegerProperty totalTimeProperty() { return totalTime; }

    public int getId() { return id.get(); }
    public String getName() { return name.get(); }
    public List<Song> getSongs() { return songs; }
    public int getSongCount() { return songCount.get(); }
    public int getTotalTime() { return totalTime.get(); }
    public String getTotalDuration() { return totalDuration.get(); }

    public void setId(int id) { this.id.set(id); }
    public void setName(String name) { this.name.set(name); }
    public void setSongCount(int songCount) { this.songCount.set(songCount); }
    public void setTotalDuration(String totalDuration) { this.totalDuration.set(totalDuration); }

    public void setSongs(List<Song> songs) {
        this.songs.clear();
        this.songs.addAll(songs);
        setSongCount(songs.size());
        calculateTotalTime();
    }

    public void setTotalTime(int totalSeconds) {
        this.totalTime.set(totalSeconds);
        this.totalDuration.set(formatSecondsToDuration(totalSeconds));
    }

    public void addSong(Song song) {
        songs.add(song);
        setSongCount(songs.size());
        setTotalTime(getTotalTime() + song.getDuration());
    }

    public void removeSong(Song song) {
        if (songs.remove(song)) {
            setSongCount(songs.size());
            setTotalTime(getTotalTime() - song.getDuration());
        }
    }

    public void calculateTotalTime() {
        int totalSeconds = 0;
        for (Song song : songs) {
            totalSeconds += song.getDuration();
        }
        setTotalTime(totalSeconds);
    }

    private String formatSecondsToDuration(int seconds) {
        if (seconds <= 0) return "00:00";
        int minutes = seconds / 60;
        int secs = seconds % 60;
        return String.format("%d:%02d", minutes, secs);
    }

    @Override
    public String toString() {
        return getName() + " (" + getSongCount() + " songs, " + getTotalDuration() + ")";
    }
}
