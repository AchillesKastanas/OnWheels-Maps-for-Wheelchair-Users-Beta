package ProfileSystem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.onwheelsapi28android90.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import Helpers.ActionRecorder;

public class CommunityPointsActivity extends AppCompatActivity {

    ImageView badgeHolderImageView;
    ActionRecorder actionRecorder = new ActionRecorder();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_community_points);

        //Load all the required xp/leveling info
        loadUserProfile();

        //Setup Status Bar
        try{
            getSupportActionBar().hide();
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);

            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(getResources().getColor(R.color.minimalGray));
            window.setNavigationBarColor(getResources().getColor(R.color.minimalGray));
        }
        catch (Exception z){
            //Title bar already disabled?
        }

        //Manage the UI elements
        badgeHolderImageView = (ImageView) findViewById(R.id.badgeHolderImageView);
    }

    void loadUserProfile(){
        //Get Logged in user UID
        String UID = FirebaseAuth.getInstance().getUid();

        //Get firebase realtime database reference
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        DatabaseReference userReference = firebaseDatabase.getReference().child("users").child(UID);

        //Get all the required TextViews to display the level + remaining xp
        TextView levelTextView = findViewById(R.id.levelTextView);
        TextView xpRemainingTextView = findViewById(R.id.xpremainingTextView);
        TextView currentLevelTextView = findViewById(R.id.currentlevelTextView);
        TextView nextLevelTextView = findViewById(R.id.nextlevelTextView);

        //Update the TextViews
        userReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int[] userXP = xpLevelingManager(Integer.parseInt(snapshot.child("statistics").child("xp").getValue().toString()));

                levelTextView.setText("LEVEL: " + userXP[0]);
                xpRemainingTextView.setText(userXP[1] + "XP remaining for Level Up");
                currentLevelTextView.setText("Level " + userXP[0]);
                nextLevelTextView.setText("Level " + (userXP[0] + 1));

                //Update the xpBar
                ProgressBar xpBar = findViewById(R.id.xpBar);
                xpBar.setProgress(progressBarCalculator(Integer.parseInt(snapshot.child("statistics").child("xp").getValue().toString()), userXP[0]));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    int[] xpLevelingManager(int currentXP){
        // Returns -> [USER LEVEL, XP REMAINING FOR LEVEL UP]
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

        if(currentXP < 100) {
            badgeHolderImageView.setImageResource(R.drawable.rising_star);
            return new int[]{0, 100 - currentXP};
        }
        else if(currentXP < 225) {
            badgeHolderImageView.setImageResource(R.drawable.star_badge);
            return new int[]{1, 225 - currentXP};
        }
        else if(currentXP < 475) {
            badgeHolderImageView.setImageResource(R.drawable.star_medal);
            return new int[]{2, 475 - currentXP};
        }
        else if(currentXP < 975) {
            badgeHolderImageView.setImageResource(R.drawable.star_award);
            return new int[]{3, 975 - currentXP};
        }
        else if(currentXP < 1975) {
            badgeHolderImageView.setImageResource(R.drawable.star_collector);
            return new int[]{4, 1975 - currentXP};
        }
        else if(currentXP < 3975) {
            badgeHolderImageView.setImageResource(R.drawable.star_pro);
            return new int[]{5, 3975 - currentXP};
        }
        else if(currentXP < 7975) {
            badgeHolderImageView.setImageResource(R.drawable.star_master);
            return new int[]{6, 7975 - currentXP};
        }
        else if(currentXP < 15975) {
            badgeHolderImageView.setImageResource(R.drawable.star_king);
            return new int[]{7, 15975 - currentXP};
        }
        else {
            return new int[]{8, 0};
        }
    }

    int progressBarCalculator(int currentUserXP, int currentUserLevel){

        //formula ((currentUserXP - minXPForCurrentLevel) * 100) / XPForNextLevel - XPForCurrentLevel

        if(currentUserLevel < 1) return ((currentUserXP - 0) * 100) / (100 - 0);
        else if(currentUserLevel < 2) return ((currentUserXP - 100) * 100) / (225 - 100);
        else if(currentUserLevel < 3) return ((currentUserXP - 225) * 100) / (475 - 225);
        else if(currentUserLevel < 4) return ((currentUserXP - 475) * 100) / (975 - 475);
        else if(currentUserLevel < 5) return ((currentUserXP - 975) * 100) / (1975 - 975);
        else if(currentUserLevel < 6) return ((currentUserXP - 1975) * 100) / (3975 - 1975);
        else if(currentUserLevel < 7) return ((currentUserXP - 3975) * 100) / (7975 - 3975);
        else if(currentUserLevel < 8) return ((currentUserXP - 3975) * 100) / (15975 - 7975);
        else return 100;
    }

    public void goToCommunityPointsInfoTab(View view){
        //Record View Click
        actionRecorder.addAction(view, "click", this);

        Intent intent =new Intent(this, CommunityPointsInfoActivity.class);
        startActivity(intent);
    }

    public void goToProfileTab(View view){
        //Record View Click
        actionRecorder.addAction(view, "click", this);

        finish();
    }

}