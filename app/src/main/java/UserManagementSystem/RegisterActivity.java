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
import android.view.animation.TranslateAnimation;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import AppStartupManager.SplashScreen;
import Helpers.ActionRecorder;

public class RegisterActivity extends AppCompatActivity {

    Button backButton, loginButton, loginWithGoogleButton;
    TextView loginTitle, loginDescription, forgotPasswordText;
    EditText usernameInput, passwordInput, emailInput, firstnameInput, lastnameInput;
    Spinner dayInput, monthInput, yearInput;
    ImageView loginImage;
    ConstraintLayout bottomTab;
    CheckBox checkBox;

    //Firebase variables
    FirebaseAuth firebaseAuth;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;

    //Google Sign in
    GoogleSignInOptions googleSignInOptions;
    GoogleSignInClient googleSignInClient;

    Context context;

    ActionRecorder actionRecorder = new ActionRecorder();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

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
        loginWithGoogleButton = (Button) findViewById(R.id.loginWithGoogleButton);

        loginTitle = (TextView) findViewById(R.id.Title);
        loginDescription = (TextView) findViewById(R.id.Description);

        usernameInput = (EditText) findViewById(R.id.chatRoomTitleEditText);
        passwordInput = (EditText) findViewById(R.id.passwordInput);
        emailInput = (EditText) findViewById(R.id.emailInput);
        firstnameInput = (EditText) findViewById(R.id.firstnameInput);
        lastnameInput = (EditText) findViewById(R.id.lastnameInput);

        dayInput = (Spinner) findViewById(R.id.dayInput);
        monthInput = (Spinner) findViewById(R.id.monthInput);
        yearInput = (Spinner) findViewById(R.id.yearInput);

        loginImage = (ImageView) findViewById(R.id.loginImage);

        bottomTab = (ConstraintLayout) findViewById(R.id.bottomTab);

        checkBox = (CheckBox) findViewById(R.id.termsCheckBox);

        context = this;

        //Setup all the available options on the spinners
        setupSpinners();
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
        bottomTab.startAnimation(fadeIn);

        //Setup dropping pin animation
        Animation movingLogo = new TranslateAnimation(500,0,0,0);
        movingLogo.setDuration(1300);
        movingLogo.setFillAfter(true);
        usernameInput.startAnimation(movingLogo);

        movingLogo = new TranslateAnimation(500,0,0,0);
        movingLogo.setDuration(1500);
        movingLogo.setFillAfter(true);
        passwordInput.startAnimation(movingLogo);

        movingLogo = new TranslateAnimation(500,0,0,0);
        movingLogo.setDuration(1700);
        movingLogo.setFillAfter(true);
        emailInput.startAnimation(movingLogo);

        movingLogo = new TranslateAnimation(500,0,0,0);
        movingLogo.setDuration(1900);
        movingLogo.setFillAfter(true);
        firstnameInput.startAnimation(movingLogo);

        movingLogo = new TranslateAnimation(500,0,0,0);
        movingLogo.setDuration(2100);
        movingLogo.setFillAfter(true);
        lastnameInput.startAnimation(movingLogo);

        movingLogo = new TranslateAnimation(500,0,0,0);
        movingLogo.setDuration(2300);
        movingLogo.setFillAfter(true);
        dayInput.startAnimation(movingLogo);

        movingLogo = new TranslateAnimation(500,0,0,0);
        movingLogo.setDuration(2300);
        movingLogo.setFillAfter(true);
        monthInput.startAnimation(movingLogo);

