package com.example.locateapp;

import static androidx.core.content.ContextCompat.startActivity;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class recycleViewAdapter extends RecyclerView.Adapter<recycleViewAdapter.viewHolder>{

    ArrayList<AppNameWithLocationTime> appNameWithLocationsTime;
    Context context;

    public recycleViewAdapter(ArrayList<AppNameWithLocationTime> appNameWithLocationsTime, Context context) {
        this.appNameWithLocationsTime = appNameWithLocationsTime;
        this.context = context;
    }

    @NonNull
    @Override
    public viewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.app_detail,parent,false);
        return new recycleViewAdapter.viewHolder(view);

    }

    @Override
    public void onBindViewHolder(@NonNull viewHolder holder, int position) {

        final int pos = position;

        String[] packageParts = appNameWithLocationsTime.get(pos).appName.split("\\.");
        String app_name = packageParts[packageParts.length-1];
        String firstChar = app_name.substring(0, 1).toUpperCase();
        String restOfStr = app_name.substring(1).toLowerCase();
        app_name = firstChar + restOfStr;

        holder.app_name_txt.setText("App Name : "+app_name);
        holder.app_location_txt.setText("Location : Latitude -> "+appNameWithLocationsTime.get(pos).latitude+", Longitude -> "+appNameWithLocationsTime.get(position).longitude);
        holder.app_launchTime.setText("Launch Time -> "+appNameWithLocationsTime.get(pos).time);

        holder.app_location_txt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String strUri = "http://maps.google.com/maps?q=loc:" + appNameWithLocationsTime.get(pos).latitude + "," + appNameWithLocationsTime.get(pos).longitude;
                Intent intent = new Intent(android.content.Intent.ACTION_VIEW, Uri.parse(strUri));
                intent.setClassName("com.google.android.apps.maps", "com.google.android.maps.MapsActivity");
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return appNameWithLocationsTime.size();
    }

    public class viewHolder extends RecyclerView.ViewHolder{
        TextView app_name_txt, app_location_txt, app_launchTime;
        public viewHolder(@NonNull View itemView) {
            super(itemView);
            app_name_txt = itemView.findViewById(R.id.app_name_txt);
            app_location_txt = itemView.findViewById(R.id.app_location_txt);
            app_launchTime = itemView.findViewById(R.id.app_launchTime);
        }
    }

}
