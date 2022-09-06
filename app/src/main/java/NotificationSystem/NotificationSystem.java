package NotificationSystem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import com.example.onwheelsapi28android90.R;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.view.animation.RotateAnimation;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

public class NotificationSystem extends AppCompatActivity {

    FirebaseDatabase firebaseDatabase;
    DatabaseReference userReference, notificationReference;
    String UID;
    LinearLayout outerOldNotificationContainerLinearLayout, outerNewNotificationContainerLinearLayout;
    Context context;
    ValueEventListener valueEventListener;
    AppCompatButton backButton;
    TextView notificationTitleTextView, newNotificationTitleTextView;

    ArrayList loadedNotificationsArrayList = new ArrayList();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(R.anim.activity_slide_left_to_center, R.anim.activity_fade_out);
        setContentView(R.layout.activity_notification_system);

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

        //Get the context
        context = this;

        //Get the required UI Elements
        outerOldNotificationContainerLinearLayout = (LinearLayout) findViewById(R.id.outerOldNotificationContainerLinearLayout);
        outerNewNotificationContainerLinearLayout = (LinearLayout) findViewById(R.id.outerNewNotificationContainerLinearLayout);
        notificationTitleTextView = (TextView) findViewById(R.id.notificationTitleTextView);
        newNotificationTitleTextView = (TextView) findViewById(R.id.newNotificationTitleTextView);
        notificationTitleTextView.setAnimation(AnimationUtils.loadAnimation(this, R.anim.slide_left));
        backButton = (AppCompatButton) findViewById(R.id.backButton);
        backButton.setAnimation((RotateAnimation) AnimationUtils.loadAnimation(this,R.anim.rotate_90_left));

        //Get Logged in user UID
        UID = FirebaseAuth.getInstance().getUid();

        //Setup firebase realtime database references
        firebaseDatabase = FirebaseDatabase.getInstance();
        userReference = firebaseDatabase.getReference().child("users").child(UID);
        notificationReference = userReference.child("notifications");

        //Setup listener for loading old + new notifications from the notificationReference
        valueEventListener = notificationReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot notification: snapshot.getChildren()){
                    //If the notification is not loaded
                    if(!loadedNotificationsArrayList.contains(notification.getKey())){
                        //Get the notification text
                        String notificationText = notification.getValue().toString();
                        //Add the notification to the loaded notifications ArrayList
                        loadedNotificationsArrayList.add(notification.getKey());

                        //Load new notification
                        createNotificationBubble(notificationText, notification.getKey());
                    }

                }
                if(outerNewNotificationContainerLinearLayout.getChildCount() == 0){
                    newNotificationTitleTextView.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    void createNotificationBubble(String text, String notificationKey){

        //Create Button
        MaterialButton notificationButton = new MaterialButton(context);

        //Set ID
        int notificationButtonID = notificationButton.generateViewId();
        notificationButton.setId(notificationButtonID);
        //Set Text Color
        notificationButton.setTextColor(Color.parseColor("#676986"));
        //Set Corner Radius
        notificationButton.setCornerRadius(dpToPixelConverter(context, 15));

        //Add A thin boarder around the Button
        notificationButton.setStrokeWidth(dpToPixelConverter(context, 1));
        //Set Color of the stroke
        notificationButton.setStrokeColor(ColorStateList.valueOf(Color.parseColor("#676986")));
        //Set Text Alignment
        notificationButton.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_START);
        //Set Text Caps Mode
        notificationButton.setAllCaps(false);
        //Text Padding
        notificationButton.setIconPadding(dpToPixelConverter(context, 15));
        //Set Button Layout
        LinearLayout.LayoutParams notificationButtonParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dpToPixelConverter(context, 80));
        notificationButtonParams.setMargins(dpToPixelConverter(context, 20), dpToPixelConverter(context, 0), dpToPixelConverter(context, 20), 0);
        //Set Button Params
        notificationButton.setLayoutParams(notificationButtonParams);
        //Set the Button Drawable
        if(text.contains("Welcome")){
            notificationButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.welcome_notification, 0, 0, 0);
        }
        else if(text.contains("Like")){
            notificationButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.like_35px, 0, 0, 0);
        }
        else if(text.contains("Dislike")){
            notificationButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.dislike_35px, 0, 0, 0);
        }
        else if(text.contains("Used")){
            notificationButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.purplemarker, 0, 0, 0);
        }
        else if(text.contains("Views")){
            notificationButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.view_35px, 0, 0, 0);
        }
        //Notifications are stored in the database with a "R." in the beginning of the title if the notification has been read before
        //If the notification gets loaded for the first time, set an "R." in the beginning of the title
        //Else, remove the "R." from the title string, and display it as "Read Notification"
        if(text.contains("R.")){
            //Remove it from the UI
            text = text.replace("R.", "");
            //Set Background Tint Color
            notificationButton.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#EEEEEE")));
            //Set Title
            notificationButton.setText(text);
            //Place roomButton inside the oldNotificationContainer
            outerOldNotificationContainerLinearLayout.addView(notificationButton, 0);
        }
        else{
            //Set Background Tint Color
            notificationButton.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FFFFFF")));
            //Add a "R." to the database / mark it as Read
            notificationReference.child(notificationKey).setValue("R." + text);
            //Text Bold
            notificationButton.setTypeface(null, Typeface.BOLD);
            //Set Title
            notificationButton.setText(text);
            //Place roomButton inside the newNotificationContainer
            outerNewNotificationContainerLinearLayout.addView(notificationButton, 0);
        }


    }

    //The parameters are only accepted in pixel format, so a conversion has to be made to match every phone
    int dpToPixelConverter(Context context, int dps){
        final float scale = context.getResources().getDisplayMetrics().density;
        int pixels = (int) (dps * scale + 0.5f);

        return pixels;
    }

    @Override
    public void finish() {
        super.finish();
        //Exit animation
        overridePendingTransition(R.anim.activity_fade_in, R.anim.activity_slide_center_to_left);
    }

    public void goBack(View view){
        notificationReference.removeEventListener(valueEventListener);
        backButton.startAnimation((RotateAnimation) AnimationUtils.loadAnimation(this,R.anim.rotate_90_left));
        finishAfterTransition();

        //Send a broadcast to remove the notification dot from the main activity
        Intent intent = new Intent("reset_notification_dot");
        sendBroadcast(intent);
    }
}