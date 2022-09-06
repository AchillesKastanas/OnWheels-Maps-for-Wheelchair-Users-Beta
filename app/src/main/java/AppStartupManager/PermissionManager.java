package AppStartupManager;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;

import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class PermissionManager {

    Context context;
    private int COARSE_LOCATION_PERMISSION_CODE = 1;
    private int FINE_LOCATION_PERMISSION_CODE = 2;

    PermissionManager(Context context){
        this.context = context;
    }

    public void requestCoarseLocationPermission() {
        //Check if permission is granted
        if (ContextCompat.checkSelfPermission(context,
                Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            //Toast.makeText(this, "Permission 'COARSE_LOCATION' Already Granted", Toast.LENGTH_SHORT).show();
        }
        //If the permission is NOT granted
        else {
            //Check if the App should show the user why the permission is needed (User has already denied the permission once).
            if (ActivityCompat.shouldShowRequestPermissionRationale((Activity) context, Manifest.permission.ACCESS_COARSE_LOCATION)) {

                new AlertDialog.Builder(context)
                        .setTitle("Permission Needed")
                        .setMessage("This permission is needed because this and that")
                        .setPositiveButton("ok ", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ActivityCompat.requestPermissions((Activity) context, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, COARSE_LOCATION_PERMISSION_CODE);
                            }
                        })
                        .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .create().show();

            }
            //Else (Its the first time) request the permission
            else {
                ActivityCompat.requestPermissions((Activity) context, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, COARSE_LOCATION_PERMISSION_CODE);
            }
        }
    }

    public void requestFineLocationPermission() {
        //Check if permission is granted
        if (ContextCompat.checkSelfPermission(context,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            //Toast.makeText(this, "Permission 'FINE_LOCATION' Already Granted", Toast.LENGTH_SHORT).show();
        }
        //If the permission is NOT granted
        else {
            //Check if the App should show the user why the permission is needed (User has already denied the permission once).
            if (ActivityCompat.shouldShowRequestPermissionRationale((Activity) context, Manifest.permission.ACCESS_FINE_LOCATION)) {

                new AlertDialog.Builder(context)
                        .setTitle("Permission Needed")
                        .setMessage("This permission is needed because this and that")
                        .setPositiveButton("ok ", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ActivityCompat.requestPermissions((Activity) context, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, FINE_LOCATION_PERMISSION_CODE);
                            }
                        })
                        .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .create().show();

            }
            //Else (Its the first time) request the permission
            else {
                ActivityCompat.requestPermissions((Activity) context, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, FINE_LOCATION_PERMISSION_CODE);
            }
        }
    }
}
