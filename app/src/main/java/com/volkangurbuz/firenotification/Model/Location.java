package com.volkangurbuz.firenotification.Model;

/**
 * Created by VolkanGurbuz on 4/20/2018.
 */


public class Location {

    private String videoID;
    private String latitude , longitude;


    public Location(String videoID, String latitude, String longitude) {
        this.videoID = videoID;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getVideoID() {
        return videoID;
    }

    public void setVideoID(String videoID) {
        this.videoID = videoID;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    @Override
    public String toString() {
        return "Location{" +
                "videoID='" + videoID + '\'' +
                ", latitude='" + latitude + '\'' +
                ", longitude='" + longitude + '\'' +
                '}';
    }
}
