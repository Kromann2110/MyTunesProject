package dk.easv.demo.BE;

/**
 * Represents a music playlist with basic info and calculated properties
 */
public class Playlist {
    private int id;
    private String name;
    private int songCount;
    private String totalDuration;

    // Creates new playlist with default values
    public Playlist(int id, String name) {
        this.id = id;
        this.name = name;
        this.songCount = 0;
        this.totalDuration = "00:00";
    }

    // Creates playlist with all properties
    public Playlist(int id, String name, int songCount, String totalDuration) {
        this.id = id;
        this.name = name;
        this.songCount = songCount;
        this.totalDuration = totalDuration;
    }

    // Getters and setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getSongCount() { return songCount; }
    public void setSongCount(int songCount) { this.songCount = songCount; }

    public String getTotalDuration() { return totalDuration; }
    public void setTotalDuration(String totalDuration) { this.totalDuration = totalDuration; }

    // Display playlist name in UI components
    @Override
    public String toString() {
        return name;
    }
}