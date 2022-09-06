package com.example.onwheelsapi28android90;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

public class MarkerGeneralPlacementActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_marker_general_placement);

        //Setup Status Bar
        try{
            getSupportActionBar().hide();
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);

            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(getResources().getColor(R.color.minimalDarkPurple));
            window.setNavigationBarColor(getResources().getColor(R.color.minimalDarkPurple));
        }
        catch (Exception z){
            //Title bar already disabled?
        }

    }

    @Override
    public void finish() {
        super.finish();
        //Exit animation
        overridePendingTransition(R.anim.activity_fade_in, R.anim.activity_slide_center_to_right);
    }

    public void closeTab(View view){
        //Send a broadcast to show the UI that was hidden when the MarkerGeneralPlacementActivity launched
        Intent intent = new Intent("show_map_ui");
        sendBroadcast(intent);

        finish();
    }

    public void addMarkerOnMap(View view){
        //Send a broadcast to notify that a marker addition event has started from the MarkerGeneralPlacementActivity
        Intent intent = new Intent("add_marker_on_map");
        sendBroadcast(intent);

        finish();
    }

    public void addMarkerOnUser(View view){
        //Send a broadcast to notify that a marker addition on user location event has started from the MarkerGeneralPlacementActivity
        Intent intent = new Intent("add_marker_on_user");
        sendBroadcast(intent);

        finish();
    }
}