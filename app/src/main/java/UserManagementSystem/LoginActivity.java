package UserManagementSystem;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.RotateAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.onwheelsapi28android90.MapActivity;
import com.example.onwheelsapi28android90.R;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import AppStartupManager.SplashScreen;
import Helpers.ActionRecorder;

public class LoginActivity extends AppCompatActivity {

    Button backButton, loginButton, loginWithGoogleButton;
    TextView loginTitle, loginDescription, forgotPasswordText, orText;
    EditText emailInput, passwordInput;
    ImageView loginImage;
    ConstraintLayout bottomTab;
    FirebaseAuth firebaseAuth;
    View divider1, divider2;

    //Google Sign in
    GoogleSignInOptions googleSignInOptions;
    GoogleSignInClient googleSignInClient;

    Context context;

    ActionRecorder actionRecorder = new ActionRecorder();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

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

        backButton = (Button) findViewById(R.id.backButton);
        loginButton = (Button) findViewById(R.id.loginButton);
        loginWithGoogleButton = (Button) findViewById(R.id.loginWithGoogle);

        loginTitle = (TextView) findViewById(R.id.Title);
        loginDescription = (TextView) findViewById(R.id.Description);
        forgotPasswordText = (TextView) findViewById(R.id.forgotPasswordText);
        orText = (TextView) findViewById(R.id.orTextView);

        emailInput = (EditText) findViewById(R.id.chatRoomTitleEditText);
        passwordInput = (EditText) findViewById(R.id.passwordInputLogin);

        loginImage = (ImageView) findViewById(R.id.loginImage);

        bottomTab = (ConstraintLayout) findViewById(R.id.bottomTab);

        divider1 = (View) findViewById(R.id.divider1);
        divider2 = (View) findViewById(R.id.divider2);

        context = this;

        //Setup the firebase connections
        setupFirebase();
        //Setup the Google Sign in
        setupGoogleSignIn();
        //Play the intro animation
        introAnimation();
    }

    void introAnimation(){
        Animation fadeIn = new AlphaAnimation(0.0f, 1.0f);
        fadeIn.setDuration(1000);

        backButton.startAnimation(fadeIn);
        loginButton.startAnimation(fadeIn);
        loginWithGoogleButton.startAnimation(fadeIn);
        forgotPasswordText.startAnimation(fadeIn);
        loginImage.startAnimation(fadeIn);
        bottomTab.startAnimation(fadeIn);
        divider1.startAnimation(fadeIn);
        divider2.startAnimation(fadeIn);
        orText.startAnimation(fadeIn);

        //Setup moving input animation
        Animation movingLogo = new TranslateAnimation(500,0,0,0);
        movingLogo.setDuration(1300);
        movingLogo.setFillAfter(true);
        emailInput.startAnimation(movingLogo);

        movingLogo = new TranslateAnimation(500,0,0,0);
        movingLogo.setDuration(1500);
        movingLogo.setFillAfter(true);
        passwordInput.startAnimation(movingLogo);

    }

    //Plays the outro Animation + Calls the next activity
    void outroAnimation(){

        //Setup  animation
        Animation movingLogo = new TranslateAnimation(0,-1000,0,0);
        movingLogo.setDuration(1000);
        movingLogo.setFillAfter(true);
        emailInput.startAnimation(movingLogo);

        movingLogo = new TranslateAnimation(0,-1000,0,0);
        movingLogo.setDuration(1200);
        movingLogo.setFillAfter(true);
        movingLogo.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                //Make the popup menu fullscreen
                Animation movePopupToTop = new TranslateAnimation(0,0,0,-1500);
                movePopupToTop.setDuration(1200);
                movePopupToTop.setFillAfter(true);
                movePopupToTop.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        startActivity(new Intent(context, MapActivity.class));
                        overridePendingTransition(R.anim.fade_in, R.anim.activity_fade_out);
                        finish();
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });
                bottomTab.startAnimation(movePopupToTop);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        passwordInput.startAnimation(movingLogo);

        Animation fadeOut = new AlphaAnimation(1.0f, 0.0f);
        fadeOut.setFillAfter(true);
        fadeOut.setDuration(1000);

        backButton.startAnimation(fadeOut);
        loginButton.startAnimation(fadeOut);
        loginWithGoogleButton.startAnimation(fadeOut);
        forgotPasswordText.startAnimation(fadeOut);
        loginImage.startAnimation(fadeOut);
        divider2.startAnimation(fadeOut);
        divider1.startAnimation(fadeOut);
        orText.startAnimation(fadeOut);
    }

    public void setupFirebase(){
        firebaseAuth = FirebaseAuth.getInstance();
    }

    public void goToSplashScreen(View view){
        //Record View Click
        actionRecorder.addAction(view, "click",this);

        startActivity(new Intent(this, SplashScreen.class));
        overridePendingTransition(R.anim.activity_slide_up_to_center, R.anim.activity_slide_center_to_bottom);
        finish();
    }

    public void goToMap(View view){
        //Record View Click
        actionRecorder.addAction(view, "click",this);

        //Check if some/all required fields are empty
        if(emailInput.getText().toString().matches("") || passwordInput.getText().toString().matches("")){
            Toast.makeText(this, "Please fill all the Fields", Toast.LENGTH_SHORT).show();
        }
        else{
            //Login the user
            firebaseAuth.signInWithEmailAndPassword(emailInput.getText().toString(), passwordInput.getText().toString())
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful()){

                                //Play the activity outro
                                outroAnimation();

                                //Disable any button clicks
                                loginButton.setOnClickListener(null);
                                loginWithGoogleButton.setOnClickListener(null);
                                backButton.setOnClickListener(null);
                            }
                            else{
                                Toast.makeText(context, "Credentials are incorrect. Please try again.", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }

    public void goToPasswordResetActivity(View view){
        //Record View Click
        actionRecorder.addAction(view, "click",this);
    }

    public void setupGoogleSignIn(){
        googleSignInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestEmail().build();
        googleSignInClient = GoogleSignIn.getClient(this, googleSignInOptions);
    }

    public void googleAuthentication(View view){
        Intent signInIntent = googleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, 2000);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //Google sign in
        if(requestCode == 2000){
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);

            try {
                task.getResult(ApiException.class);

                startActivity(new Intent(context, MapActivity.class));
                overridePendingTransition(R.anim.fade_in, R.anim.activity_fade_out);
                finish();
            } catch (ApiException e) {
                //Toast.makeText(context, "Google Error", Toast.LENGTH_SHORT).show();
            }

        }
    }
}