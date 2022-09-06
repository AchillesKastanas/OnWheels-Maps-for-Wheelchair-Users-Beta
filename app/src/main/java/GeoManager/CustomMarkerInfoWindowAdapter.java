package GeoManager;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;
import com.example.onwheelsapi28android90.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.crashlytics.buildtools.reloc.com.google.common.base.CharMatcher;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.w3c.dom.Text;

import io.opencensus.internal.StringUtils;

public class CustomMarkerInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {

    private final View mWindow;
    private Context mContex;
    private boolean refreshTheInfoWindowOnce = true;

    public CustomMarkerInfoWindowAdapter(Context context){
        mContex = context;
        mWindow = LayoutInflater.from(context).inflate(R.layout.custom_marker_info_window,null);
    }

    private void renderWindow(Marker marker, View view){

        String UID = FirebaseAuth.getInstance().getUid();
        //marker.getTitle() contains the unique marker ID.
        //using the ID we access the database and retrieve the remaining info

        //Locate all the required UI elements
        TextView markerTitle = (TextView) view.findViewById(R.id.markerTitleTextView);
        TextView markerNotes = (TextView) view.findViewById(R.id.markerNotesTextView);
        TextView markerNotesText = (TextView) view.findViewById(R.id.markerNotesText);
        TextView markerAvailability = (TextView) view.findViewById(R.id.markerAvailabilityTextView);
        TextView markerLikes = (TextView) view.findViewById(R.id.markerLikesTextView);
        TextView markerDislikes = (TextView) view.findViewById(R.id.markerDislikesTextView);
        ConstraintLayout backgroundTab = (ConstraintLayout) view.findViewById(R.id.backgroundTab);
        TextView markerTypeText = (TextView) view.findViewById(R.id.markerTypeText);
        TextView markerTypeTextView = (TextView) view.findViewById(R.id.markerTypeTextView);
        TextView tapHere = (TextView) view.findViewById(R.id.tapHereTextView);
        TextView markerViews = (TextView) view.findViewById(R.id.markerViewsTextView);

        //Temp marker is the name of the destination marker during the Trip Planning faze. If a user decided to click the destination marker
        //during that faze, because the destination marker is a client sided marker, a search to the database would result in a crash.
        if(!marker.getTitle().equals("tempMarker")) {

            //Setup Firebase instance
            DatabaseReference currentMarkerReference = FirebaseDatabase.getInstance().getReference().child("geofire").child(marker.getTitle());

            //Access the DatabaseReference and retrieve/ set all the data
            currentMarkerReference.child("markerdetails").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {


                    //Update the Tab
                    markerTitle.setText(snapshot.child("title").getValue().toString());
                    markerNotes.setText(snapshot.child("notes").getValue().toString());
                    markerLikes.setText("  " + numberRounder(snapshot.child("likes").child("amount").getValue().toString()));
                    markerDislikes.setText("  " + numberRounder(snapshot.child("dislikes").child("amount").getValue().toString()));
                    markerViews.setText("  " + numberRounder(snapshot.child("views").child("amount").getValue().toString()));

                    //Setup the availability color UI elements
                    String markerAvailabilityS = snapshot.child("availability").getValue().toString();
                    markerAvailability.setText(markerAvailabilityS);
                    //The markerAvailabilityTextView is already set to green accents by default. In case the marker is unavailable, change the colors to red
                    if (markerAvailabilityS.equals("unavailable")) {
                        markerAvailability.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FFFFDFDF")));
                        markerAvailability.setTextColor(Color.parseColor("#FF866767"));
                        backgroundTab.setBackgroundDrawable(ContextCompat.getDrawable(view.getContext(), R.drawable.round_tab_red_stroke));
                    } else {
                        markerAvailability.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FFE2FFEA")));
                        markerAvailability.setTextColor(Color.parseColor("#FF619C71"));
                        backgroundTab.setBackgroundDrawable(ContextCompat.getDrawable(view.getContext(), R.drawable.round_tab_white_stroke));
                    }



                    //Check if the logged in user is in the views list
                    boolean userHasViewedTheMarker = false;
                    for(DataSnapshot viewedUser: snapshot.child("views").child("accounts").getChildren()){
                        Log.d("EDWW", viewedUser + "");
                        if(viewedUser.getValue().toString().equals(UID)){
                            userHasViewedTheMarker = true;
                        }
                    }
                    //Add +1 view to the view counter of the marker only if the user has not viewed the marker before
                    if(!userHasViewedTheMarker){
                        int newNumberOfViews = Integer.parseInt(snapshot.child("views").child("amount").getValue().toString()) + 1;
                        currentMarkerReference.child("markerdetails").child("views").child("amount").setValue(newNumberOfViews);
                        currentMarkerReference.child("markerdetails").child("views").child("accounts").push().setValue(UID);

                        //Re-update the markerViews Text View
                        markerViews.setText("  " + numberRounder(newNumberOfViews + ""));

                        //Send a notification to the user if a view milestone is reached
                        notifyUserForMarkerViewsMilestone(newNumberOfViews, currentMarkerReference, snapshot.child("title").getValue().toString());
                    }

                    //Refresh the InfoWindow when the data have been loaded
                    if (refreshTheInfoWindowOnce) {
                        marker.hideInfoWindow();
                        marker.showInfoWindow();
                        refreshTheInfoWindowOnce = false;
                    }

                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

        }
        else{
            //Catches the case of a click event on the destination marker during the Trip Planning faze
            //Marker had an empty title. Show empty marker instead
            markerTitle.setText("Your Destination");
            markerAvailability.setVisibility(View.GONE);
            markerNotes.setVisibility(View.GONE);
            markerNotesText.setVisibility(View.GONE);
            markerTypeText.setVisibility(View.GONE);
            markerTypeTextView.setVisibility(View.GONE);
            markerLikes.setVisibility(View.GONE);
            markerDislikes.setVisibility(View.GONE);
            tapHere.setVisibility(View.GONE);
        }
    }

    @Override
    public View getInfoContents(@NonNull Marker marker) {
        refreshTheInfoWindowOnce = true;
        renderWindow(marker, mWindow);
        return mWindow;
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

    //The notifyUser function is responsible for checking if certain milestones are completed for
    //Marker Views for every 10 , 100 , 1000 , 10000 etc
    //and sends a notification to the notification tab of the user
    void notifyUserForMarkerViewsMilestone(int amount, DatabaseReference currentMarkerReference, String markerName){
        //If the amount is one of the current milestones for views
        int lengthOfZerosRequiredForMilestone = (int)(Math.log10(amount)+1) - 1; //ex. For 10 1 zero is required, for 100 2 zeros etc...
        String currentAmount = amount + "";
        int count = CharMatcher.is('0').countIn(currentAmount);

        if(count == lengthOfZerosRequiredForMilestone){
            //Send a notification to the users inbox
            //Find the user from the owner tab of the marker
            currentMarkerReference.child("markerdetails").child("owner").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for(DataSnapshot owner: snapshot.getChildren()){
                        DatabaseReference userNotificationsReference = FirebaseDatabase.getInstance().getReference().child("users").child(owner.getValue().toString()).child("notifications");
                        //Send the notification
                        userNotificationsReference.push().setValue("Your Marker '" + markerName + "' has reached " + amount + " Views!");
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
    }

    @Nullable
    @Override
    public View getInfoWindow(@NonNull Marker marker) {
        refreshTheInfoWindowOnce = true;
        renderWindow(marker, mWindow);
        return mWindow;
    }
}
