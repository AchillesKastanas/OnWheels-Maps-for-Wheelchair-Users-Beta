package AppStartupManager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.TransitionDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.onwheelsapi28android90.R;

import Helpers.ActionRecorder;
import UserManagementSystem.LoginActivity;
import UserManagementSystem.RegisterActivity;

public class SplashScreen extends AppCompatActivity {

    TextView title;
    TextView description;
    ImageView logo;
    ConstraintLayout background, popup;
    Context context;

    private int COARSE_LOCATION_PERMISSION_CODE = 1;
    private int FINE_LOCATION_PERMISSION_CODE = 2;

    ActionRecorder actionRecorder = new ActionRecorder();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        //Log the AppStart
        actionRecorder.addAppBootToLog(this);

        //Setup Status Bar
        try {
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

        context = this;

        title = (TextView) findViewById(R.id.textTitle);
        description = (TextView) findViewById(R.id.textDescription);
        logo = (ImageView) findViewById(R.id.imageLogo);
        background = (ConstraintLayout) findViewById(R.id.background);
        popup = (ConstraintLayout) findViewById(R.id.bottomTab);

        animateSplashScreenPhase1();
        askForAllPermissions();

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        //Check if Permissions are granted
        if (requestCode == COARSE_LOCATION_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permission 'COARSE_LOCATION' Granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Permission 'COARSE_LOCATION' Denied", Toast.LENGTH_SHORT).show();
            }
        }
        if (requestCode == FINE_LOCATION_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permission 'FINE_LOCATION' Granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Permission 'FINE_LOCATION' Denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    void animateSplashScreenPhase1(){

        //Setup fade in animation
        Animation fadeIn = new AlphaAnimation(0.0f, 1.0f);
        fadeIn.setDuration(1500);
        //Setup fade in 2 animation
        Animation fadeIn2 = new AlphaAnimation(0.0f, 1.0f);
        fadeIn2.setDuration(2000);

        fadeIn2.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
//                Intent intent = new Intent(context, MapActivity.class);
//                startActivity(intent);
//                finish();
                animateSplashScreenPhase2();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        //Setup dropping pin animation
        final Animation movingLogo = new TranslateAnimation(0,0,-500,0);
        movingLogo.setDuration(950);
        movingLogo.setFillAfter(true);
        logo.startAnimation(movingLogo);

        //Run on title + logo
        title.startAnimation(fadeIn);
        //logo.startAnimation(fadeIn);
        description.startAnimation(fadeIn2);
    }

    void animateSplashScreenPhase2(){
        //Setup lifting animation
        Animation movingLogo = new TranslateAnimation(0,0,0,-400);
        movingLogo.setDuration(750);
        movingLogo.setFillAfter(true);

        movingLogo.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                animateSplashScreenPhase3();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        logo.startAnimation(movingLogo);
        title.startAnimation(movingLogo);
        description.startAnimation(movingLogo);

        //Change background color
        ColorDrawable[] colorDrawables = {new ColorDrawable(Color.parseColor("#8B89C1")),
                new ColorDrawable(Color.WHITE)};
        TransitionDrawable transitionDrawable = new TransitionDrawable(colorDrawables);
        background.setBackground(transitionDrawable);
        transitionDrawable.startTransition(2000);

        //Change title color
        ObjectAnimator colorAnim = ObjectAnimator.ofInt(title, "textColor",
                Color.WHITE, Color.parseColor("#8B89C1"));
        colorAnim.setEvaluator(new ArgbEvaluator());
        colorAnim.start();

        //Change text color
        ObjectAnimator colorAnim2 = ObjectAnimator.ofInt(description, "textColor",
                Color.WHITE, Color.parseColor("#8B89C1"));
        colorAnim2.setEvaluator(new ArgbEvaluator());
        colorAnim2.start();
    }

    void animateSplashScreenPhase3(){
        //Popup menu
        Animation movingPopup = new TranslateAnimation(0,0,1000,0);
        movingPopup.setDuration(900);
        movingPopup.setFillAfter(true);

        popup.startAnimation(movingPopup);
        popup.setVisibility(View.VISIBLE);


    }

    void askForAllPermissions(){

        PermissionManager permissionManager = new PermissionManager(this);

        //Request The Required Location Permission
        permissionManager.requestCoarseLocationPermission();

        //Request The Required GPS Permission
        permissionManager.requestFineLocationPermission();
    }

    public void login(View view){

        //Record View Click
        actionRecorder.addAction(view, "click",this);

        //Setup fade out animation
        Animation fadeOut = new AlphaAnimation(1.0f, 0.0f);
        fadeOut.setDuration(500);
        fadeOut.setFillAfter(true);

        logo.startAnimation(fadeOut);
        title.startAnimation(fadeOut);
        description.startAnimation(fadeOut);

        TextView popupTitle = (TextView) findViewById(R.id.Title);
        TextView popupDescription = (TextView) findViewById(R.id.Description);
        Button loginButton = (Button) findViewById(R.id.loginButton);
        Button signupButton = (Button) findViewById(R.id.loginWithGoogleButton);
        loginButton.setOnClickListener(null);
        signupButton.setOnClickListener(null);

        popupTitle.startAnimation(fadeOut);
        popupDescription.startAnimation(fadeOut);
        loginButton.startAnimation(fadeOut);
        signupButton.startAnimation(fadeOut);

        //Make the popup menu fullscreen
        Animation movePopupToTop = new TranslateAnimation(0,0,0,-1500);
        movePopupToTop.setDuration(1000);
        movePopupToTop.setFillAfter(true);
        popup.startAnimation(movePopupToTop);

        startActivity(new Intent(context, LoginActivity.class));
        overridePendingTransition(R.anim.activity_slide_bottom_to_center, R.anim.activity_slide_center_to_up);
        finish();
    }

    public void signup(View view){
        //Record View Click
        actionRecorder.addAction(view, "click",this);

        //Setup fade out animation
        Animation fadeOut = new AlphaAnimation(1.0f, 0.0f);
        fadeOut.setDuration(500);
        fadeOut.setFillAfter(true);

        logo.startAnimation(fadeOut);
        title.startAnimation(fadeOut);
        description.startAnimation(fadeOut);

        TextView popupTitle = (TextView) findViewById(R.id.Title);
        TextView popupDescription = (TextView) findViewById(R.id.Description);
        Button loginButton = (Button) findViewById(R.id.loginButton);
        Button signupButton = (Button) findViewById(R.id.loginWithGoogleButton);
        loginButton.setOnClickListener(null);
        signupButton.setOnClickListener(null);

        popupTitle.startAnimation(fadeOut);
        popupDescription.startAnimation(fadeOut);
        loginButton.startAnimation(fadeOut);
        signupButton.startAnimation(fadeOut);

        //Make the popup menu fullscreen
        Animation movePopupToTop = new TranslateAnimation(0,0,0,-1500);
        movePopupToTop.setDuration(1000);
        movePopupToTop.setFillAfter(true);
        popup.startAnimation(movePopupToTop);

        startActivity(new Intent(context, RegisterActivity.class));
        overridePendingTransition(R.anim.activity_slide_bottom_to_center, R.anim.activity_slide_center_to_up);
        finish();
    }
}