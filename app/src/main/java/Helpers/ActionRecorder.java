package Helpers;

import android.content.Context;
import android.view.View;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class ActionRecorder {

    // Create an instance of SimpleDateFormat used for formatting
    // the string representation of date according to the chosen pattern
    DateFormat df = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");

    public void addAppBootToLog(Context context){
        try {
            // Get the today date using Calendar object.
            Date today = Calendar.getInstance().getTime();
            // Using DateFormat format method we can create a string
            // representation of a date with the defined format.
            String currentTime = df.format(today);

            String finalString = "\n--- [" + currentTime + "] APP STARTED ---\n\n";

            //Wipe previous log, start a new one
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(context.openFileOutput("user_log.txt", Context.MODE_PRIVATE));
            System.out.println("EDW" + context.getFileStreamPath("user_log.txt").getAbsolutePath());
            outputStreamWriter.append(finalString);
            outputStreamWriter.close();
        }
        catch (IOException e) {
            //Log.e("Exception", "File write failed: " + e.toString());
        }
    }

    public void addAction(View view, String action, Context context) {
        try {
            // Get the today date using Calendar object.
            Date today = Calendar.getInstance().getTime();
            // Using DateFormat format method we can create a string
            // representation of a date with the defined format.
            String currentTime = df.format(today);
            String finalString = "TIME: [" + currentTime + "]\nCONTEXT: {" + context + "}\nVIEW: Class{" + view.getClass().getName() + "}, id{" + context.getResources().getResourceEntryName(view.getId()) + "}\nACTION: "+ action + "\n\n";

            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(context.openFileOutput("user_log.txt", Context.MODE_APPEND));
            System.out.println("EDW" + context.getFileStreamPath("user_log.txt").getAbsolutePath());
            outputStreamWriter.append(finalString);
            outputStreamWriter.close();
        }
        catch (IOException e) {
            //Log.e("Exception", "File write failed: " + e.toString());
        }
    }

    public void addAction(GoogleMap googleMap, String action, Context context) {
        try {
            // Get the today date using Calendar object.
            Date today = Calendar.getInstance().getTime();
            // Using DateFormat format method we can create a string
            // representation of a date with the defined format.
            String currentTime = df.format(today);
            String finalString = "TIME: [" + currentTime + "]\nCONTEXT: {" + context + "}\nVIEW: Class{GoogleMap}, id{googleMap}\nACTION: "+ action + "\n\n";

            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(context.openFileOutput("user_log.txt", Context.MODE_APPEND));
            System.out.println("EDW" + context.getFileStreamPath("user_log.txt").getAbsolutePath());
            outputStreamWriter.append(finalString);
            outputStreamWriter.close();
        }
        catch (IOException e) {
            //Log.e("Exception", "File write failed: " + e.toString());
        }
    }

    public void addAction(Marker marker, String action, Context context) {
        try {
            // Get the today date using Calendar object.
            Date today = Calendar.getInstance().getTime();
            // Using DateFormat format method we can create a string
            // representation of a date with the defined format.
            String currentTime = df.format(today);
            String finalString = "TIME: [" + currentTime + "]\nCONTEXT: {" + context + "}\nVIEW: Class{MARKER}, id{" + marker.getId() + "}\nACTION: "+ action + "\n\n";

            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(context.openFileOutput("user_log.txt", Context.MODE_APPEND));
            System.out.println("EDW" + context.getFileStreamPath("user_log.txt").getAbsolutePath());
            outputStreamWriter.append(finalString);
            outputStreamWriter.close();
        }
        catch (IOException e) {
            //Log.e("Exception", "File write failed: " + e.toString());
        }
    }
}
