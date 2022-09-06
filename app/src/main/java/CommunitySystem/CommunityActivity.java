package CommunitySystem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewManager;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.onwheelsapi28android90.R;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import ProfileSystem.ProfileActivity;
import ProfileSystem.UserStatisticsTracker;

public class CommunityActivity extends AppCompatActivity {

    Context context;
    BroadcastReceiver broadcastReceiver;

    EditText usernameInput;
    TextView popularInAreaText, joinedText;
    Button plusButton, searchButton;
    HorizontalScrollView popularInAreaScrollView;
    ScrollView joinedScrollView;

    ConstraintLayout joinedConstraintLayout, popularInAreaConstraintLayout;

    LinearLayout joinedChatContainerLinearLayout;

    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;
    String UID;

    UserStatisticsTracker userStatisticsTracker = new UserStatisticsTracker();

    //UI MANAGEMENT
    //All the loaded room button ids are stored here
    ArrayList<Integer> loadedJoinedRooms = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_community);
        context = this;

        //Setup Status Bar
        try{
            getSupportActionBar().hide();
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);

            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(getResources().getColor(R.color.minimalLightPurple));
            window.setNavigationBarColor(getResources().getColor(R.color.minimalLightPurple));
        }
        catch (Exception z){
            //Title bar already disabled?
        }

        //Setup Firebase
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference();
        UID = FirebaseAuth.getInstance().getUid();

        //Locate everything
        usernameInput = (EditText) findViewById(R.id.chatRoomTitleEditText);
        popularInAreaScrollView = (HorizontalScrollView) findViewById(R.id.popularInAreaScrollView);
        joinedScrollView = (ScrollView) findViewById(R.id.joinedScrollView);
        popularInAreaText = (TextView) findViewById(R.id.popularInAreaText);
        joinedText = (TextView) findViewById(R.id.joinedText);
        searchButton = (Button) findViewById(R.id.searchButton);
        plusButton = (Button) findViewById(R.id.plusButton);

        //Show all the Nearby/Joined Rooms
        //loadNearbyRooms();
        loadJoinedRooms();

        //Play the intro animation
        introAnimation();

        //Receive "refresh_joined_rooms_UI" broadcast when a user leaves a room so the chats get refreshed.
        broadcastReceiver = new BroadcastReceiver() {

            @Override
            public void onReceive(Context arg0, Intent intent) {
                String action = intent.getAction();
                if (action.equals("refresh_joined_rooms_UI")) {
                    refreshJoinedRoomsUI();
                }
            }
        };

        registerReceiver(broadcastReceiver, new IntentFilter("refresh_joined_rooms_UI"));
    }

    void refreshJoinedRoomsUI() {
        //Clear from the UI
        for(int roomID: loadedJoinedRooms){

            MaterialButton roomButton = findViewById(roomID);
            roomButton.setVisibility(View.GONE);
        }
        //Clear the stored items from the ArrayList
        loadedJoinedRooms.clear();
        //Reload Everything
        loadJoinedRooms();
    }

    void loadJoinedRooms(){
        joinedConstraintLayout = (ConstraintLayout) findViewById(R.id.joinedConstraintLayout);
        joinedChatContainerLinearLayout = (LinearLayout) findViewById(R.id.joinedChatContainerLinearLayout);

        //Get all joined chats
        databaseReference.child("users").child(UID).child("chatrooms").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot roomIDs: snapshot.getChildren()){
                    if (!roomIDs.getValue().toString().isEmpty()){
                        addRoomButton(joinedChatContainerLinearLayout, roomIDs.getValue().toString());
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    //addRoomButton is responsible for setting up the UI of the Room Buttons. It is called everytime a new room is added
    void addRoomButton(LinearLayout container, String roomID){

        //Get Room Details from Firebase
        DatabaseReference roomReference = databaseReference.child("chatrooms").child(roomID);

        //Create Button
        MaterialButton roomButton = new MaterialButton(context);
        //Set ID
        int roomButtonID = roomButton.generateViewId();
        roomButton.setId(roomButtonID);
        //Set Text Color
        roomButton.setTextColor(Color.parseColor("#FFFFFF"));
        //Set Corner Radius
        roomButton.setCornerRadius(dpToPixelConverter(context, 10));
        //Set Background Tint Color
        roomButton.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#8B89C1")));
        //Set Text Alignment
        roomButton.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_START);
        //Set Title
        roomReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                roomButton.setText(snapshot.child("title").getValue().toString());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        //Set Button Layout
        LinearLayout.LayoutParams roomButtonParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dpToPixelConverter(context, 100));
        roomButtonParams.setMargins(dpToPixelConverter(context, 30), 0, dpToPixelConverter(context, 30), 0);
        //Set Button Params
        roomButton.setLayoutParams(roomButtonParams);
        //Add Button Listener
        roomButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Go to CommunityChatRoomActivity
                Intent intent = new Intent(context, CommunityChatRoomActivity.class);
                intent.putExtra("roomID", roomID);
                startActivity(intent);
            }
        });

        //Place roomButton inside the ConstrainLayout
        container.addView(roomButton);
        //Store it in the ArrayList
        loadedJoinedRooms.add(roomButtonID);

    }

    void introAnimation(){
        Animation slide_left = AnimationUtils.loadAnimation(context,R.anim.slide_left);
        Animation slide_up = AnimationUtils.loadAnimation(context,R.anim.slide_up);
        Animation fade_in = AnimationUtils.loadAnimation(context, R.anim.fade_in);
        Animation slide_down = AnimationUtils.loadAnimation(context, R.anim.slide_down);
        Animation slide_right = AnimationUtils.loadAnimation(context, R.anim.slide_right);

        usernameInput.startAnimation(fade_in);
        searchButton.startAnimation(fade_in);

        popularInAreaText.startAnimation(fade_in);
        joinedText.startAnimation(fade_in);

        popularInAreaScrollView.startAnimation(slide_left);
        joinedScrollView.startAnimation(slide_up);

        plusButton.startAnimation(slide_left);

    }

    public void goToCommunityRoomCreation(View view){
        Intent intent =new Intent(this, CommunityRoomCreationActivity.class);
        startActivity(intent);
    }

    public void goBack(View view){
        finish();
    }

    //The parameters are only accepted in pixel format, so a conversion has to be made to match every phone
    int dpToPixelConverter(Context context, int dps){
        final float scale = context.getResources().getDisplayMetrics().density;
        int pixels = (int) (dps * scale + 0.5f);

        return pixels;
    }

}