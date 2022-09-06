package CommunitySystem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.BroadcastReceiver;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.example.onwheelsapi28android90.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class CommunityChatRoomOptionsActivity extends AppCompatActivity {

    String roomID, UID;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_community_chat_room_options);

        //Setup Firebase
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference();
        UID = FirebaseAuth.getInstance().getUid();

        //Get the roomID
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            roomID = extras.getString("roomID");
        }
    }

    public void leaveChatRoom(View view){
        //Remove user from the database of chatrooms
        databaseReference.child("chatrooms").child(roomID).child("members").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot member: snapshot.getChildren()){
                    if(member.getValue().toString().equals(UID)){
                        member.getRef().removeValue();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        //Remove user from the database of users
        databaseReference.child("users").child(UID).child("chatrooms").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot chatRoomID: snapshot.getChildren()){
                    if(chatRoomID.getValue().toString().equals(roomID)){
                        chatRoomID.getRef().removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            //Execute and wait for complete
                            public void onComplete(@NonNull Task<Void> task) {

                                //Send a broadcast to end the CommunityChatRoomActivity as well
                                Intent intent = new Intent("finish_chat_room_activity");
                                sendBroadcast(intent);

                                //Finish current activity
                                finish();

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
}