package com.example.onwheelsapi28android90;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.PersistableBundle;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.RotateAnimation;
import android.view.animation.TranslateAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.GroundOverlay;
import com.google.android.gms.maps.model.GroundOverlayOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.maps.DirectionsApi;
import com.google.maps.DirectionsApiRequest;
import com.google.maps.GeoApiContext;
import com.google.maps.model.DirectionsLeg;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.DirectionsRoute;
import com.google.maps.model.DirectionsStep;
import com.google.maps.model.EncodedPolyline;
import com.google.maps.model.TravelMode;
import com.squareup.picasso.Picasso;

import org.greenrobot.eventbus.EventBus;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import CommunitySystem.CommunityActivity;
import GeoManager.CustomMarkerInfoWindowAdapter;
import GeoManager.GeoQueryHandler;
import Helpers.ActionRecorder;
import NotificationSystem.NotificationSystem;
import ProfileSystem.ProfileActivity;
import ProfileSystem.UserStatisticsTracker;
import de.hdodenhof.circleimageview.CircleImageView;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {

    // --- VARIABLES --- //

    MapView mapView;
    Button communityButton, plusButton, backgroundButton, backgroundButton2, confirmDestinationPointButton, cancelDestinationPointButton, searchLocationButton, markerSelectionBackgroundButton;
    CircleImageView profileButton;
    ImageButton trackUserLocationButton;
    EditText searchBarEditText;
    ImageView markerSelectionImage;
    TextView confirmDestinationPointTextView, rampSelectionTitleTextView, rampSelectionDescriptionTextView, notificationDotCounter;
    GoogleMap googleMap;
    String UID;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference, userReference;
    //Stores the latest circle location so it can get deleted and re-drawn
    Circle oldUserSearchCircle;
    LatLng storedDestinationLatLng;
    Polyline currentMapPolyline;
    Context context;
    public GeoQueryHandler geoQuerieHandler;
    BroadcastReceiver broadcastReceiver;

    String apiKEY = "Enter Your Api Key";
    boolean trackUserLocation = true;
    boolean openInfoWindow = false;
    Marker markerWithOpenInfoWindow = null;
    boolean recordMetersTraveled = false;
    boolean firstTimeLoad = true;
    boolean cameraFollowsUser = true;
    //Represents the marker addition mode. When the user select the: Place marker on map, markerAdditionMode becomes true
    boolean markerAdditionMode = false;
    boolean markerSelectionMode = false;
    ArrayList<Marker> selectedMarkersArrayList = new ArrayList<>();
    LatLng lastKnownUserLocation = new LatLng(0,0);
    int newNotificationCounter = 0;

    UserStatisticsTracker userStatisticsTracker = new UserStatisticsTracker();
    ActionRecorder actionRecorder = new ActionRecorder();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = this;

        //Setup Status Bar
        try{
            getSupportActionBar().hide();
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);

            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(getResources().getColor(R.color.white));
            window.setNavigationBarColor(getResources().getColor(R.color.white));
        }
        catch (Exception z){
            //Title bar already disabled?
        }

        //Get Logged in user UID
        UID = FirebaseAuth.getInstance().getUid();

        //Get firebase realtime database reference
        firebaseDatabase = FirebaseDatabase.getInstance();
        userReference = firebaseDatabase.getReference().child("users").child(UID);

        communityButton = (Button) findViewById(R.id.notificationButton);
        markerSelectionBackgroundButton = (Button) findViewById(R.id.markerSelectionBackgroundButton);
        profileButton = (CircleImageView) findViewById(R.id.profileButton);
        markerSelectionImage = (ImageView) findViewById(R.id.markerSelectionImage);
        plusButton = (Button) findViewById(R.id.plusButton);
        backgroundButton = (Button) findViewById(R.id.backgroundButton);
        backgroundButton2 = (Button) findViewById(R.id.backgroundButton2);
        confirmDestinationPointButton = (Button) findViewById(R.id.confirmDestinationPointButton);
        cancelDestinationPointButton = (Button) findViewById(R.id.cancelDestinationPointButton);
        searchLocationButton = (Button) findViewById(R.id.searchLocationButton);
        trackUserLocationButton = (ImageButton) findViewById(R.id.trackUserLocationButton);
        searchBarEditText = (EditText) findViewById(R.id.searchBarEditText);
        confirmDestinationPointTextView = (TextView) findViewById(R.id.confirmDestinationPointTextView);
        rampSelectionTitleTextView = (TextView) findViewById(R.id.rampSelectionTitleTextView);
        rampSelectionDescriptionTextView = (TextView) findViewById(R.id.rampSelectionDescriptionTextView);
        notificationDotCounter = (TextView) findViewById(R.id.notificationDotCounter);

        //Set up search bar
        searchBarEditText.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                // If the event is a key-down event on the "enter" button
                if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    searchLocation(v);
                    return true;
                }
                return false;
            }
        });

        //Setup the profileButton with the user profile image
        //If user has a profile image uploaded, load it in the imageView
        if(!userReference.child("profileimage").getKey().isEmpty()){
            userReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    //Get the image URL
                    String imageURL = snapshot.child("profileimage").getValue().toString();
                    //Use Picasso to load the image in the imageView
                    Picasso.with(context).load(imageURL).into(profileButton);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
        //If user has no uploaded profile image, load a temporary one
        else{
            profileButton.setImageResource(R.drawable.profile);
        }

        //Play the intro animation
        introAnimation();

        //Initialize the geoQuerieHandler class
        geoQuerieHandler = new GeoQueryHandler();

        //Map Setup
        mapView = findViewById(R.id.mapView);
        mapView.getMapAsync(this);
        mapView.onCreate(savedInstanceState);

        //Receive "finish_main_activity" broadcast when the user gets signed out so no activities are left running.
        //Recieve "show_map_ui" broadcast when an activity that used hideTopBar() or hideBottomBar() closes.
        broadcastReceiver = new BroadcastReceiver() {

            @Override
            public void onReceive(Context arg0, Intent intent) {
                String action = intent.getAction();
                Toast.makeText(context, "RECIEVED", Toast.LENGTH_SHORT);
                if (action.equals("finish_main_activity")) {
                    unregisterReceiver(broadcastReceiver);
                    finish();
                }
                else if(action.equals("show_map_ui")){
                    showBottomBar();
                    showTopBar();
                }
                else if(action.equals("add_marker_on_map")){
                    markerAdditionMode = true;
                }
                else if(action.equals("add_marker_on_user")){
                    //Simulate a "Click" on the user location, launch the MarkerOnMapPlacementActivity that is responsible for the "Click" to add a marer

                    pauseUserTracking();
                    pauseCameraTracking();

                    //Call the Marker Placement Activity and pass the geoQueryHandlerObject to the new Activity
                    Intent intent2 = new Intent(context, MarkerOnMapPlacementActivity.class);
                    //intent.putExtra("geoQueryHandlerObject", (Parcelable) geoQuerieHandler);
                    //Pass the clicked coordinates
                    intent2.putExtra("LatLng", lastKnownUserLocation);
                    //Pass the googleMap avoiding serialization of the object by Using EventBus
                    //Something like -> intent.putExtra("googleMapObject", (Serializable) googleMap);
                    EventBus.getDefault().postSticky(googleMap);
                    //Start the overlay activity
                    startActivity(intent2);
                }
                else if(action.equals("marker_added")){
                    addXPToUser(25);
                    markerAdditionMode = false;
                    showTopBar();
                    showBottomBar();

                    //Record Statistic
                    userStatisticsTracker.addStat("rampsplaced");
                }
                else if(action.equals("marker_not_added")){
                    markerAdditionMode = false;
                    showTopBar();
                    showBottomBar();
                }
                else if(action.equals("reset_notification_dot")){
                    //remove the notification bubble and reset the counter
                    newNotificationCounter = 0;
                    notificationDotCounter.setVisibility(View.INVISIBLE);
                    notificationDotCounter.setText("");
                }
                else{
                    Toast.makeText(context, action, Toast.LENGTH_SHORT);
                }
            }
        };

        registerReceiver(broadcastReceiver, new IntentFilter("finish_main_activity"));
        registerReceiver(broadcastReceiver, new IntentFilter("show_map_ui"));
        registerReceiver(broadcastReceiver, new IntentFilter("add_marker_on_map"));
        registerReceiver(broadcastReceiver, new IntentFilter("add_marker_on_user"));
        registerReceiver(broadcastReceiver, new IntentFilter("marker_added"));
        registerReceiver(broadcastReceiver, new IntentFilter("marker_not_added"));
        registerReceiver(broadcastReceiver, new IntentFilter("reset_notification_dot"));

        //Setup a notification listener to display a notification bubble on new notification arrival
        userReference.child("notifications").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot notification: snapshot.getChildren()){
                    //if it is a new notification
                    if(!notification.getValue().toString().contains("R.")){
                        newNotificationCounter += 1;
                        notificationDotCounter.setVisibility(View.VISIBLE);
                        if(newNotificationCounter > 99){
                            //Too many notifications to display
                            notificationDotCounter.setText("99+");
                        }
                        else{
                            notificationDotCounter.setText(newNotificationCounter + "");
                        }

                    }
                }
                newNotificationCounter = 0;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    @SuppressLint({"MissingPermission", "PotentialBehaviorOverride"})
    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        //Store the googleMap
        this.googleMap = googleMap;

        //Setup the Custom Marker Info Window
        googleMap.setInfoWindowAdapter(new CustomMarkerInfoWindowAdapter(MapActivity.this));

        //googleMap.setBuildingsEnabled(true);
        googleMap.setMyLocationEnabled(true);
        googleMap.getUiSettings().setMyLocationButtonEnabled(false);

        try {
            // Customise the styling of the base map using a JSON object defined
            // in a raw resource file.
            boolean success = googleMap.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(
                            this, R.raw.style_json));

            if (!success) {
                Log.e("MAPTAG", "Style parsing failed.");
            }
        } catch (Resources.NotFoundException e) {
            Log.e("MAPTAG", "Can't find style. Error: ", e);
        }

        //Get the user location
        LocationManager lm = (LocationManager)getSystemService(this.LOCATION_SERVICE);

        //Monitor the user location / Run GeoQuerie / Draw Circle around him
        //For Android
        //lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 3500, 0, new LocationListener() {
        //For Emulator
        lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 3500, 0, new LocationListener() {
            @Override
            public void onLocationChanged(@NonNull Location location) {
                if(firstTimeLoad){
                    firstTimeLoad = false;
                }

                //Update the latest known user location
                lastKnownUserLocation = new LatLng(location.getLatitude(), location.getLongitude());

                //Running a geoquerie refreshes all the markers on the search area.
                //Refreshing a marker closes its infowindow if it is open
                //If an info winodw is open, wait for the user to close it in order to resend a geoquerie, to avoid autoclosing the window

                if(!openInfoWindow && !markerSelectionMode){
                    //Run GeoQuery
                    geoQuerieHandler.runGeoQuery(new LatLng(location.getLatitude(), location.getLongitude()), 0.1f, googleMap);
                }

                if(trackUserLocation){
                    double longitude = location.getLongitude();
                    double latitude = location.getLatitude();

                    //Draw circle around user
                    drawSearchCircle(googleMap, latitude, longitude, 100);
                }
                if(cameraFollowsUser){
                    //Animate map on users coordinates
                    animateMapOnCoordinates(googleMap, location);
                }
                //If user distance traveled is recorded (during trip mode)
                if(recordMetersTraveled){
                    int distanceCovered = Math.round(distanceBetweenPoints(lastKnownUserLocation.latitude, lastKnownUserLocation.longitude, location.getLatitude(), location.getLongitude()));
                    userStatisticsTracker.addToDistanceTraveled(distanceCovered);
                }
            }
        });

        //Add listener for "click" event on the google map
        googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(@NonNull LatLng latLng) {
                //Pause the person camera tracking to focus on the new added marker
                if(openInfoWindow) {
                    //If there is an open info window, close it
                    openInfoWindow = false;
                    if(markerWithOpenInfoWindow.isInfoWindowShown()){
                        markerWithOpenInfoWindow.hideInfoWindow();
                    }

                }
                else if(markerAdditionMode){
                    //Record Map Click
                    actionRecorder.addAction(googleMap, "click",context);

                    pauseUserTracking();
                    pauseCameraTracking();

                    //Call the Marker Placement Activity and pass the geoQueryHandlerObject to the new Activity
                    Intent intent = new Intent(context, MarkerOnMapPlacementActivity.class);
                    //intent.putExtra("geoQueryHandlerObject", (Parcelable) geoQuerieHandler);
                    //Pass the clicked coordinates
                    intent.putExtra("LatLng", latLng);
                    //Pass the googleMap avoiding serialization of the object by Using EventBus
                    //Something like -> intent.putExtra("googleMapObject", (Serializable) googleMap);
                    EventBus.getDefault().postSticky(googleMap);
                    //Start the overlay activity
                    startActivity(intent);
                }
            }
        });

        googleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(@NonNull Marker marker) {
                //Marker Selection Mode During Trip Planning
                if(markerSelectionMode){
                    //Record Map Click
                    actionRecorder.addAction(marker, "click",context);

                    if(!selectedMarkersArrayList.contains(marker)){
                        BitmapDescriptor bitmapDescriptor = BitmapDescriptorFactory.fromResource(R.drawable.greenmarker);
                        marker.setIcon(bitmapDescriptor);

                        selectedMarkersArrayList.add(marker);
                    }else{
                        BitmapDescriptor bitmapDescriptor = BitmapDescriptorFactory.fromResource(R.drawable.purplemarker);
                        marker.setIcon(bitmapDescriptor);

                        selectedMarkersArrayList.remove(marker);
                    }

                    return true;
                }
                else{
                    markerWithOpenInfoWindow = marker;
                    //Notify the app that there is an open info window so the on-screen taps can be adjusted
                    openInfoWindow = true;

                    //Record the marker view stat
                    userStatisticsTracker.addStat("rampsviewed");

                    return false;
                }

            }
        });

        googleMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(@NonNull Marker marker) {
                //If NOT a temporary marker has been clicked
                if(!marker.getTitle().toString().equals("tempTitle")) {
                    //Record InfoWindow Click
                    actionRecorder.addAction(marker, "click (info-window)", context);

                    Intent intent = new Intent(context, MarkerDetailsActivity.class);
                    intent.putExtra("markerID", marker.getTitle());
                    startActivity(intent);
                    //Hide the open infoWindow
                    marker.hideInfoWindow();
                    openInfoWindow = false;
                }
            }
        });

        googleMap.setOnCameraMoveStartedListener(new GoogleMap.OnCameraMoveStartedListener() {
            @Override
            public void onCameraMoveStarted(int reason) {
                cameraMovementDetector(reason);
            }


        });
    }

    void addXPToUser(int givenXP){
        userReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot child: snapshot.getChildren()){
                    if(child.getKey().equals("statistics")){
                        String currentXP = child.child("xp").getValue().toString();
                        int finalXP = Integer.parseInt(currentXP) + givenXP;
                        userReference.child("statistics").child("xp").setValue(finalXP);
                        showUserLevelingNotification("XP_GAINED", Integer.parseInt(currentXP), givenXP, 0);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    //
    //
    // --- ACTIVITY REDIRECTORS --- //
    //
    //

    public void goToProfileTab(View view){
        try {
            //Close any open InfoWindows
            if (markerWithOpenInfoWindow.isInfoWindowShown()) {
                markerWithOpenInfoWindow.hideInfoWindow();
                openInfoWindow = false;
            }
        }
        catch(Exception e){
            //No markerWithOpenInfoWindow ever stored
        }
        //Record View Click
        actionRecorder.addAction(view, "click",this);

        Intent intent =new Intent(context, ProfileActivity.class);
        startActivity(intent);
    }

    public void goToNotificationsTab(View view){
        try {
            //Close any open InfoWindows
            if (markerWithOpenInfoWindow.isInfoWindowShown()) {
                markerWithOpenInfoWindow.hideInfoWindow();
                openInfoWindow = false;
            }
        }
        catch(Exception e){
            //No markerWithOpenInfoWindow ever stored
        }
        //Record View Click
        actionRecorder.addAction(view, "click",this);

        Intent intent =new Intent(context, NotificationSystem.class);
        startActivity(intent);
    }

    public void openMarkerGeneralPlacementActivity(View view){
        //Record View Click
        actionRecorder.addAction(view, "click",this);

        //Call the Marker Placement Activity and pass the geoQueryHandlerObject to the new Activity
        Intent intent = new Intent(context, MarkerGeneralPlacementActivity.class);

        //intent.putExtra("geoQueryHandlerObject", (Parcelable) geoQuerieHandler);
        //Pass the clicked coordinates
        //intent.putExtra("LatLng", latLng);
        //Pass the googleMap avoiding serialization of the object by Using EventBus
        //Something like -> intent.putExtra("googleMapObject", (Serializable) googleMap);
        EventBus.getDefault().postSticky(googleMap);
        //Start the overlay activity
        startActivity(intent);
        overridePendingTransition(R.anim.activity_slide_left_to_center, R.anim.activity_slide_center_to_up);

        hideTopBar();
        hideBottomBar();
    }

    //
    //
    // --- GOOGLE MAPS BASED FUNCTIONS (Routing etc) --- //
    //
    //

    //Trip Started - Request routing data from the api
    void requestFromRouteAPI(){
        //TODO remove the old polyline?
        //Stop Marker Selection Mode since the Trip has Started
        markerSelectionMode = false;

        //Record trip started statistic
        userStatisticsTracker.addStat("totaltrips");

        //Define list to get all latlng for the route
        List<LatLng> path = new ArrayList();

        //Execute Directions API request
        GeoApiContext geoApiContext = new GeoApiContext.Builder()
                .apiKey(apiKEY)
                .build();

        DirectionsApiRequest req = DirectionsApi.newRequest(geoApiContext)
                .mode(TravelMode.WALKING)
                .origin(lastKnownUserLocation.latitude + "," + lastKnownUserLocation.longitude)
                .destination(storedDestinationLatLng.latitude + "," + storedDestinationLatLng.longitude);
                //.waypoints(selectedMarkersArrayList.get(0).getPosition().latitude + "," + selectedMarkersArrayList.get(0).getPosition().longitude);

        //Add all the waypoints
        if(!selectedMarkersArrayList.isEmpty()) {

            //Pass the waypoints/ selected markers to the GeoQuerieHandler excludedMarkers array list, so they are always drawn on the map
            geoQuerieHandler.setupExcludedMarkersArrayList(selectedMarkersArrayList);

            String waypoints = "";
            for (int listLength = 0; listLength < selectedMarkersArrayList.size(); listLength++) {
                waypoints += selectedMarkersArrayList.get(listLength).getPosition().latitude + "," + selectedMarkersArrayList.get(listLength).getPosition().longitude + "|";
            }

            req.waypoints(waypoints);
            req.optimizeWaypoints(true);
        }

        try {
            DirectionsResult res = req.await();

            //Loop through legs and steps to get encoded polylines of each step
            if (res.routes != null && res.routes.length > 0) {
                DirectionsRoute route = res.routes[0];

                if (route.legs !=null) {
                    for(int i=0; i<route.legs.length; i++) {
                        DirectionsLeg leg = route.legs[i];
                        if (leg.steps != null) {
                            for (int j=0; j<leg.steps.length;j++){
                                DirectionsStep step = leg.steps[j];
                                if (step.steps != null && step.steps.length >0) {
                                    for (int k=0; k<step.steps.length;k++){
                                        DirectionsStep step1 = step.steps[k];
                                        EncodedPolyline points1 = step1.polyline;
                                        if (points1 != null) {
                                            //Decode polyline and add points to list of route coordinates
                                            List<com.google.maps.model.LatLng> coords1 = points1.decodePath();
                                            for (com.google.maps.model.LatLng coord1 : coords1) {
                                                path.add(new LatLng(coord1.lat, coord1.lng));
                                            }
                                        }
                                    }
                                } else {
                                    EncodedPolyline points = step.polyline;
                                    if (points != null) {
                                        //Decode polyline and add points to list of route coordinates
                                        List<com.google.maps.model.LatLng> coords = points.decodePath();
                                        for (com.google.maps.model.LatLng coord : coords) {
                                            path.add(new LatLng(coord.lat, coord.lng));
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } catch(Exception ex) {
            Log.d("TAG", "Routing error");
        }

        //Draw the polyline
        if (path.size() > 0) {
            PolylineOptions opts = new PolylineOptions().addAll(path).color(Color.parseColor("#908ec4")).width(20);
            googleMap.addPolyline(opts);
        }

        //Record the ramp used stat
        userStatisticsTracker.addStat("rampsused", selectedMarkersArrayList.size());

        //Empty the selectedMarkersArrayList
        selectedMarkersArrayList.clear();

        changeConfirmationBarMode("Safe Travels", "TRAVEL_MODE");

        startUserTracking();

        recordMetersTraveled = true;
    }

    void pauseUserTracking(){
        //Pause user tracking
        trackUserLocation = false;
        //Enable the Resume Tracking Button
        trackUserLocationButton.setVisibility(View.VISIBLE);
    }

    void startUserTracking(){
        //Start user tracking
        trackUserLocation = true;
        //Animate Camera th lastKnownUserLocation and update the location (Makes the app feel more responsive)
        animateMapOnCoordinates(googleMap, lastKnownUserLocation);
        //Disable the Resume Tracking Button
        trackUserLocationButton.setVisibility(View.INVISIBLE);
    }

    void pauseCameraTracking(){
        cameraFollowsUser = false;
    }

    void startCameraTracking(){
        try {
            //Close any open InfoWindows
            if (markerWithOpenInfoWindow.isInfoWindowShown()) {
                markerWithOpenInfoWindow.hideInfoWindow();
                openInfoWindow = false;
            }
        }
        catch(Exception e){
            //No markerWithOpenInfoWindow ever stored
        }
            //Makes the camera tracking feel more responsive since it is not waiting for 3.5max seconds to track the new user location.
            animateMapOnCoordinates(googleMap, lastKnownUserLocation);

            cameraFollowsUser = true;
    }

    //Used for the track button to call startUserTracking()
    public void callStartCameraTracking(View view){
        //Record View Click
        actionRecorder.addAction(view, "click",this);

        startCameraTracking();
    }

    //Begin the setup of the user trip
    public void startRoutingProcess(View view, LatLng destinationLatLng){
        //Record View Click
        actionRecorder.addAction(view, "click", context);

        //PART 1: Marker Load
        //Get start + destination LatLng and find the Middle Point.
        //Fire a single GeoFire Query on the Middle Point with Range (LengthBetweenStartFinish / 2)

        //Initialization
        LatLng startLatLng = lastKnownUserLocation;
        Log.d("EDW1", startLatLng.latitude + " " + startLatLng.longitude );
        Log.d("EDW2", destinationLatLng.latitude + " " + destinationLatLng.longitude );

        //Get the Middle Point
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        builder.include(startLatLng).include(destinationLatLng);
        LatLng middlePointLatLng = builder.build().getCenter();
        Log.d("EDW3", middlePointLatLng.latitude + " " + middlePointLatLng.longitude );

        //Get the required radius
        float radiusFloat = distanceBetweenPoints(middlePointLatLng.latitude, middlePointLatLng.longitude, destinationLatLng.latitude, destinationLatLng.longitude);
        //Convert to km
        radiusFloat = radiusFloat / 1000;
        Log.d("EDW4", radiusFloat + "");

        //(radiusFloat * 1000) + 0.5 *(radiusFloat * 1000) is a +150% increased radius circle for better ramp results

        //Begin markerSelection mode
        markerSelectionMode = true;
        //Fire a single geoQuerie at middlePointLatLng
        //geoQuerieHandler.clearRunningListeners();
        geoQuerieHandler.runGeoQuery(middlePointLatLng, radiusFloat + (0.75 * radiusFloat), googleMap);

        //Draw search circle
        drawSearchCircle(googleMap, middlePointLatLng.latitude, middlePointLatLng.longitude, (radiusFloat * 1000) + 0.75 *(radiusFloat * 1000));
        changeConfirmationBarMode("Begin your journey?", "MARKER_SELECTION");

        //Zoom out the camera to fit the markers
        //Change the padding as per needed
        CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(builder.build(), 200);
        googleMap.animateCamera(cu);
    }

    //Draws a circle on the provided googleMap + removes the old one onLocationChanged
    public void drawSearchCircle(GoogleMap googleMap, double latitude, double longitude, double radius){
        if(oldUserSearchCircle != null) oldUserSearchCircle.remove();
        //Draw a circle on the googleMap
        oldUserSearchCircle = googleMap.addCircle(new CircleOptions()
                .center(new LatLng(latitude, longitude))
                .radius(radius)
                .strokeColor(Color.parseColor("#605DB3"))
                .strokeWidth(4)
                .fillColor(Color.parseColor("#2F676986")));
    }

    //Search for the given location in the searchbar
    public void searchLocation(View view){
        try {
            //Close any open InfoWindows
            if (markerWithOpenInfoWindow.isInfoWindowShown()) {
                markerWithOpenInfoWindow.hideInfoWindow();
                openInfoWindow = false;
            }
        }
        catch(Exception e){
            //No markerWithOpenInfoWindow ever stored
        }
        //Hide the keyboard
        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);

        //Record View Click
        actionRecorder.addAction(view, "click", context);

        String address = searchBarEditText.getText().toString();

        pauseCameraTracking();

        Geocoder geoCoder = new Geocoder(context, Locale.getDefault());
        try
        {
            List<Address> addresses = geoCoder.getFromLocationName(address, 5);
            if (addresses.size() > 0)
            {
                Double lat = (double) (addresses.get(0).getLatitude());
                Double lon = (double) (addresses.get(0).getLongitude());

                int count = 0;
                while(lat == null || lon == null && count < addresses.size()){
                    lat = (double) (addresses.get(0).getLatitude());
                    lon = (double) (addresses.get(0).getLongitude());
                    count ++;
                }

                final LatLng user = new LatLng(lat, lon);

                //Store the LatLng of the detination
                storedDestinationLatLng = user;

                //Add a temporary visual marker only - No connection with the database
                BitmapDescriptor bitmapDescriptor = BitmapDescriptorFactory.fromResource(R.drawable.circle_marker_ref);
                GroundOverlay tempRouteMarker = googleMap.addGroundOverlay(new GroundOverlayOptions()
                        .position(user, 20f, 20f)
                        .image(bitmapDescriptor));

                //Add a temporary visual circle to help the user understand which marker he is placing
//                tempRouteCircle = googleMap.addCircle(new CircleOptions()
//                        .center(user)
//                        .radius(10)
//                        .strokeColor(Color.parseColor("#676986"))
//                        .strokeWidth(4));

                //Animate camera OVER the marker marker
                double newLat = user.latitude;
                double newLong = user.longitude;

                // Move the camera instantly to location
                CameraPosition cameraPosition = new CameraPosition.Builder()
                        .target(new LatLng(newLat, newLong))             // Sets the center of the map to location user
                        .zoom(19)                   // Sets the zoom
                        .bearing(0)                 // Sets the orientation of the camera
                        .tilt(0)                   // Sets the tilt of the camera
                        .build();                   // Creates a CameraPosition from the builder
                googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

                //Hide the bottom navigation bar
                hideBottomBar();
                //Open the confirmation bar
                openConfirmationBar();
                changeConfirmationBarMode("Is this your destination ?", "CONFIRM_DESTINATION");
            }
        }
        catch (IOException e)
        {
            Toast.makeText(context, "Invalid Address or Non Responsive. Please try again.", Toast.LENGTH_SHORT).show();
        }
    }

    public float distanceBetweenPoints (double lat_a, double lng_a, double lat_b, double lng_b )
    {
        double earthRadius = 3958.75;
        double latDiff = Math.toRadians(lat_b-lat_a);
        double lngDiff = Math.toRadians(lng_b-lng_a);
        double a = Math.sin(latDiff /2) * Math.sin(latDiff /2) +
                Math.cos(Math.toRadians(lat_a)) * Math.cos(Math.toRadians(lat_b)) *
                        Math.sin(lngDiff /2) * Math.sin(lngDiff /2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        double distance = earthRadius * c;

        int meterConversion = 1609;

        return new Float(distance * meterConversion).floatValue();
    }

    //
    //
    // --- CAMERA ANIMATORS --- //
    //
    //

    void cameraMovementDetector(int reason){
        //Check for camera movements.
        //
        // Case 1: The user gestured on the map
        //  Pause automatic camera tracking so the user can browse around the map
        // Case 2: The user tapped on the map on the map
        //  Pause automatic camera tracking so the user can browse around the map
        // Case 3: The app moved the camera.
        //  Happens onLocationChanged and tracks the user

        //On Map: Drag, Pinch, Rotate, Tap
        // Freeze the camera tracking. Let the user browse the map
        // Show a button to resume tracking

        //GESTURE
        if (reason == 1) {
            pauseCameraTracking();
            trackUserLocationButton.setVisibility(View.VISIBLE);
            //Toast.makeText(context, "The user gestured on the map.",
            //        Toast.LENGTH_SHORT).show();

        //REASON_API_ANIMATION
        } else if (reason == 2) {
            pauseCameraTracking();
            trackUserLocationButton.setVisibility(View.VISIBLE);
            //Toast.makeText(context, "The user tapped something on the map.",
            //        Toast.LENGTH_SHORT).show();

        //REASON_DEVELOPER_ANIMATION
        } else if (reason == 3) {
            trackUserLocationButton.setVisibility(View.GONE);
            //Toast.makeText(context, "The app moved the camera.",
            //        Toast.LENGTH_SHORT).show();
        }
    }

    //Swap between Tracking and Paused states depending if the user wants to browse on the map or not
    public void changeCameraAnimationState(View view){
        if(!trackUserLocation){
            startUserTracking();
        }
    }

    void animateMapOnCoordinates(GoogleMap googleMap, Location location){
        try {
            if (firstTimeLoad) {
                CameraPosition cameraPosition = new CameraPosition.Builder()
                        .target(new LatLng(location.getLatitude(), location.getLongitude()))      // Sets the center of the map to location user
                        .zoom(19)                   // Sets the zoom
                        .bearing(0)                // Sets the orientation of the camera
                        .tilt(30)                   // Sets the tilt of the camera
                        .build();                   // Creates a CameraPosition from the builder
                googleMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
            }
            if (cameraFollowsUser) {
                CameraPosition cameraPosition = new CameraPosition.Builder()
                        .target(new LatLng(location.getLatitude(), location.getLongitude()))      // Sets the center of the map to location user
                        .zoom(19)                   // Sets the zoom
                        .bearing(0)                // Sets the orientation of the camera
                        .tilt(30)                   // Sets the tilt of the camera
                        .build();                   // Creates a CameraPosition from the builder
                googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
            }
        }
        catch(Exception e){
            Toast.makeText(this, "Camera Animation Error. Location provided not valid", Toast.LENGTH_SHORT).show();
        }
    }

    void animateMapOnCoordinates(GoogleMap googleMap, LatLng location){
        if(trackUserLocation) {
            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(new LatLng(location.latitude, location.longitude))      // Sets the center of the map to location user
                    .zoom(19)                   // Sets the zoom
                    .bearing(0)                // Sets the orientation of the camera
                    .tilt(30)                   // Sets the tilt of the camera
                    .build();                   // Creates a CameraPosition from the builder
            googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        }
    }

    //
    //
    // --- UI MANAGEMENT FUNCTIONS --- //
    //
    //

    void showUserLevelingNotification(String notificationType, int currentXPAmount, int xpEarned, int userLevel){
        // XP SYSTEM
        // ---
        // LEVEL 1: 100XP
        // LEVEL 2: 100XP x2 + 25XP = 225XP
        // LEVEL 3: 225XP x2 + 25XP = 475XP
        // LEVEL 4: 475XP x2 + 25XP = 975XP
        // LEVEL 5: 975XP x2 + 25XP = 1975XP
        // LEVEL 6: 1975XP x2 + 25XP = 3975XP
        // LEVEL 7: 3975XP x2 + 25XP = 7975XP
        // LEVEL 8: 7975XP x2 + 25XP = 15975XP
        //
        // XP REWARDS:
        // ---
        // MARKER PLACEMENT: 25XP
        // MARKER LIKED: 5XP

        if(notificationType.equals("LEVEL_UP")){
            LayoutInflater inflater = getLayoutInflater();
            View layout = inflater.inflate(R.layout.custom_level_up_toast,
                    (ViewGroup) findViewById(R.id.levelUpBackground));

            TextView levelUpText = (TextView) layout.findViewById(R.id.customLevelTextView);
            Log.d("EDW", levelUpText.getId() + "");
            levelUpText.setText(userLevel + "");

            Toast toast = new Toast(getApplicationContext());
            toast.setGravity(Gravity.CENTER_VERTICAL, 0, 600);
            toast.setDuration(Toast.LENGTH_LONG);
            toast.setView(layout);
            toast.show();

            View layout2 = inflater.inflate(R.layout.custom_badge_unlocked_toast,
                    (ViewGroup) findViewById(R.id.levelUpBackground2));

            ImageView badgeUnlockedImageView = (ImageView) layout2.findViewById(R.id.badgeUnlockedImager);

            if(userLevel == 1) {
                badgeUnlockedImageView.setImageResource(R.drawable.rising_star);
            }
            else if(userLevel == 2) {
                badgeUnlockedImageView.setImageResource(R.drawable.star_badge);
            }
            else if(userLevel == 3) {
                badgeUnlockedImageView.setImageResource(R.drawable.star_medal);
            }
            else if(userLevel == 4) {
                badgeUnlockedImageView.setImageResource(R.drawable.star_award);
            }
            else if(userLevel == 5) {
                badgeUnlockedImageView.setImageResource(R.drawable.star_collector);
            }
            else if(userLevel == 6) {
                badgeUnlockedImageView.setImageResource(R.drawable.star_pro);
            }
            else if(userLevel == 7) {
                badgeUnlockedImageView.setImageResource(R.drawable.star_master);
            }
            else if(userLevel == 8) {
                badgeUnlockedImageView.setImageResource(R.drawable.star_king);
            }

            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    //Do something after toast.getDuration()
                    Toast toast2 = new Toast(getApplicationContext());
                    toast2.setGravity(Gravity.CENTER_VERTICAL, 0, 600);
                    toast2.setDuration(Toast.LENGTH_LONG);
                    toast2.setView(layout2);
                    toast2.show();
                }
            }, toast.getDuration());
        }
        else if(notificationType.equals("XP_GAINED")){
            LayoutInflater inflater = getLayoutInflater();
            View layout = inflater.inflate(R.layout.custom_xp_gained_toast,
                    (ViewGroup) findViewById(R.id.toast_layout_root));

            TextView xpGainedText = (TextView) layout.findViewById(R.id.customXPAmountTextView);
            xpGainedText.setText("+ " + xpEarned + " XP");

            Toast toast = new Toast(getApplicationContext());
            toast.setGravity(Gravity.CENTER_VERTICAL, 0, 600);
            toast.setDuration(Toast.LENGTH_SHORT);
            toast.setView(layout);
            toast.show();

            //Check if the user will level up. If leveled up, show level up notification
            if(currentXPAmount < 100 && currentXPAmount + xpEarned >= 100) showUserLevelingNotification("LEVEL_UP", 0, 0, 1);
            else if(currentXPAmount < 225 && currentXPAmount + xpEarned >= 225) showUserLevelingNotification("LEVEL_UP", 0, 0, 2);
            else if(currentXPAmount < 475 && currentXPAmount + xpEarned >= 475) showUserLevelingNotification("LEVEL_UP", 0, 0, 3);
            else if(currentXPAmount < 975 && currentXPAmount + xpEarned >= 975) showUserLevelingNotification("LEVEL_UP", 0, 0, 4);
            else if(currentXPAmount < 1975 && currentXPAmount + xpEarned >= 1975) showUserLevelingNotification("LEVEL_UP", 0, 0, 5);
            else if(currentXPAmount < 3975 && currentXPAmount + xpEarned >= 3975) showUserLevelingNotification("LEVEL_UP", 0, 0, 6);
            else if(currentXPAmount < 7975 && currentXPAmount + xpEarned >= 7975) showUserLevelingNotification("LEVEL_UP", 0, 0, 7);
            else if(currentXPAmount < 15975 && currentXPAmount + xpEarned >= 15975) showUserLevelingNotification("LEVEL_UP", 0, 0, 8);
        }
    }

    public void cancelDestinationPoint(View view){
        //Show the bottom navigation bar
        showBottomBar();
        //close the confirmation bar
        closeConfirmationBar();

        startUserTracking();
        startCameraTracking();
    }

    void introAnimation(){
        communityButton.startAnimation(AnimationUtils.loadAnimation(context,R.anim.slide_right));
        plusButton.startAnimation(AnimationUtils.loadAnimation(context,R.anim.slide_down));
        profileButton.startAnimation(AnimationUtils.loadAnimation(context,R.anim.slide_left));
    }

    //Manages the confirmation bar ui, adapts it depending on the state of the app
    void changeConfirmationBarMode(String title, String mode){
        if(mode.equals("MARKER_SELECTION")){
            //Pause user + camera tracking
            pauseUserTracking();
            pauseCameraTracking();

            //Remove leftover markers
            geoQuerieHandler.removeOutOfBoundsMarkers();

            //Change the title
            confirmDestinationPointTextView.setText(title);

            //Update the onClick events for the buttons
            confirmDestinationPointButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view)
                {
                    //Record View Click
                    actionRecorder.addAction(view, "click",context);

                    //Fetch data from google maps routing api
                    requestFromRouteAPI();
                }
            });

            cancelDestinationPointButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //Record View Click
                    actionRecorder.addAction(view, "click",context);

                    cancelDestinationPoint(view);
                    hideMarkerSelectionTopBar();
                    showTopBar();
                    //disable marker selection mode
                    markerSelectionMode = false;
                    //clear the marker list
                    selectedMarkersArrayList.clear();
                }
            });
        }
        else if(mode.equals("CONFIRM_DESTINATION")){
            //Pause user + camera tracking
            pauseUserTracking();
            pauseCameraTracking();

            //Hide the tracking button
            trackUserLocationButton.setVisibility(View.GONE);

            //Change the title
            confirmDestinationPointTextView.setText(title);

            //Update the onClick events for the buttons
            confirmDestinationPointButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //Record View Click
                    actionRecorder.addAction(view, "click",context);
                    //Show marker selection UI
                    hideTopBar();
                    showMarkerSelectionTopBar();
                    //Begin routing process
                    startRoutingProcess(view, storedDestinationLatLng);
                }
            });

            cancelDestinationPointButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //Record View Click
                    actionRecorder.addAction(view, "click",context);
                    cancelDestinationPoint(view);

                    markerSelectionMode = false;


                }
            });
        }
        else if(mode.equals("TRAVEL_MODE")){
            //Resume user + camera tracking
            startUserTracking();
            startCameraTracking();

            confirmDestinationPointTextView.setText(title);
            confirmDestinationPointTextView.setTextColor(getColor(R.color.white));
            confirmDestinationPointButton.setText("FINISH TRIP");
            confirmDestinationPointButton.setBackgroundDrawable(getDrawable(R.drawable.round_tab_white_stroke));
            cancelDestinationPointButton.setText("CANCEL TRIP");
            cancelDestinationPointButton.setBackgroundDrawable(getDrawable(R.drawable.round_tab_red_stroke));

            backgroundButton2.setBackgroundColor(getColor(R.color.minimalDarkPurple));

            confirmDestinationPointButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //Record View Click
                    actionRecorder.addAction(view, "click",context);

                    closeConfirmationBar();
                    showTopBar();
                    showBottomBar();

                    //Empty the text from the Buttons
                    confirmDestinationPointButton.setText("");
                    cancelDestinationPointButton.setText("");

                    recordMetersTraveled = false;
                    //Store the distance traveled
                    userStatisticsTracker.saveDistanceTraveled();

                    //Trip Ended. Reward 200 XP
                    addXPToUser(200);

                    //Clear all the excluded markers (markers on the polyline/ selected during the marker selection faze)
                    geoQuerieHandler.clearExcludedMarkersArrayList();
                }
            });

            cancelDestinationPointButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //Record View Click
                    actionRecorder.addAction(view, "click",context);

                    closeConfirmationBar();
                    showBottomBar();
                    showTopBar();
                    recordMetersTraveled = false;
                    //Store the distance traveled
                    userStatisticsTracker.saveDistanceTraveled();

                    //Resume user tracking
                    startUserTracking();

                    //Empty the text from the Buttons
                    confirmDestinationPointButton.setText("");
                    cancelDestinationPointButton.setText("");

                    //Trip Canceled

                    //Clear all the excluded markers (markers on the polyline/ selected during the marker selection faze)
                    geoQuerieHandler.clearExcludedMarkersArrayList();
                }
            });
            hideMarkerSelectionTopBar();
        }
    }

    void hideBottomBar(){
        //Setup dropping bar animation
        final Animation hideBar = new TranslateAnimation(0,0,0,500);
        hideBar.setDuration(1000);
        hideBar.setFillAfter(false);

        backgroundButton.startAnimation(hideBar);
        communityButton.startAnimation(hideBar);
        profileButton.startAnimation(hideBar);
        plusButton.startAnimation(hideBar);

        hideBar.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                backgroundButton.clearAnimation();
                backgroundButton.setVisibility(View.GONE);
                communityButton.clearAnimation();
                communityButton.setVisibility(View.GONE);
                profileButton.clearAnimation();
                profileButton.setVisibility(View.GONE);
                plusButton.clearAnimation();
                plusButton.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    void showBottomBar(){
        //Setup dropping bar animation
        final Animation showBar = new TranslateAnimation(0,0,500,0);
        showBar.setDuration(1000);
        showBar.setFillAfter(true);

        backgroundButton.startAnimation(showBar);
        backgroundButton.setVisibility(View.VISIBLE);
        communityButton.startAnimation(showBar);
        communityButton.setVisibility(View.VISIBLE);
        profileButton.startAnimation(showBar);
        profileButton.setVisibility(View.VISIBLE);
        plusButton.startAnimation(showBar);
        plusButton.setVisibility(View.VISIBLE);
    }

    void hideTopBar(){
        //Setup rising bar animation
        final Animation hideBar = new TranslateAnimation(0,0,0,-500);
        hideBar.setDuration(1000);
        hideBar.setFillAfter(false);

        searchBarEditText.startAnimation(hideBar);
        searchLocationButton.startAnimation(hideBar);

        hideBar.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                searchBarEditText.clearAnimation();
                searchBarEditText.setVisibility(View.GONE);
                searchLocationButton.clearAnimation();
                searchLocationButton.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    void showTopBar(){
        //Setup dropping bar animation
        final Animation showBar = new TranslateAnimation(0,0,-500,0);
        showBar.setDuration(1000);
        showBar.setFillAfter(true);

        searchBarEditText.startAnimation(showBar);
        searchBarEditText.setVisibility(View.VISIBLE);
        searchLocationButton.startAnimation(showBar);
        searchLocationButton.setVisibility(View.VISIBLE);
    }

    void showMarkerSelectionTopBar(){
        //Setup dropping bar animation
        final Animation showBar = new TranslateAnimation(0,0,-500,0);
        showBar.setDuration(1000);
        showBar.setFillAfter(true);

        markerSelectionBackgroundButton.startAnimation(showBar);
        markerSelectionBackgroundButton.setVisibility(View.VISIBLE);
        markerSelectionImage.startAnimation(showBar);
        markerSelectionImage.setVisibility(View.VISIBLE);
        rampSelectionTitleTextView.startAnimation(showBar);
        rampSelectionTitleTextView.setVisibility(View.VISIBLE);
        rampSelectionDescriptionTextView.startAnimation(showBar);
        rampSelectionDescriptionTextView.setVisibility(View.VISIBLE);
    }

    void hideMarkerSelectionTopBar(){
        //Setup rising bar animation
        final Animation showBar = new TranslateAnimation(0,0,0,-500);
        showBar.setDuration(1000);
        showBar.setFillAfter(true);

        markerSelectionBackgroundButton.startAnimation(showBar);
        markerSelectionImage.startAnimation(showBar);
        rampSelectionTitleTextView.startAnimation(showBar);
        rampSelectionDescriptionTextView.startAnimation(showBar);

        showBar.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                markerSelectionBackgroundButton.clearAnimation();
                markerSelectionBackgroundButton.setVisibility(View.GONE);
                markerSelectionImage.clearAnimation();
                markerSelectionImage.setVisibility(View.GONE);
                rampSelectionTitleTextView.clearAnimation();
                rampSelectionTitleTextView.setVisibility(View.GONE);
                rampSelectionDescriptionTextView.clearAnimation();
                rampSelectionDescriptionTextView.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    void openConfirmationBar(){
        //Setup rising secondary bar animation
        final Animation showBar = new TranslateAnimation(0,0,500,0);
        showBar.setDuration(1000);
        showBar.setFillAfter(true);
        backgroundButton2.startAnimation(showBar);
        backgroundButton2.setVisibility(View.VISIBLE);
        backgroundButton2.setBackgroundColor(getColor(R.color.white));
        confirmDestinationPointButton.startAnimation(showBar);
        confirmDestinationPointButton.setBackgroundDrawable(getDrawable(R.drawable.confirm_button));
        confirmDestinationPointButton.setVisibility(View.VISIBLE);
        cancelDestinationPointButton.startAnimation(showBar);
        cancelDestinationPointButton.setVisibility(View.VISIBLE);
        cancelDestinationPointButton.setBackgroundDrawable(getDrawable(R.drawable.cancel_button));
        confirmDestinationPointTextView.startAnimation(showBar);
        confirmDestinationPointTextView.setVisibility(View.VISIBLE);
        confirmDestinationPointTextView.setTextColor(getColor(R.color.minimalDarkPurple));
    }

    void closeConfirmationBar(){

        //Clear the temp Markers
        googleMap.clear();

        //Setup dropping secondary bar animation
        final Animation showBar = new TranslateAnimation(0,0,0,500);
        showBar.setDuration(1000);
        showBar.setFillAfter(true);

        backgroundButton2.startAnimation(showBar);
        confirmDestinationPointButton.startAnimation(showBar);
        cancelDestinationPointButton.startAnimation(showBar);
        confirmDestinationPointTextView.startAnimation(showBar);


        showBar.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                backgroundButton2.clearAnimation();
                backgroundButton2.setVisibility(View.GONE);
                confirmDestinationPointButton.clearAnimation();
                confirmDestinationPointButton.setVisibility(View.GONE);
                cancelDestinationPointButton.clearAnimation();
                cancelDestinationPointButton.setVisibility(View.GONE);
                confirmDestinationPointTextView.clearAnimation();
                confirmDestinationPointTextView.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    //
    //
    // --- GOOGLE MAPS SPARE FUNCTIONS --- //
    //
    //

    @Override
    protected void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState, @NonNull PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

//    //GET THE BEST USER LOCATION AVAILABLE - Monitor the user location / Run GeoQuerie / Draw Circle around him
//    private Location getLastKnownLocation(LocationManager mLocationManager) {
//        mLocationManager = (LocationManager)getApplicationContext().getSystemService(LOCATION_SERVICE);
//        List<String> providers = mLocationManager.getProviders(true);
//        Location bestLocation = null;
//        for (String provider : providers) {
//            @SuppressLint("MissingPermission") Location l = mLocationManager.getLastKnownLocation(provider);
//            if (l == null) {
//                continue;
//            }
//            if (bestLocation == null || l.getAccuracy() < bestLocation.getAccuracy()) {
//                // Found best last known location: %s", l);
//                bestLocation = l;
//            }
//        }
//        return bestLocation;
//    }

}