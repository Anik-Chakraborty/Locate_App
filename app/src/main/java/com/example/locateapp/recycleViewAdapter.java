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

    ArrayList<AppNameWithLocation> appNameWithLocations;
    Context context;

    public recycleViewAdapter(ArrayList<AppNameWithLocation> appNameWithLocations, Context context) {
        this.appNameWithLocations = appNameWithLocations;
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

        String[] packageParts = appNameWithLocations.get(pos).appName.split("\\.");
        String app_name = packageParts[packageParts.length-1];
        String firstChar = app_name.substring(0, 1).toUpperCase();
        String restOfStr = app_name.substring(1).toLowerCase();
        app_name = firstChar + restOfStr;

        holder.app_name_txt.setText("App Name : "+app_name);
        holder.app_location_txt.setText("Location : Latitude -> "+appNameWithLocations.get(pos).latitude+", Longitude -> "+appNameWithLocations.get(position).longitude);


        holder.app_location_txt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String strUri = "http://maps.google.com/maps?q=loc:" + appNameWithLocations.get(pos).latitude + "," + appNameWithLocations.get(pos).longitude;
                Intent intent = new Intent(android.content.Intent.ACTION_VIEW, Uri.parse(strUri));
                intent.setClassName("com.google.android.apps.maps", "com.google.android.maps.MapsActivity");
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return appNameWithLocations.size();
    }

    public class viewHolder extends RecyclerView.ViewHolder{
        TextView app_name_txt, app_location_txt;
        public viewHolder(@NonNull View itemView) {
            super(itemView);
            app_name_txt = itemView.findViewById(R.id.app_name_txt);
            app_location_txt = itemView.findViewById(R.id.app_location_txt);
        }
    }

}
