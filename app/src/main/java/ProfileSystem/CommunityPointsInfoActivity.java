package ProfileSystem;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.example.onwheelsapi28android90.R;

import Helpers.ActionRecorder;

public class CommunityPointsInfoActivity extends AppCompatActivity {

    ActionRecorder actionRecorder = new ActionRecorder();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_community_points_info);

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


    public void goToCommunityPointsTab(View view){
        //Record View Click
        actionRecorder.addAction(view, "click", this);

        finish();
    }
}