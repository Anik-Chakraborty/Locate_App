package com.example.locateapp;

public class AppNameWithLocationTime {

    String appName;
    double latitude, longitude;
    String time;

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public AppNameWithLocationTime() {

    }

    public AppNameWithLocationTime(String appName,double latitude, double longitude, String time) {
        this.appName = appName;
        this.latitude = latitude;
        this.longitude = longitude;
        this.time = time;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }
}
