package com.example.locateapp;

import static android.location.LocationManager.GPS_PROVIDER;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class AppUsagesBackgroundService extends Service implements LocationListener {

    private static final String Foreground_ID = "Foreground Notification";
    private static final int NOTIFICATION_ID = 100;
    public static final String DATA = "Data";
    LocationManager locationManager;
    ArrayList<AppNameWithTime> appNameWithTimeArrayListCur;
    ArrayList<AppNameWithTime> appNameWithTimeArrayListPre ;

    ArrayList<AppNameWithLocation> appNameWithLocations;

    long count =1;

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        appNameWithTimeArrayListPre = new ArrayList<AppNameWithTime>();

        Intent notifyUser = new Intent(this, MainActivity.class);
        PendingIntent actionForeground = PendingIntent.getActivity(
                this, 0, notifyUser, PendingIntent.FLAG_UPDATE_CURRENT
        );

        NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            Notification notificationForeground = new Notification.Builder(this,Foreground_ID)
                    .setSmallIcon(R.drawable.app_icon)
                    .setContentText("Service of App Usages is running")
                    .setSubText("Service Running")
                    .setChannelId(Foreground_ID)
                    .setContentIntent(actionForeground)
                    .build();

            nm.createNotificationChannel(new NotificationChannel(Foreground_ID, "Foreground Channel", NotificationManager.IMPORTANCE_HIGH));

            startForeground(NOTIFICATION_ID, notificationForeground);
        }

        //store app name along with usages time in shared preference
        Thread thread = new Thread(){
            @Override
            public void run() {
                super.run();


                UsageStatsManager statsManager = (UsageStatsManager) getSystemService(USAGE_STATS_SERVICE);
                long endTime = System.currentTimeMillis();
                long startTime = endTime - (1000);
                List<UsageStats> usageStatsList = statsManager.queryUsageStats(UsageStatsManager.INTERVAL_BEST,startTime, endTime);
                if(usageStatsList != null){
                    for(UsageStats usageStats : usageStatsList){
                            String packageName = usageStats.getPackageName();
                            double totalTime = 0;
                            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                                totalTime = (usageStats.getTotalTimeVisible()*1) / 60000;
                            }
                            else {
                                totalTime = (usageStats.getTotalTimeInForeground()*1) / 60000;
                            }

                            appNameWithTimeArrayListPre.add(new AppNameWithTime(packageName,totalTime));
                    }

                    SharedPreferences sharedPreferences = getApplication().getSharedPreferences(DATA,MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    Gson gson = new Gson();
                    String json =gson.toJson(appNameWithTimeArrayListPre);
                    editor.putString("AppUsesData",json);
                    editor.apply();

                }
                else{
                    Log.i("HI","HI");
                }


            }
        };
        thread.start();

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getApplicationContext(),
                android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            //
        } else {
            locationManager.requestLocationUpdates(GPS_PROVIDER, 20000, 50,this);
        }

        return Service.START_STICKY;
    }



    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        appNameWithLocations = new ArrayList<AppNameWithLocation>();
        double latitude =location.getLatitude();
        double longitude = location.getLongitude();

        //Previous App Name with Total Time
        SharedPreferences sharedPreferences = getApplication().getSharedPreferences(DATA,MODE_PRIVATE);
        Gson gson = new Gson();
        String json = sharedPreferences.getString("AppUsesData", " ");
        Type type = new TypeToken<ArrayList<AppNameWithTime>>(){
        }.getType();

        appNameWithTimeArrayListPre = new ArrayList<AppNameWithTime>();

        appNameWithTimeArrayListPre = gson.fromJson(json,type);

//        Log.i("Size Pre", String.valueOf(appNameWithTimeArrayListPre.size()));

        //Current App Name with Total Time
        appNameWithTimeArrayListCur = new ArrayList<AppNameWithTime>();




                UsageStatsManager statsManager = (UsageStatsManager) getSystemService(USAGE_STATS_SERVICE);
                long endTime = System.currentTimeMillis();
                long startTime = endTime - (20000);
                List<UsageStats> usageStatsList = statsManager.queryUsageStats(UsageStatsManager.INTERVAL_BEST,startTime, endTime);

//               Log.i("Size List", String.valueOf(usageStatsList.size()));

                if(usageStatsList != null){
                    for(UsageStats usageStats : usageStatsList){
                        //if(installedApp.contains(usageStats.getPackageName())) {
                            String packageName = usageStats.getPackageName();
                            double totalTime = 0;
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                totalTime = (double) (usageStats.getTotalTimeVisible() * 1) / 60000;
                            } else {
                                totalTime = (double)(usageStats.getTotalTimeInForeground() * 1) / 60000;
                            }
                            appNameWithTimeArrayListCur.add(new AppNameWithTime(packageName, totalTime));
                        //}
                    }
                }
                else{
                    Log.i("HI","HI");
                }

//                Log.i("Size Cur", String.valueOf(appNameWithTimeArrayListCur.size()));


        //Comparing Previous Data With Current Data
        for(int i=0;i<appNameWithTimeArrayListCur.size();i++){
            String CurPackageName = appNameWithTimeArrayListCur.get(i).appName;
            for(int j=0;j<appNameWithTimeArrayListPre.size();j++){
                String PrePackageName = appNameWithTimeArrayListPre.get(j).appName;
                if(CurPackageName.equals(PrePackageName)){
                    double CurPackageTime = appNameWithTimeArrayListCur.get(i).usesTime;
                    double PrePackageTime = appNameWithTimeArrayListPre.get(j).usesTime;
                    if(CurPackageTime>PrePackageTime && CurPackageTime > 0){
                        appNameWithLocations.add(new AppNameWithLocation(CurPackageName,latitude,longitude));
                    }
                    break;
                }

            }
        }

        Log.i("Size Loc", String.valueOf(appNameWithLocations.size()));

        if(appNameWithLocations.size()>0){
            for(int i=0; i<appNameWithLocations.size();i++){
                Log.i("Result",appNameWithLocations.get(i).appName);
            }
        }



        //Store Result Data
        SharedPreferences finalResult = getApplication().getSharedPreferences("Result",MODE_PRIVATE);
        SharedPreferences.Editor editorResult = finalResult.edit();
        gson = new Gson();
        json =gson.toJson(appNameWithLocations);
        editorResult.putLong("count",count);
        editorResult.putString("ResultData"+count,json);
        editorResult.apply();

        Log.i("count", String.valueOf(count));
        ++count;


        //Clear and Update Previous Data

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();

        Gson gsonResult = new Gson();
        String jsonResult =gsonResult.toJson(appNameWithTimeArrayListCur);
        editor.putString("AppUsesData",jsonResult);
        editor.apply();


        try{
            appNameWithTimeArrayListPre.clear();
            appNameWithTimeArrayListCur.clear();
        }
        catch(Exception e){
            Log.i("Error",e.getMessage().toString());
        }


    }

    @Override
    public void onProviderEnabled(@NonNull String provider) {

    }

    @Override
    public void onProviderDisabled(@NonNull String provider) {

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        locationManager.removeUpdates(this);

    }
}
