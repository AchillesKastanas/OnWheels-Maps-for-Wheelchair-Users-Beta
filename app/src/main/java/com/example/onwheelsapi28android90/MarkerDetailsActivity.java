package com.example.onwheelsapi28android90;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.crashlytics.buildtools.reloc.com.google.common.base.CharMatcher;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.w3c.dom.Text;

import Helpers.ActionRecorder;
import ProfileSystem.UserStatisticsTracker;

public class MarkerDetailsActivity extends AppCompatActivity {

    DatabaseReference markerDetailsReference;
    String UID;
    String markerID;
    String markerAvailability = ""; // Available - Unavailable

    TextView markerTitleTextView, markerTypeTextView, markerNotesTextView, markerAvailabilityTextView, markerAvailabilityTextView2, markerLikesTextView, markerDislikesTextView, markerViewsTextView, markerIdTextView;
    AppCompatButton likeButton, dislikeButton, markAvailabilityButton, reportMarkerButton;

    UserStatisticsTracker userStatisticsTracker = new UserStatisticsTracker();

    ActionRecorder actionRecorder = new ActionRecorder();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_marker_details);

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

        //Get markerID
        markerID = getIntent().getExtras().getString("markerID");

        //Setup FirebaseDatabase
        markerDetailsReference = FirebaseDatabase.getInstance().getReference().child("geofire").child(markerID).child("markerdetails");
        UID = FirebaseAuth.getInstance().getUid();

        //Locate the UI Elements
        markerTitleTextView = (TextView) findViewById(R.id.markerTitleTextView);
        markerTypeTextView = (TextView) findViewById(R.id.markerTypeTextView);
        markerNotesTextView = (TextView) findViewById(R.id.markerNotesTextView);
        markerAvailabilityTextView = (TextView) findViewById(R.id.markerAvailabilityTextView);
        markerAvailabilityTextView2 = (TextView) findViewById(R.id.markerAvailabilityTextView2);
        markerLikesTextView = (TextView) findViewById(R.id.markerLikesTextView);
        markerDislikesTextView = (TextView) findViewById(R.id.markerDislikesTextView);
        markerViewsTextView = (TextView) findViewById(R.id.markerViewsTextView);
        markerIdTextView = (TextView) findViewById(R.id.markerIdTextView);
        likeButton = (AppCompatButton) findViewById(R.id.likeButton);
        dislikeButton = (AppCompatButton) findViewById(R.id.dislikeButton);
        markAvailabilityButton = (AppCompatButton) findViewById(R.id.markAvailabilityButton);
        reportMarkerButton = (AppCompatButton) findViewById(R.id.reportMarkerButton);

        //Check if the currently signed in user has liked/ disliked the marker before, and update the ui accordingly
        markerDetailsReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean userInLikes = false;
                boolean userInDislikes = false;

                //If user exists in the likes accounts section
                for(DataSnapshot likedUsers: snapshot.child("likes").child("accounts").getChildren()){
                    if(likedUsers.getValue().equals(UID)){
                        //User located in the likes section
                        userInLikes = true;
                        break;
                    }
                }
                if(!userInLikes){
                    //User was not in likes section
                    //If user exists in the dislikes accounts section
                    for(DataSnapshot dislikedUsers: snapshot.child("dislikes").child("accounts").getChildren()){
                        if(dislikedUsers.getValue().equals(UID)){
                            //User located in the likes section
                            userInDislikes = true;
                            break;
                        }
                    }
                }

                //Check the LoopResults
                if(userInLikes){
                    //Update UI
                    likeButton.setBackgroundDrawable(getResources().getDrawable(R.drawable.round_like_button_selected));
                    likeButton.setClickable(false);

                    dislikeButton.setVisibility(View.INVISIBLE);
                    dislikeButton.setClickable(false);
                }
                if(userInDislikes){
                    //Update UI
                    dislikeButton.setBackgroundDrawable(getResources().getDrawable(R.drawable.round_dislike_button_selected));
                    dislikeButton.setClickable(false);

                    likeButton.setVisibility(View.INVISIBLE);
                    likeButton.setClickable(false);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        //Update the UI with the Database Data
        markerDetailsReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                markerTitleTextView.setText(snapshot.child("title").getValue().toString());
                markerTypeTextView.setText(snapshot.child("type").getValue().toString());
                markerNotesTextView.setText(snapshot.child("notes").getValue().toString());
                markerLikesTextView.setText("  " + numberRounder(snapshot.child("likes").child("amount").getValue().toString()));
                markerDislikesTextView.setText("  " + numberRounder(snapshot.child("dislikes").child("amount").getValue().toString()));
                markerViewsTextView.setText("  " + numberRounder(snapshot.child("views").child("amount").getValue().toString()));
                markerIdTextView.setText(markerID);


                //Setup marker availability colors
                markerAvailability = snapshot.child("availability").getValue().toString();
                markerAvailabilityTextView.setText(markerAvailability);
                markerAvailabilityTextView2.setText(markerAvailability);
                //The markerAvailabilityTextView is already set to green accents by default. In case the marker is unavailable, change the colors to red
                if(markerAvailability.equals("unavailable")){
                    markerAvailabilityTextView.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FFFFDFDF")));
                    markerAvailabilityTextView2.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FFFFDFDF")));
                    markerAvailabilityTextView.setTextColor(Color.parseColor("#FF866767"));
                    markerAvailabilityTextView2.setTextColor(Color.parseColor("#FF866767"));
                    markAvailabilityButton.setText("Mark as AVAILABLE");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void likeMarker(View view){
        //Record View Click
        actionRecorder.addAction(view, "click",this);

        //Update Firebase
        markerDetailsReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //+1 likes in Database
                int currentLikeCount = Integer.parseInt(snapshot.child("likes").child("amount").getValue().toString());
                currentLikeCount++;
                markerDetailsReference.child("likes").child("amount").setValue(currentLikeCount);

                notifyUserForMarkerViewsMilestone(currentLikeCount, markerDetailsReference, markerTitleTextView.getText().toString());

                //Add the UID in the marker likes
                markerDetailsReference.child("likes").child("accounts").push().setValue(UID);

                //Update UI
                likeButton.setBackgroundDrawable(getResources().getDrawable(R.drawable.round_like_button_selected));
                likeButton.setClickable(false);

                dislikeButton.setVisibility(View.INVISIBLE);
                dislikeButton.setClickable(false);

                //Record liked statistic
                userStatisticsTracker.addStat("rampsliked");

//                int newLikeCount = Integer.parseInt(markerLikesTextView.getText().toString().replace("  ", ""));
//                newLikeCount ++;
//                markerLikesTextView.setText(newLikeCount);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


    }

    public void dislikeMarker(View view){
        //Record View Click
        actionRecorder.addAction(view, "click",this);

        //Update Firebase
        markerDetailsReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //+1 likes in Database
                int currentLikeCount = Integer.parseInt(snapshot.child("dislikes").child("amount").getValue().toString());
                currentLikeCount++;
                markerDetailsReference.child("dislikes").child("amount").setValue(currentLikeCount);

                //Add the UID in the marker likes
                markerDetailsReference.child("dislikes").child("accounts").push().setValue(UID);

                //Update UI
                dislikeButton.setBackgroundDrawable(getResources().getDrawable(R.drawable.round_dislike_button_selected));
                dislikeButton.setClickable(false);

                likeButton.setVisibility(View.INVISIBLE);
                likeButton.setClickable(false);

                //Record liked statistic
                userStatisticsTracker.addStat("rampsdisliked");

//                int newDislikeCount = Integer.parseInt(markerDislikesTextView.getText().toString().replace("  ", ""));
//                newDislikeCount ++;
//                markerLikesTextView.setText(newDislikeCount);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void changeMarkerAvailability(View view){
        //Record View Click
        actionRecorder.addAction(view, "click",this);

        //available -> unavailable
        if(markerAvailability.equals("available")){
            //Update local value
            markerAvailability = "unavailable";

            //Update firebase value
            markerDetailsReference.child("availability").setValue("unavailable");

            //Update the UI elements
            markerAvailabilityTextView.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FFFFDFDF")));
            markerAvailabilityTextView2.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FFFFDFDF")));
            markerAvailabilityTextView.setTextColor(Color.parseColor("#FF866767"));
            markerAvailabilityTextView2.setTextColor(Color.parseColor("#FF866767"));
            markerAvailabilityTextView.setText("unavailable");
            markerAvailabilityTextView2.setText("unavailable");

            markAvailabilityButton.setText("Mark as AVAILABLE");

            //Record liked statistic
            userStatisticsTracker.addStat("rampsdisabled");
        }
        //unavailable -> available
        else{
            //Update local value
            markerAvailability = "available";

            //Update firebase value
            markerDetailsReference.child("availability").setValue("available");

            //Update the UI elements
            markerAvailabilityTextView.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FFE2FFEA")));
            markerAvailabilityTextView2.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FFE2FFEA")));
            markerAvailabilityTextView.setTextColor(Color.parseColor("#FF619C71"));
            markerAvailabilityTextView2.setTextColor(Color.parseColor("#FF619C71"));
            markerAvailabilityTextView.setText("available");
            markerAvailabilityTextView2.setText("available");

            markAvailabilityButton.setText("Mark as NOT AVAILABLE");
            //Record liked statistic
            userStatisticsTracker.addStat("rampsenabled");
        }
    }

    //The notifyUser function is responsible for checking if certain milestones are completed for
    //Marker Views for every 10 , 100 , 1000 , 10000 etc
    //and sends a notification to the notification tab of the user
    void notifyUserForMarkerViewsMilestone(int amount, DatabaseReference currentMarkerDetailsReference, String markerName){
        //If the amount is one of the current milestones for views
        int lengthOfZerosRequiredForMilestone = (int)(Math.log10(amount)+1) - 1; //ex. For 10 1 zero is required, for 100 2 zeros etc...
        String currentAmount = amount + "";
        int count = CharMatcher.is('0').countIn(currentAmount);

        if(count == lengthOfZerosRequiredForMilestone){
            //Send a notification to the users inbox
            //Find the user from the owner tab of the marker
            currentMarkerDetailsReference.child("owner").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for(DataSnapshot owner: snapshot.getChildren()){
                        DatabaseReference userNotificationsReference = FirebaseDatabase.getInstance().getReference().child("users").child(owner.getValue().toString()).child("notifications");
                        //Send the notification
                        userNotificationsReference.push().setValue("Your Marker '" + markerName + "' has " + amount + " Likes!");
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
    }

    //Makes the Numbers have a K for >1000 and a M for >1000000
    String numberRounder(String Snumber){
        int number = Integer.parseInt(Snumber);
        String roundedNumber = "";

        if(number < 1000){
            roundedNumber += number;
        }
        else if (number < 1000000){
            number = Math.round(number / 1000);
            roundedNumber += number + "K";
        }
        else{
            number = Math.round(number / 1000000);
            roundedNumber += number + "M";
        }

        return roundedNumber;
    }

    public void goBack(View view){
        //Record View Click
        actionRecorder.addAction(view, "click",this);

        finish();
    }
}