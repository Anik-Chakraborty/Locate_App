package com.example.locateapp;

import static android.location.LocationManager.GPS_PROVIDER;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.usage.UsageEvents;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Calendar;

public class AppUsagesBackgroundService extends Service implements LocationListener {

    private static final String Foreground_ID = "Foreground Notification";
    private static final int NOTIFICATION_ID = 100;
    public static final String DATA = "Data";
    LocationManager locationManager;

    ArrayList<AppNameWithLocationTime> appNameWithLocationsTime;

    long count =1;

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {


        Intent notifyUser = new Intent(this, MainActivity.class);
        PendingIntent actionForeground = PendingIntent.getActivity(
                this, 0, notifyUser, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
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



        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getApplicationContext(),
                android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            //
        } else {
            locationManager.requestLocationUpdates(GPS_PROVIDER, 20000, 50,this);
        }

        //store app name along with usages time in shared preference

        return Service.START_STICKY;
    }



    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        appNameWithLocationsTime = new ArrayList<AppNameWithLocationTime>();
        double latitude =location.getLatitude();
        double longitude = location.getLongitude();

        long startTime =System.currentTimeMillis() - 1 * 60 * 1000;
        long endTime = System.currentTimeMillis();


        UsageStatsManager usageStatsManager = (UsageStatsManager) getSystemService(Context.USAGE_STATS_SERVICE);
        UsageEvents.Event event = new UsageEvents.Event();
        UsageEvents usageEvents = usageStatsManager.queryEvents(startTime, endTime);

        while (usageEvents.hasNextEvent()) {
            usageEvents.getNextEvent(event);

            if (event.getEventType() == UsageEvents.Event.MOVE_TO_FOREGROUND && !event.getPackageName().contains("launcher"))
            {
                long millis = event.getTimeStamp();
                Calendar calendar = Calendar.getInstance();
                calendar.setTimeInMillis(millis);
                int hour = calendar.get(Calendar.HOUR_OF_DAY);
                int minute = calendar.get(Calendar.MINUTE);
                int second = calendar.get(Calendar.SECOND);

                String time = hour+":"+minute+":"+second;

                String packageName = event.getPackageName();
//                Log.d("Launch", "App launched: " + packageName +" "+ hour+":"+minute+":"+second);

                appNameWithLocationsTime.add(new AppNameWithLocationTime(packageName,latitude,longitude,time));

                SharedPreferences sharedPreferences = getSharedPreferences("Result",MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();



                Gson gsonResult = new Gson();
                String jsonResult =gsonResult.toJson(appNameWithLocationsTime);
                editor.putString("ResultData"+count,jsonResult);
                editor.putLong("count",count);
                editor.apply();

                ++count;

            }
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
