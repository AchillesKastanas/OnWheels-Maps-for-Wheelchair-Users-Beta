package GeoManager;


import android.util.Log;

import androidx.annotation.NonNull;

import com.example.onwheelsapi28android90.R;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class GeoQueryHandler{

    DatabaseReference geofireDatabaseReference;
    GeoFire geoFire;
    String loggedInUser;
    //Context context;
    ArrayList<Marker> oldRenderedMarkers = new ArrayList<>();
    ArrayList<Marker> newRenderedMarkers = new ArrayList<>();
    GoogleMap googleMap;
    GeoQuery geoQuery;
    //excludedMarkers ArrayList lists markers that will not be removed/ affected at any time
    //Ex. Trip markers on the polyline should not be removed while out of range, because the user should be able to see them all at any point and time
    ArrayList<Marker> excludedMarkers = new ArrayList<>();

    public GeoQueryHandler(){

        //this.context = context;
        geofireDatabaseReference = FirebaseDatabase.getInstance().getReference().child("geofire");
        geoFire = new GeoFire(geofireDatabaseReference);
        loggedInUser = FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

    public void setupExcludedMarkersArrayList(ArrayList<Marker> newExcludedMarkers){
        for(Marker newExcludedMarker: newExcludedMarkers){
            excludedMarkers.add(newExcludedMarker);
        }
    }

    public void clearExcludedMarkersArrayList(){
        excludedMarkers.clear();
    }

    public void runGeoQuery(LatLng latLng, double radius, GoogleMap googleMap){

        clearRunningListeners();

        this.googleMap = googleMap;

        // creates a new query around [Long, Lat] with radius
        geoQuery = geoFire.queryAtLocation(new GeoLocation(latLng.latitude, latLng.longitude), radius);

        //Add Listener
        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                System.out.println(String.format("Key %s entered the search area at [%f,%f]", key, location.latitude, location.longitude));
                //Toast.makeText(context, "Key " +  key + " entered the search area", Toast.LENGTH_SHORT).show();
                showMarkerOnMap(key, location, googleMap);
            }

            @Override
            public void onKeyExited(String key) {
                System.out.println(String.format("Key %s is no longer in the search area", key));
                //Toast.makeText(context, "Key " +  key + " is no longer in the search area", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {
                System.out.println(String.format("Key %s moved within the search area to [%f,%f]", key, location.latitude, location.longitude));
                //Toast.makeText(context, "Key " +  key + " moved", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onGeoQueryReady() {
                System.out.println("All initial data has been loaded and events have been fired!");
                //Toast.makeText(context, "onGeoQueryReady", Toast.LENGTH_SHORT).show();
                geoQuery.removeAllListeners();
                removeOutOfBoundsMarkers();
            }

            @Override
            public void onGeoQueryError(DatabaseError error) {
                System.err.println("There was an error with this query: " + error);
                //Toast.makeText(context, "There was an error with this query: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void clearRunningListeners(){
        try {
            geoQuery.removeAllListeners();
        }
        catch (Exception e){
            //no running listeners
        }
    }

    public void addMarker(double Long, double Lat, String title, String description){

        String nodeName = title + " - " + description;
        geoFire.setLocation(nodeName , new GeoLocation(Lat, Long));

        //Rearrange the Firebase Database
        //
        // In order to avoid editing the geoFire library, for every new entry added, the entry will be accessed from the
        // Database and re edited in order for more info to be added

        //Create a new marker with a unique ID
        DatabaseReference newMarkerReference = geofireDatabaseReference.push();

        //Grab the marker that was created in geoFire.setLocation(); and copy all the Data to the newly pushed marker
        geofireDatabaseReference.child(nodeName).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                //Copy all the Data to the newly pushed marker (newMarkerReference)
                Map<String, Object> update = new HashMap<>();
                update.put("g", snapshot.child("g").getValue().toString());
                update.put("l", Arrays.asList(Float.parseFloat(snapshot.child("l").child("0").getValue().toString()),
                        Float.parseFloat(snapshot.child("l").child("1").getValue().toString())));
                newMarkerReference.setValue(update);

                //Add the new data
                if(title.equals("")){
                    newMarkerReference.child("markerdetails").child("title").setValue("Unknown Location");
                }
                else{
                    newMarkerReference.child("markerdetails").child("title").setValue(title);
                }
                newMarkerReference.child("markerdetails").child("notes").setValue(description);
                newMarkerReference.child("markerdetails").child("type").setValue("Wheelchair Ramp");
                newMarkerReference.child("markerdetails").child("availability").setValue("available");
                newMarkerReference.child("markerdetails").child("likes").child("amount").setValue(1);
                newMarkerReference.child("markerdetails").child("likes").child("accounts").push().setValue(loggedInUser);
                newMarkerReference.child("markerdetails").child("dislikes").child("amount").setValue(0);
                newMarkerReference.child("markerdetails").child("views").child("amount").setValue(1);
                newMarkerReference.child("markerdetails").child("views").child("accounts").push().setValue(loggedInUser);
                newMarkerReference.child("markerdetails").child("owner").push().setValue(loggedInUser);

                //Delete the geoFire.setLocation() marker after all the copying is done
                geofireDatabaseReference.child(nodeName).getRef().removeValue();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    void showMarkerOnMap(String key, GeoLocation geoLocation, GoogleMap googleMap){

        //Check for Marker Availability. If marker is Enabled, then the marker will be purple, else it will be red
        geofireDatabaseReference.child(key).child("markerdetails").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                try {
                     //Draw marker normally
                     String availability = snapshot.child("availability").getValue().toString();
                     if (availability.equals("available")) {
                         BitmapDescriptor bitmapDescriptor = BitmapDescriptorFactory.fromResource(R.drawable.purplemarker);

                         Marker marker = googleMap.addMarker(new MarkerOptions()
                                 .position(new LatLng(geoLocation.latitude, geoLocation.longitude))
                                 .title(key)
                                 .icon(bitmapDescriptor));
                         newRenderedMarkers.add(marker);
                     } else {

                         BitmapDescriptor bitmapDescriptor = BitmapDescriptorFactory.fromResource(R.drawable.redmarker);

                         Marker marker = googleMap.addMarker(new MarkerOptions()
                                 .position(new LatLng(geoLocation.latitude, geoLocation.longitude))
                                 .title(key)
                                 .icon(bitmapDescriptor));
                         newRenderedMarkers.add(marker);
                     }
                }
                catch(Exception e){
                    //This marker crashes the app because it is not fully set up yet, skip it and re render it in the future
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    public void removeOutOfBoundsMarkers(){
        //Check the oldRenderedMarkers Array. If there are more markers there, it means that
        //there are out of bounds markers. Loop oldRenderedMarkers to check them
        if(!oldRenderedMarkers.isEmpty()) {
            for (Marker marker : oldRenderedMarkers) {
                //If one of the old keys not in the excludedMarkers ArrayList
                if (!excludedMarkers.contains(marker)) { //!newRenderedMarkers.contains(marker) &&
                    //Remove the old key
                    marker.remove();
                }
            }
        }
        oldRenderedMarkers.clear();
        for(Marker marker: newRenderedMarkers){
            oldRenderedMarkers.add(marker);
        }
        newRenderedMarkers.clear();
    }


}
