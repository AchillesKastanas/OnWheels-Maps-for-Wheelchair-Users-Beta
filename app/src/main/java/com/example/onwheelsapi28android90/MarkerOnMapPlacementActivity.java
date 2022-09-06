package com.example.onwheelsapi28android90;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.greenrobot.eventbus.EventBus;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import GeoManager.GeoQueryHandler;
import Helpers.ActionRecorder;

public class MarkerOnMapPlacementActivity extends AppCompatActivity {

    GeoQueryHandler geoQueryHandler = new GeoQueryHandler();
    LatLng latLng;
    GoogleMap googleMap;
    Context context;

    ActionRecorder actionRecorder = new ActionRecorder();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_marker_on_map_placement);

        context = this;

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

        //Retrieve geoQuerieHandlerObject and use it to place markers
        //geoQueryHandler = (GeoQueryHandler) getIntent().getSerializableExtra("geoQueryHandlerObject");
        latLng = (LatLng) getIntent().getExtras().getParcelable("LatLng");
        googleMap = EventBus.getDefault().getStickyEvent(GoogleMap.class);

        //Locate the UI Elements
        EditText titleEditText = (EditText) findViewById(R.id.titleEditText);
        EditText descriptionEditText = (EditText) findViewById(R.id.notesEditText);

        //Fill the address of the clicked location
        try {

            Geocoder geocoder = new Geocoder(this, Locale.getDefault());
            List<Address> addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5

            String address = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
//            String city = addresses.get(0).getLocality();
//            String state = addresses.get(0).getAdminArea();
//            String country = addresses.get(0).getCountryName();
//            String postalCode = addresses.get(0).getPostalCode();
//            String knownName = addresses.get(0).getFeatureName(); // Only if available else return NULL
            titleEditText.setText(address);

        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            //Add a temporary visual marker only - No connection with the database
            BitmapDescriptor bitmapDescriptor = BitmapDescriptorFactory.fromResource(R.drawable.purplemarker);
            Marker tempMarker = googleMap.addMarker(new MarkerOptions()
                    .position(latLng)
                    .icon(bitmapDescriptor)
                    .title("tempMarker"));

            //Add a temporary visual circle to help the user understand which marker he is placing
            Circle tempCircle = googleMap.addCircle(new CircleOptions()
                    .center(latLng)
                    .radius(7)
                    .strokeColor(Color.parseColor("#605DB3"))
                    .strokeWidth(4)
                    .fillColor(Color.parseColor("#2F676986")));

            //Animate camera OVER the marker marker
            double newLat = latLng.latitude + 0.0005;
            double newLong = latLng.longitude;

            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(new LatLng(newLat, newLong))             // Sets the center of the map to location user
                    .zoom(19)                   // Sets the zoom
                    .bearing(0)                 // Sets the orientation of the camera
                    .tilt(30)                   // Sets the tilt of the camera
                    .build();                   // Creates a CameraPosition from the builder
            googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));


            Button okButton = (Button) findViewById(R.id.okButton);
            okButton.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View view) {
                    //Record View Click
                    actionRecorder.addAction(view, "click", context);

                    //Store the new Marker

                    String title = titleEditText.getText().toString();

                    title = title.replace('.', ' ');
                    title = title.replace('#', ' ');
                    title = title.replace('$', ' ');
                    title = title.replace('[', ' ');
                    title = title.replace(']', ' ');
                    title = title.replace("  ", " ");

                    String description = descriptionEditText.getText().toString();

                    description = description.replace('.', ' ');
                    description = description.replace('#', ' ');
                    description = description.replace('$', ' ');
                    description = description.replace('[', ' ');
                    description = description.replace(']', ' ');
                    description = description.replace("  ", " ");

                    geoQueryHandler.addMarker(latLng.longitude, latLng.latitude, title, description);

                    tempMarker.remove();
                    tempCircle.remove();

                    Intent intent = new Intent("marker_added");
                    sendBroadcast(intent);

                    finish();
                }
            });

            Button closeTabButton = (Button) findViewById(R.id.closeTabButton);
            closeTabButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //Record View Click
                    actionRecorder.addAction(view, "click", context);

                    tempMarker.remove();
                    tempCircle.remove();

                    Intent intent = new Intent("marker_not_added");
                    sendBroadcast(intent);

                    finish();
                }
            });
        }
        catch(Exception e){
            Toast.makeText(context, "Error placing marker (No location found ?). Please try again later", Toast.LENGTH_SHORT).show();
        }
    }
}