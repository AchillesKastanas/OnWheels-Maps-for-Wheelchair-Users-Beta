package CommunitySystem;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;

import com.example.onwheelsapi28android90.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;

import java.text.SimpleDateFormat;
import java.util.Date;

import ProfileSystem.UserStatisticsTracker;

public class CommunityRoomCreationActivity extends AppCompatActivity {

    UserStatisticsTracker userStatisticsTracker = new UserStatisticsTracker();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_community_room_creation);

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
    }

    public void closeTab(View view){
        finish();
    }

    public void createRoom(View view){
        EditText chatRoomTitleEditText = (EditText) findViewById(R.id.chatRoomTitleEditText);
        String chatRoomTitle = chatRoomTitleEditText.getText().toString();

        //Setup Firebase
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        DatabaseReference chatRoomReference = databaseReference.child("chatrooms");
        String UID = FirebaseAuth.getInstance().getUid();

        //Create a unique id for the room
        DatabaseReference pushedUniqueID = chatRoomReference.push();

        String timeOfCreation = String.valueOf(System.currentTimeMillis()/1000);

        //Store the chatRoomTitle
        pushedUniqueID.child("title").setValue(chatRoomTitle);
        //Store the current user Auth ID in the members list chronologically
        pushedUniqueID.child("members").child(timeOfCreation).setValue(UID);
        //Store the chat ID in the user account
        databaseReference.child("users").child(UID).child("chatrooms").child(timeOfCreation).setValue(pushedUniqueID.getKey());
        //Create the first message.
        DatabaseReference newMessageReference = pushedUniqueID.child("messages").child(timeOfCreation);
        newMessageReference.child("data").setValue("Hello! I just created this Chat Room:)");
        newMessageReference.child("author").setValue(UID);

        //Record room created + room joined stat
        userStatisticsTracker.addStat("chatroomscreated");
        userStatisticsTracker.addStat("chatroomsjoined");

        //Send a broadcast to refresh CommunityActivity joined rooms ui
        Intent intent = new Intent("refresh_joined_rooms_UI");
        sendBroadcast(intent);

        finish();
    }
}