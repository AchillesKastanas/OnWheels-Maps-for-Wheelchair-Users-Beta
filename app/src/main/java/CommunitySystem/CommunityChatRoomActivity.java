package CommunitySystem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.onwheelsapi28android90.R;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;

import ProfileSystem.UserStatisticsTracker;
import de.hdodenhof.circleimageview.CircleImageView;

public class CommunityChatRoomActivity extends AppCompatActivity {

    String roomID, UID;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference, roomReference;
    BroadcastReceiver broadcastReceiver;

    EditText chatRoomMessageBoxEditText;
    TextView chatJoinTextView, roomTitleTextView;
    MaterialButton joinChatButton;
    AppCompatButton sendMessageButton;
    ConstraintLayout messageContainerConstraintLayout;

    String usernameOfCurrentUser;

    UserStatisticsTracker userStatisticsTracker = new UserStatisticsTracker();

    Context context;

    //UI variables
    int lastMessageID = -999;
    int loadedMessageCount = 0;

    //RecyclerViewAdapter recyclerViewAdapter;
    //LinearLayoutManager linearLayoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_community_chat_room);

        context = this;

        //Receive the roomID from the intent from the CommunityActivity
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            roomID = extras.getString("roomID");
        }

        //Setup Firebase
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference();
        roomReference = FirebaseDatabase.getInstance().getReference().child("chatrooms").child(roomID);
        UID = FirebaseAuth.getInstance().getUid();

        //Get the username of the signed in user
        databaseReference.child("users").child(UID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                usernameOfCurrentUser = snapshot.child("username").getValue().toString();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        //Check if the user is joined in the specific chat room.
        //-  If the user is not joined, the room appears in "view" mode. A button is requesting the user to
        //      join in order to interact with the chat.
        //-  If the user is joined, an EditText and a sendMessageButton appear on the bottom of the screen
        databaseReference.child("users").child(UID).child("chatrooms").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean memberOfChatRoom = false;
                for(DataSnapshot roomIDs: snapshot.getChildren()){
                    //If user has joined the room
                    if(roomIDs.getValue().equals(roomID)){
                        memberOfChatRoom = true;
                    }
                }
                if(memberOfChatRoom){
                    loadMemberChatRoom();
                }
                else{
                    loadViewerChatRoom();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        //Load the Room Title
        databaseReference.child("chatrooms").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot roomIDs: snapshot.getChildren()){
                    if(roomIDs.getKey().equals(roomID)){
                        roomTitleTextView.setText(roomIDs.child("title").getValue().toString());
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        //Locate all the required UI elements
        chatRoomMessageBoxEditText = (EditText) findViewById(R.id.chatRoomMessageBoxEditText);
        chatJoinTextView = (TextView) findViewById(R.id.chatJoinTextView);
        roomTitleTextView = (TextView) findViewById(R.id.notificationTitleTextView);
        joinChatButton = (MaterialButton) findViewById(R.id.joinChatButton);
        sendMessageButton = (AppCompatButton) findViewById(R.id.sendMessageButton);
        messageContainerConstraintLayout = (ConstraintLayout) findViewById(R.id.messageContainerConstraintLayout);

        //Load all the messages and setup listeners for new ones
        loadMessages();

        //Receive "finish_chat_room_activity" broadcast when the user selects "Leave Room" so no activities are left running.
        broadcastReceiver = new BroadcastReceiver() {

            @Override
            public void onReceive(Context arg0, Intent intent) {
                String action = intent.getAction();
                if (action.equals("finish_chat_room_activity")) {
                    unregisterReceiver(broadcastReceiver);

                    //Send a broadcast to refresh CommunityActivity as well
                    Intent intent2 = new Intent("refresh_joined_rooms_UI");
                    sendBroadcast(intent2);

                    finish();
                }
            }
        };

        registerReceiver(broadcastReceiver, new IntentFilter("finish_chat_room_activity"));
    }

    void loadMessages(){
        //Get all messages and load listen for the new ones.
        databaseReference.child("chatrooms").child(roomID).child("messages").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                //Store loopedmessages count
                int loopedMessageCount = 0;

                for(DataSnapshot message: snapshot.getChildren()){

                    loopedMessageCount ++;
                    if(loadedMessageCount < loopedMessageCount ) {

                        String authorID = message.child("author").getValue().toString();
                        String messageText = message.child("data").getValue().toString();

                        //Locate the username corresponding to the authorID
                        databaseReference.child("users").child(authorID).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                String username = snapshot.child("username").getValue().toString();

                                //Load the message in the app UI
                                createMessageUI(messageText, username, authorID);
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    void createMessageUI(String messageText, String username, String authorID){
        //Note that +1 message has been loaded
        loadedMessageCount++;

        //Create Button
        MaterialButton chatBubbleButton = new MaterialButton(context);
        //Set ID
        chatBubbleButton.setId(chatBubbleButton.generateViewId());
        //Disable all caps
        chatBubbleButton.setAllCaps(false);
        //Set Text Color
        chatBubbleButton.setTextColor(Color.parseColor("#FFFFFF"));
        //Set Corner Radius
        chatBubbleButton.setCornerRadius(dpToPixelConverter(context, 10));
        //Set Background Tint Color
        chatBubbleButton.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#8B89C1")));
        //Set Button Text
        chatBubbleButton.setText("@"+ username + "\n" + messageText);
        //Set Text Alignment
        chatBubbleButton.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_START);
        //Set Button Layout
        LinearLayout.LayoutParams roomButtonParams = new LinearLayout.LayoutParams(dpToPixelConverter(context, 300), ViewGroup.LayoutParams.WRAP_CONTENT);
        //roomButtonParams.setMargins(dpToPixelConverter(context, 10), 0, dpToPixelConverter(context, 10), 0);
        //Set Button Params
        chatBubbleButton.setLayoutParams(roomButtonParams);
        //Add Button Listener
        chatBubbleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO
                //GO TO USER PROFILE?? MESSAGE SETTINGS??
            }
        });


        //Create Profile Icon
        CircleImageView profileIcon = new CircleImageView(context);
        //Set ID
        profileIcon.setId(profileIcon.generateViewId());
        //Set Layout Parameters
        LinearLayout.LayoutParams profileIconParams = new LinearLayout.LayoutParams(dpToPixelConverter(context, 65), dpToPixelConverter(context, 65));
        profileIcon.setLayoutParams(profileIconParams);
        //Load user Image
        //If the user has no profile image, load a temporary one
        if(databaseReference.child("users").child(authorID).child("profileimage").getKey().isEmpty()){
            profileIcon.setImageResource(R.drawable.profile);
        }
        //If the user has a profile image, load it
        else{
            databaseReference.child("users").child(authorID).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    //Get the image URL
                    String imageURL = snapshot.child("profileimage").getValue().toString();
                    //Use Picasso to load the image in the imageView
                    Picasso.with(context).load(imageURL).into(profileIcon);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }


        //Insert the bubble + profile icon in the ConstraintLayout
        messageContainerConstraintLayout.addView(profileIcon);
        messageContainerConstraintLayout.addView(chatBubbleButton);

        //Add constraints to the chatBubbleButton + profileIcon
        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.clone(messageContainerConstraintLayout);

        //Message UI Formula:
        //
        // |
        // | <-- 10 -- ProfileIcon <-- 10 --> messageButton -- 10 -->
        // |
        //

        // VERTICAL CONSTRAINTS
        //Check if there are previous messages
        //If there are, save the bubble id and constrain the new message under the last one
        //If not (-999 flag), save the bubble id and constrain the new message on top of the screen
        if(lastMessageID == -999){
            constraintSet.connect(profileIcon.getId(), ConstraintSet.TOP, messageContainerConstraintLayout.getId(), ConstraintSet.TOP, 5);
            constraintSet.connect(chatBubbleButton.getId(), ConstraintSet.TOP, messageContainerConstraintLayout.getId(), ConstraintSet.TOP, 5);
            lastMessageID = chatBubbleButton.getId();
        }
        else{
            constraintSet.connect(profileIcon.getId(), ConstraintSet.TOP, lastMessageID, ConstraintSet.BOTTOM, 5);
            constraintSet.connect(chatBubbleButton.getId(), ConstraintSet.TOP, lastMessageID, ConstraintSet.BOTTOM, 5);
            lastMessageID = chatBubbleButton.getId();
        }
        // HORIZONTAL CONSTRAINTS
        //Check if messages have been sent from the currently logged in user
        //current user messages
        if(username.equals(usernameOfCurrentUser)){
            //Set Personal Text Color
            chatBubbleButton.setTextColor(Color.parseColor("#676986"));
            //Set Personal Background Tint Color
            chatBubbleButton.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#EEEEEE")));

            constraintSet.connect(profileIcon.getId(), ConstraintSet.RIGHT, messageContainerConstraintLayout.getId(), ConstraintSet.RIGHT, 10);
            constraintSet.connect(chatBubbleButton.getId(), ConstraintSet.RIGHT, profileIcon.getId(), ConstraintSet.LEFT, 10);
            //constraintSet.connect(chatBubbleButton.getId(), ConstraintSet.RIGHT, messageContainerConstraintLayout.getId(), ConstraintSet.RIGHT);
        }
        //Other users messages
        else{
            constraintSet.connect(profileIcon.getId(), ConstraintSet.LEFT, messageContainerConstraintLayout.getId(), ConstraintSet.LEFT, 10);
            constraintSet.connect(chatBubbleButton.getId(), ConstraintSet.LEFT, profileIcon.getId(), ConstraintSet.RIGHT, 10);
            //constraintSet.connect(chatBubbleButton.getId(), ConstraintSet.RIGHT, messageContainerConstraintLayout.getId(), ConstraintSet.RIGHT);
        }
        constraintSet.applyTo(messageContainerConstraintLayout);

    }

    void loadMemberChatRoom(){
        chatJoinTextView.setVisibility(View.GONE);
        joinChatButton.setVisibility(View.GONE);
        chatRoomMessageBoxEditText.setVisibility(View.VISIBLE);
        sendMessageButton.setVisibility(View.VISIBLE);
    }

    void loadViewerChatRoom(){
        chatJoinTextView.setVisibility(View.VISIBLE);
        joinChatButton.setVisibility(View.VISIBLE);
        chatRoomMessageBoxEditText.setVisibility(View.GONE);
        sendMessageButton.setVisibility(View.GONE);
    }

    //The parameters are only accepted in pixel format, so a conversion has to be made to match every phone
    int dpToPixelConverter(Context context, int dps){
        final float scale = context.getResources().getDisplayMetrics().density;
        int pixels = (int) (dps * scale + 0.5f);

        return pixels;
    }

    public void goToChatRoomOptions(View view){
        Intent intent =new Intent(this, CommunityChatRoomOptionsActivity.class);
        intent.putExtra("roomID", roomID);
        startActivity(intent);
    }

    public void goBack(View view){
        finish();
    }

    public void sendMessage(View view){
        String message = chatRoomMessageBoxEditText.getText().toString();

        //Add message to room
        String timeOfCreation = String.valueOf(System.currentTimeMillis()/1000);
        DatabaseReference newMessageReference = databaseReference.child("chatrooms").child(roomID).child("messages").child(timeOfCreation);

        Map<String, Object> map = new HashMap<>();
        //newMessageReference.child("data").setValue(message);
        map.put("data", message);
        //newMessageReference.child("author").setValue(UID);
        map.put("author", UID);
        newMessageReference.updateChildren(map);

        chatRoomMessageBoxEditText.setText("");

        //Record message sent stat
        userStatisticsTracker.addStat("messagessent");
    }

    //    //RecyclerView initialization
//    void initRecyclerView(){
//        //Load all the required data
//        recyclerViewAdapter = new RecyclerViewAdapter();
//        linearLayoutManager = new LinearLayoutManager(context);
//
//        linearLayoutManager.setOrientation(RecyclerView.VERTICAL);
//        chatContainerRecyclerView.setLayoutManager(linearLayoutManager);
//        chatContainerRecyclerView.setAdapter(recyclerViewAdapter);
//    }
//
//    //Refreshes the adapter. Updates the recyclerView with the updated data from the initialization.
//    void updateRecyclerView(){
//        recyclerViewAdapter.notifyDataSetChanged();
//    }
}