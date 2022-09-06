package ProfileSystem;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.onwheelsapi28android90.R;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import AppStartupManager.SplashScreen;
import Helpers.ActionRecorder;
import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {

    Button communityPointsButton, travelStatisticsButton, profileSettingsButton, signOutButton;
    TextView nameTextView, usernameTextView;
    ImageView backgroundImage, profileImage, profileBadgeHolderImageView;
    Context context;

    StorageReference storageReference;
    ActionRecorder actionRecorder = new ActionRecorder();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(R.anim.activity_slide_right_to_center, R.anim.activity_fade_out);
        setContentView(R.layout.activity_profile);
        context = this;

        //Setup Status Bar
        try{
            getSupportActionBar().hide();
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);

            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(getResources().getColor(R.color.minimalGray));
            window.setNavigationBarColor(getResources().getColor(R.color.white));
        }
        catch (Exception z){
            //Title bar already disabled?
        }

        communityPointsButton = (Button) findViewById(R.id.communityPointsButton);
        travelStatisticsButton = (Button) findViewById(R.id.travelStatisticsButton);
        profileSettingsButton = (Button) findViewById(R.id.profileSettingsButton);
        signOutButton = (Button) findViewById(R.id.signOutButton);

        nameTextView = (TextView) findViewById(R.id.fullnameTextView);
        usernameTextView = (TextView) findViewById(R.id.usernameTextView);

        backgroundImage = (ImageView) findViewById(R.id.backgroundImage);
        profileImage = (CircleImageView) findViewById(R.id.profileImageImageView);
        profileBadgeHolderImageView = (CircleImageView) findViewById(R.id.profileBadgeHolderImageView);

        introAnimation();
        loadUserProfile();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //Check if user pressed ok in the gallery image selection
        if(resultCode == RESULT_OK && data !=null && requestCode == 3) {
            //Insert the new Image
            Uri selectedImage = data.getData();
            profileImage.setImageURI(selectedImage);
            //Upload it in the FirebaseStorage as well
            String timeOfCreation = String.valueOf(System.currentTimeMillis()/1000);

            storageReference = FirebaseStorage.getInstance().getReference().child(timeOfCreation);
            //On Success, Store the image URL in the users data in realtime database
            storageReference.putFile(selectedImage).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    if (taskSnapshot.getMetadata() != null) {
                        if (taskSnapshot.getMetadata().getReference() != null) {
                            Task<Uri> result = taskSnapshot.getStorage().getDownloadUrl();
                            result.addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    String imageUrl = uri.toString();
                                    //createNewPost(imageUrl);

                                    //On success - Store the image url it in the user data in Realtime Database

                                    //Get Logged in user UID
                                    String UID = FirebaseAuth.getInstance().getUid();

                                    //Get firebase realtime database reference
                                    FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
                                    firebaseDatabase.getReference().child("users").child(UID).child("profileimage").setValue(imageUrl);
                                }
                            });
                        }
                    }
                }});
        }
    }

    void introAnimation(){
        Animation slide_left_1 = AnimationUtils.loadAnimation(context,R.anim.slide_left);
        slide_left_1.setDuration(400);
        Animation slide_left_2 = AnimationUtils.loadAnimation(context,R.anim.slide_left);
        slide_left_2.setDuration(600);
        Animation slide_left_3 = AnimationUtils.loadAnimation(context,R.anim.slide_left);
        slide_left_3.setDuration(800);
        Animation slide_left_4 = AnimationUtils.loadAnimation(context,R.anim.slide_left);
        slide_left_4.setDuration(1000);

        communityPointsButton.startAnimation(slide_left_1);
        travelStatisticsButton.startAnimation(slide_left_2);
        profileSettingsButton.startAnimation(slide_left_3);
        signOutButton.startAnimation(slide_left_4);

        Animation slide_down = AnimationUtils.loadAnimation(context, R.anim.slide_down);
        backgroundImage.startAnimation(slide_down);
        profileImage.startAnimation(slide_down);
        profileBadgeHolderImageView.startAnimation(slide_down);

        Animation fade_in = AnimationUtils.loadAnimation(context, R.anim.fade_in);
        nameTextView.startAnimation(fade_in);
        usernameTextView.startAnimation(fade_in);
    }

    void loadUserProfile(){
        //Get Logged in user UID
        String UID = FirebaseAuth.getInstance().getUid();

        //Get firebase realtime database reference
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        DatabaseReference userReference = firebaseDatabase.getReference().child("users").child(UID);

        //Setup clickable profile icon
        profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Record View Click
                actionRecorder.addAction(view, "click", context);

                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, 3);
            }
        });

        //Get the current profile image
        //If user has a profile image uploaded, load it in the imageView
        if(!userReference.child("profileimage").getKey().isEmpty()){
            userReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                        //Get the image URL
                        String imageURL = snapshot.child("profileimage").getValue().toString();
                        //Use Picasso to load the image in the imageView
                        Picasso.with(context).load(imageURL).into(profileImage);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
        //If user has no uploaded profile image, load a temporary one
        else{
            profileImage.setImageResource(R.drawable.tap_here);
        }

        //Display the current user badge
        userReference.child("statistics").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot dataSnapshot: snapshot.getChildren()){
                    if(dataSnapshot.getKey().equals("xp")){
                        int xp = Integer.parseInt(dataSnapshot.getValue().toString());

                        if(xp < 100) {
                            //0 level - empty badge
                        }
                        else if(xp < 225) {
                            profileBadgeHolderImageView.setImageResource(R.drawable.rising_star);
                        }
                        else if(xp < 475) {
                            profileBadgeHolderImageView.setImageResource(R.drawable.star_badge);
                        }
                        else if(xp < 975) {
                            profileBadgeHolderImageView.setImageResource(R.drawable.star_medal);
                        }
                        else if(xp < 1975) {
                            profileBadgeHolderImageView.setImageResource(R.drawable.star_award);
                        }
                        else if(xp < 3975) {
                            profileBadgeHolderImageView.setImageResource(R.drawable.star_collector);
                        }
                        else if(xp < 7975) {
                            profileBadgeHolderImageView.setImageResource(R.drawable.star_pro);
                        }
                        else if(xp < 15975) {
                            profileBadgeHolderImageView.setImageResource(R.drawable.star_master);
                        }
                        else {
                            profileBadgeHolderImageView.setImageResource(R.drawable.star_king);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        //Get all the required TextViews to display the name + surname + username of the current user
        TextView fullnameTextView = findViewById(R.id.fullnameTextView);
        TextView usernameTextView = findViewById(R.id.usernameTextView);

        //Update the TextViews with the userdata
        userReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                fullnameTextView.setText(snapshot.child("firstname").getValue() + " " + snapshot.child("lastname").getValue());
                usernameTextView.setText("@" + snapshot.child("username").getValue());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public void finish() {
        super.finish();
        //Exit animation
        overridePendingTransition(R.anim.activity_fade_in, R.anim.activity_slide_center_to_right);
    }

    public void goToMapTab(View view){
        //Record View Click
        actionRecorder.addAction(view, "click", context);

        finish();
    }

    public void goToCommunityPointsTab(View view){
        //Record View Click
        actionRecorder.addAction(view, "click", context);

        Intent intent =new Intent(this, CommunityPointsActivity.class);
        startActivity(intent);
    }

    public void goToTravelStatisticsTab(View view){
        //Record View Click
        actionRecorder.addAction(view, "click", context);

        Intent intent =new Intent(this, TravelStatisticsActivity.class);
        startActivity(intent);
    }

    public void signOut(View view){
        //Record View Click
        actionRecorder.addAction(view, "click", context);

        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        firebaseAuth.signOut();

        //Send a broadcast to end main activity
        Intent intent = new Intent("finish_main_activity");
        sendBroadcast(intent);

        finish();
        startActivity(new Intent(context, SplashScreen.class));

    }
}