        movingLogo = new TranslateAnimation(500,0,0,0);
        movingLogo.setDuration(2300);
        movingLogo.setFillAfter(true);
        yearInput.startAnimation(movingLogo);

    }

    public void setupFirebase(){
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference();
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
        if(emailInput.getText().toString().matches("") || passwordInput.getText().toString().matches("")
        || usernameInput.getText().toString().matches("") || firstnameInput.getText().toString().matches("")
                || lastnameInput.getText().toString().matches("") || dayInput.getSelectedItem().toString().matches("")
                || monthInput.getSelectedItem().toString().matches("") || yearInput.getSelectedItem().toString().matches("")){
            Toast.makeText(this, "Please fill all the Fields", Toast.LENGTH_SHORT).show();
        }
        else if(!checkBox.isChecked()){
            Toast.makeText(this, "Please accept the Terms", Toast.LENGTH_SHORT).show();
        }
        else{
            //Create a new user on the Authentication tab of the Firebase Database
            firebaseAuth.createUserWithEmailAndPassword(emailInput.getText().toString(),passwordInput.getText().toString())
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful()){

                                loginButton.setOnClickListener(null);
                                loginWithGoogleButton.setOnClickListener(null);
                                backButton.setOnClickListener(null);

                                //On Success, create a new field in the users tab in the Realtime Database, so more info can be assigned to the user
                                DatabaseReference userReference = databaseReference.child("users").child(firebaseAuth.getUid());
                                userReference.child("username").setValue(usernameInput.getText().toString());
                                userReference.child("firstname").setValue(firstnameInput.getText().toString());
                                userReference.child("lastname").setValue(lastnameInput.getText().toString());
                                userReference.child("dateofbirth").setValue(dayInput.getSelectedItem().toString() + "." + monthInput.getSelectedItem().toString() + "." + yearInput.getSelectedItem().toString() + ".");
                                userReference.child("profileimage").setValue("https://firebasestorage.googleapis.com/v0/b/onwheels-maps.appspot.com/o/1652270641?alt=media&token=477934fd-1c70-4bfb-8ed5-1558cba80778");
                                userReference.child("statistics").child("xp").setValue("0");
                                userReference.child("statistics").child("distancetraveled").setValue("0");
                                userReference.child("statistics").child("totaltrips").setValue("0");
                                userReference.child("statistics").child("rampsused").setValue("0");
                                userReference.child("statistics").child("rampsviewed").setValue("0");
                                userReference.child("statistics").child("rampsplaced").setValue("0");
                                userReference.child("statistics").child("rampsliked").setValue("0");
                                userReference.child("statistics").child("rampsdisliked").setValue("0");
                                userReference.child("statistics").child("rampsenabled").setValue("0");
                                userReference.child("statistics").child("rampsdisabled").setValue("0");
                                userReference.child("statistics").child("chatroomscreated").setValue("0");
                                userReference.child("statistics").child("chatroomsjoined").setValue("0");
                                userReference.child("statistics").child("messagessent").setValue("0");
                                userReference.child("notifications").push().setValue("Welcome to the OnWheels Community. Enjoy your stay!");

                                startActivity(new Intent(context, MapActivity.class));
                                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                                finish();
                            }
                            else{
                                Toast.makeText(context,"Something went worng. Please try again.",Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }

    public void setupSpinners(){
        String[] days = new String[]{"1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23",
                "24", "25", "26", "27", "28", "29", "30", "31"};
        String[] months = new String[]{"January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"};
        String[] years = new String[]{"1901", "1902", "1903", "1904", "1905", "1906", "1907", "1908", "1909", "1910", "1911", "1912", "1913", "1914", "1915",
                "1916", "1917", "1918", "1919", "1920", "1921", "1922", "1923", "1924", "1925", "1926", "1927", "1928", "1929", "1930", "1931", "1932", "1933",
                "1934", "1935", "1936", "1937", "1938", "1939", "1940", "1941", "1942", "1943", "1944", "1945", "1946", "1947", "1948", "1949", "1950", "1951",
                "1952", "1953", "1954", "1955", "1956", "1957", "1958", "1959", "1960", "1961", "1962", "1963", "1964", "1965", "1966", "1967", "1968", "1969",
                "1970", "1971", "1972", "1973", "1974", "1975", "1976", "1977", "1978", "1979", "1980", "1981", "1982", "1983", "1984", "1985", "1986", "1987",
                "1988", "1989", "1990", "1991", "1992", "1993", "1994", "1995", "1996", "1997", "1998", "1999", "2000", "2001", "2002", "2003", "2004", "2005",
                "2006", "2007", "2008", "2009", "2010", "2011", "2012", "2013", "2014", "2015", "2016", "2017", "2018", "2019", "2020", "2021"};

        ArrayAdapter<String> dayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, days);
        ArrayAdapter<String> monthAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, months);
        ArrayAdapter<String> yearAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, years);

        dayInput.setAdapter(dayAdapter);
        monthInput.setAdapter(monthAdapter);
        yearInput.setAdapter(yearAdapter);
    }

    public void setupGoogleSignIn(){
        googleSignInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestEmail().build();
        googleSignInClient = GoogleSignIn.getClient(this, googleSignInOptions);
    }

    public void googleAuthentication(View view){
        Intent signInIntent = googleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, 1000);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //Google sign in
        if(requestCode == 1000){
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);

            try {
                task.getResult(ApiException.class);

                startActivity(new Intent(context, MapActivity.class));
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                finish();
            } catch (ApiException e) {
                //Toast.makeText(context, "Google Error", Toast.LENGTH_SHORT).show();
            }

        }
    }
}