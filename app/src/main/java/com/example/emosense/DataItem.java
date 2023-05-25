package com.example.emosense;

import java.io.Serializable;

public class DataItem implements Serializable {

    private String artistName;
    private String trackName;
    private String url;

    public DataItem(String artistName, String trackName, String url) {
        this.artistName = artistName;
        this.trackName = trackName;
        this.url = url;
    }

    public String getArtistName() {
        return artistName;
    }

    public void setArtistName(String artistName) {
        this.artistName = artistName;
    }

    public String getTrackName() {
        return trackName;
    }

    public void setTrackName(String trackName) {
        this.trackName = trackName;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
