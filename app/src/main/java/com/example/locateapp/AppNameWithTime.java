package com.example.locateapp;

public class AppNameWithTime {
    String appName;
    double usesTime;


    AppNameWithTime(){
    }


    AppNameWithTime(String appName, double usesTime){
        this.appName = appName;
        this.usesTime =usesTime;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public double getUsesTime() {
        return usesTime;
    }

    public void setUsesTime(long usesTime) {
        this.usesTime = usesTime;
    }
}
