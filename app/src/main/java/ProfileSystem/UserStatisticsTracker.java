package ProfileSystem;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

//Class responsible for tracking all the user actions/ statistics and storing them in firebase
public class UserStatisticsTracker {

    //Firebase variables
    FirebaseAuth firebaseAuth;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference, userReference, userStatisticsReference;

    int distanceTraveled = 0;

    public UserStatisticsTracker(){
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference();
        userReference = databaseReference.child("users").child(firebaseAuth.getUid());
        userStatisticsReference = userReference.child("statistics");
    }

    //
    //
    // --- TRAVEL STATISTICS ---
    //
    //

    public void addToDistanceTraveled(int meters){
        distanceTraveled += meters;
    }

    public void saveDistanceTraveled(){
        addStat("distancetraveled", distanceTraveled);
        distanceTraveled = 0;
    }

    // statName vars:
    //
    // chatroomscreated, chatroomsjoined, distancetraveled
    // messagessent, rampsdisabled, rampsdisliked
    // rampsenabled, rampsliked, rampsplaced, rampsused
    // rampsviewed, totaltrips, xp

    public void addStat(String statName){
        userStatisticsReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String data = snapshot.child(statName).getValue().toString();
                userStatisticsReference.child(statName).setValue(Integer.parseInt(data) + 1);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void addStat(String statName, int amount){
        userStatisticsReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String data = snapshot.child(statName).getValue().toString();
                userStatisticsReference.child(statName).setValue(Integer.parseInt(data) + amount);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

}
