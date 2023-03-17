package com.example.locateapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.ActivityManager;
import android.app.AppOpsManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    RecyclerView recycleView;

    TextView usagesData;
    Button service_on_off_btn;
    LocationManager locationManager;
    ArrayList<AppNameWithLocation> appNameWithLocations;
    Boolean flag=false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //View Binding
        service_on_off_btn = findViewById(R.id.service_on_off_btn);
        recycleView = findViewById(R.id.recycleView);

        service_on_off_btn_setText();

        if(flag){
            onRestart();
        }

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        service_on_off_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String service_btn_text = service_on_off_btn.getText().toString();
                if(service_btn_text.equals("START SERVICE")){
                    if(checkUsagesStatsAllowedOrNot()){ // check Usages Stats Allowed Or not in user mobile
                        if (ActivityCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION) !=
                                PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(MainActivity.this,
                                android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                            ActivityCompat.requestPermissions(MainActivity.this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION,
                                    Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
                        } else {

                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                startForegroundService(new Intent(MainActivity.this, AppUsagesBackgroundService.class));
                            }
                            else{
                                startService(new Intent(MainActivity.this, AppUsagesBackgroundService.class));
                            }
                            service_on_off_btn.setText("STOP SERVICE");

                        }

                    }
                    else{
                        Intent usageAccessIntent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
                        usageAccessIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(usageAccessIntent);

                        if(checkUsagesStatsAllowedOrNot()){ // check Usages Stats Allowed Or not in user mobile

                            if (ActivityCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION) !=
                                    PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(MainActivity.this,
                                    android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                                ActivityCompat.requestPermissions(MainActivity.this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION,
                                        Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
                            }
                            else{
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                    startForegroundService(new Intent(MainActivity.this, AppUsagesBackgroundService.class));
                                }
                                else{
                                    startService(new Intent(MainActivity.this, AppUsagesBackgroundService.class));
                                }
                                service_on_off_btn.setText("STOP SERVICE");
                            }

                        }
                        else{
                            Toast.makeText(MainActivity.this, "Please Give Access", Toast.LENGTH_SHORT).show();
                        }

                    }

                } else if (service_btn_text.equals("STOP SERVICE")) {
                        stopService(new Intent(MainActivity.this, AppUsagesBackgroundService.class));
                        service_on_off_btn.setText("START SERVICE");

                        SharedPreferences sharedPreferences = getApplication().getSharedPreferences("Data",MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.clear().commit();

                        SharedPreferences finalResult = getApplication().getSharedPreferences("Result",MODE_PRIVATE);
                        SharedPreferences.Editor editorResult = finalResult.edit();
                        editorResult.clear().commit();
                }
                else{
                    Log.i("Error",service_btn_text);
                }

            }


        });


        //




    }

    private boolean checkUsagesStatsAllowedOrNot() {
       try {
           PackageManager packageManager = getPackageManager();
           ApplicationInfo applicationInfo = packageManager.getApplicationInfo(getPackageName(),0);
           AppOpsManager appOpsManager = (AppOpsManager) getSystemService(APP_OPS_SERVICE);
           int mode = appOpsManager.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS,applicationInfo.uid,applicationInfo.packageName);
           return (mode == AppOpsManager.MODE_ALLOWED);
       }
       catch (Exception E){
           Log.i("Error On Catch", E.getMessage().toString());
           return false;
       }
    }
    private void service_on_off_btn_setText() {
        boolean flag =isMyServiceRunning(AppUsagesBackgroundService.class);

        if(flag){
            service_on_off_btn.setText("STOP SERVICE");
        }
        else{
            service_on_off_btn.setText("START SERVICE");
        }
    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                flag =true;
                return true;
            }
        }
        flag =false;
        return false;
    }

    @Override
    protected void onRestart() {
        super.onRestart();

        appNameWithLocations = new ArrayList<AppNameWithLocation>();

        SharedPreferences finalResult = getSharedPreferences("Result",MODE_PRIVATE);

        long count = finalResult.getLong("count",0);

        for(long j=0; j<count; j++){
            Gson gson = new Gson();
            String json = finalResult.getString("ResultData"+j,null);
            Type type = new TypeToken<ArrayList<AppNameWithLocation>>(){
            }.getType();

            appNameWithLocations = gson.fromJson(json,type);
            if(appNameWithLocations!=null){
                LinearLayoutManager linearLayout = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
                recycleView.setLayoutManager(linearLayout);
                recycleView.setAdapter(new recycleViewAdapter(appNameWithLocations, getApplicationContext()));
                for(int i=0; i<appNameWithLocations.size(); i++){
                    String[] packageParts = appNameWithLocations.get(i).appName.split("\\.");
                    String appName = packageParts[packageParts.length-1];
                    String firstChar = appName.substring(0, 1).toUpperCase();
                    String restOfStr = appName.substring(1).toLowerCase();
                    appName = firstChar + restOfStr;

                    Log.i("Result", appName+" "+appNameWithLocations.get(i).latitude+" "+appNameWithLocations.get(i).longitude);
                }
            }
            else{
                Log.i("Result","Empty");
            }

        }

    }


}