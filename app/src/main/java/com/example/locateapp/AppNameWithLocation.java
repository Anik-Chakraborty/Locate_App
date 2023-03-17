package com.example.locateapp;

import androidx.recyclerview.widget.RecyclerView;

public class AppNameWithLocation {

    String appName;
    double latitude, longitude;

    public AppNameWithLocation() {

    }

    public AppNameWithLocation(String appName,double latitude, double longitude) {
        this.appName = appName;
        this.latitude = latitude;
        this.longitude = longitude;
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
