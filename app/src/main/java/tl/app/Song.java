package tl.app;

public final class Song {

    private final String album;
    private final String artist;
    private final String duration;
    private final String uri;
    private final String id;
    private final String favorite;
    private final String serverId;
    private final String cover;
    private final String songTitle;

    Song(String album, String artist, String duration, String uri, String id, String favorite, String serverId, String cover, String songTitle) {
        this.album = album;
        this.artist = artist;
        this.duration = duration;
        this.uri = uri;
        this.id = id;
        this.favorite = favorite;
        this.serverId = serverId;
        this.cover = cover;

        this.songTitle = songTitle;
    }

    @Override
    public boolean equals(Object obj) {
        return obj == this || obj != null && obj.getClass() == this.getClass();
    }

    @Override
    public int hashCode() {
        return 1;
    }

    @Override
    public String toString() {
        return "SongInfo["
                + "album=" + album
                + ", artist=" + artist
                + ", duration=" + duration
                + ", uri=" + uri
                + ", id=" + id
                + ", favorite=" + favorite
                + ", serverId=" + serverId
                + ", cover=" + cover
                + ", songTitle=" + songTitle
                + "]";
    }

    public String getCover() {
        return cover;
    }

    public String getAlbum() {
        return album;
    }

    public String getArtist() {
        return artist;
    }

    public String getDuration() {
        return duration;
    }

    public String getUri() {
        return uri;
    }

    public String getId() {
        return id;
    }

    public String getFavorite() {
        return favorite;
    }

    public String getServerId() {
        return serverId;
    }

    public String getSongTitle() {
        return songTitle;
    }
}
