package ProfileSystem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.example.onwheelsapi28android90.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import Helpers.ActionRecorder;

public class TravelStatisticsActivity extends AppCompatActivity {

    FirebaseDatabase firebaseDatabase;
    DatabaseReference userReference, userStatisticsReference;
    String UID;

    TextView totalTripsCounterTextView, distanceTraveledCounterTextView, rampsViewedCounterTextView, rampsUsedCounterTextView, rampsPlacedCounterTextView;
    TextView rampsLikedCounterTextView, rampsDislikedCounterTextView, rampsEnabledCounterTextView, rampsDisabledCounterTextView;
    TextView messagesSentCounterTextView, chatRoomsCreatedCounterTextView, chatRoomsJoinedCounterTextView;

    ActionRecorder actionRecorder = new ActionRecorder();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_travel_statistics);

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

        //Get UI Elements
        totalTripsCounterTextView = (TextView) findViewById(R.id.totalTripsCounterTextView);
        distanceTraveledCounterTextView = (TextView) findViewById(R.id.distanceTraveledCounterTextView);
        rampsViewedCounterTextView = (TextView) findViewById(R.id.rampsViewedCounterTextView);
        rampsUsedCounterTextView = (TextView) findViewById(R.id.rampsUsedCounterTextView);
        rampsPlacedCounterTextView = (TextView) findViewById(R.id.rampsPlacedCounterTextView);
        rampsLikedCounterTextView = (TextView) findViewById(R.id.rampsLikedCounterTextView);
        rampsDislikedCounterTextView = (TextView) findViewById(R.id.rampsDislikedCounterTextView);
        rampsEnabledCounterTextView = (TextView) findViewById(R.id.rampsEnabledCounterTextView);
        rampsDisabledCounterTextView = (TextView) findViewById(R.id.rampsDisabledCounterTextView);
        messagesSentCounterTextView = (TextView) findViewById(R.id.messagesSentCounterTextView);
        chatRoomsCreatedCounterTextView = (TextView) findViewById(R.id.chatRoomsCreatedCounterTextView);
        chatRoomsJoinedCounterTextView = (TextView) findViewById(R.id.chatRoomsJoinedCounterTextView);

        //Setup Firebase
        //Get Logged in user UID
        UID = FirebaseAuth.getInstance().getUid();

        //Get firebase realtime database reference
        firebaseDatabase = FirebaseDatabase.getInstance();
        userReference = firebaseDatabase.getReference().child("users").child(UID);
        userStatisticsReference = userReference.child("statistics");

        //Sync all the UI Elements with the Database
        userStatisticsReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                totalTripsCounterTextView.setText(snapshot.child("totaltrips").getValue() + " Trips");
                distanceTraveledCounterTextView.setText(snapshot.child("distancetraveled").getValue() + " Meters");
                rampsViewedCounterTextView.setText(snapshot.child("rampsviewed").getValue() + " Ramps");
                rampsUsedCounterTextView.setText(snapshot.child("rampsused").getValue() + " Ramps");
                rampsPlacedCounterTextView.setText(snapshot.child("rampsplaced").getValue() + " Ramps");
                rampsLikedCounterTextView.setText(snapshot.child("rampsliked").getValue() + " Ramps");
                rampsDislikedCounterTextView.setText(snapshot.child("rampsdisliked").getValue() + " Ramps");
                rampsEnabledCounterTextView.setText(snapshot.child("rampsenabled").getValue() + " Ramps");
                rampsDisabledCounterTextView.setText(snapshot.child("rampsdisabled").getValue() + " Ramps");
                messagesSentCounterTextView.setText(snapshot.child("messagessent").getValue() + " Messages");
                chatRoomsCreatedCounterTextView.setText(snapshot.child("chatroomscreated").getValue() + " Rooms Created");
                chatRoomsJoinedCounterTextView.setText(snapshot.child("chatroomsjoined").getValue() + " Rooms Joined");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    public void goToProfileTab(View view){
        //Record View Click
        actionRecorder.addAction(view, "click", this);

        finish();
    }
}