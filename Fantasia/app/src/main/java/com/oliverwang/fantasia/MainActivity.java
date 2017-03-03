package com.oliverwang.fantasia;

import android.Manifest;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.view.ContextThemeWrapper;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.eclipse.moquette.server.Server;

import java.io.IOException;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private ListView verboseLog;
    private Button configureBroker;
    private Button runBroker;
    private Button configureClient;
    private Button runClient;
    private Toolbar toolbar;

    public ArrayList<String> log;

    //@todo add function to refresh listview on update of log arraylist
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //setup all objects
        verboseLog = (ListView) findViewById(R.id.verbose_log);
        configureBroker = (Button) findViewById(R.id.configure_broker);
        runBroker = (Button) findViewById(R.id.run_broker);
        configureClient = (Button) findViewById(R.id.configure_client);
        runClient = (Button) findViewById(R.id.run_client);

        //set onClickListener for buttons
        configureBroker.setOnClickListener(buttonOnClickListener);

        //setup arraylist
        log = new ArrayList<String>();
        ArrayAdapter<String> logAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, android.R.id.text1, log);
        verboseLog.setAdapter(logAdapter);
    }

    public boolean isPermissionGranted() {
        boolean writeExternalStorage = false;
        boolean accessFineLocation = false;
        if (Build.VERSION.SDK_INT >= 23) {
            Log.v("permissions", "need runtime dialog");
            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                Log.v("external storage", "permission is granted");
                writeExternalStorage = true;
            } else {
                Log.v("external storage", "Permission is revoked");
                writeExternalStorage = false;
                // ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
            }
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                Log.v("fine location", "permission is granted");
                accessFineLocation = true;
            } else {
                Log.v("fine location", "Permission is revoked");
                accessFineLocation = false;
                // ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
            }

            if (writeExternalStorage == false && accessFineLocation == false) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.ACCESS_FINE_LOCATION}, 1);
                return false;
            } else if (writeExternalStorage == false && accessFineLocation == true) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                return false;
            } else if (writeExternalStorage == true && accessFineLocation == false) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
                return false;
            } else if (writeExternalStorage == true && accessFineLocation == true) {
                return true;
            } else return false;
        }
        //permission is automatically granted on any sdk lower than 23 upon installation
        Log.v("all perms", "Permission is granted");
        return true;

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        for (int i = 0; i < grantResults.length - 1; i++) {
            if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                if (permissions[i].equals(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    Toast.makeText(getApplicationContext(), "Broker permissions added", Toast.LENGTH_SHORT).show();
                    /*
                    //run broker
                    try {
                        new Server().startServer();
                        Toast.makeText(getApplicationContext(),"Broker started",Toast.LENGTH_LONG).show();
                    } catch (IOException e) {
                        e.printStackTrace();
                        Log.v("launch broker","io exception");
                    }
                    */
                } else if (permissions[i].equals(Manifest.permission.ACCESS_FINE_LOCATION)) {
                    Toast.makeText(getApplicationContext(), "Geofencing permissions added", Toast.LENGTH_SHORT).show();
                    /*
                    Toast.makeText(getApplicationContext(),"Geofencing Permissions Added",Toast.LENGTH_LONG).show();
                    */

                }
            }
        }
    }

    //@todo add custom alert dialogs for configureBroker, runBroker, configureClient, and runClient
    private View.OnClickListener buttonOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(final View v) {

            switch (v.getId()) {

                case R.id.configure_broker:
                    LayoutInflater inflater = getLayoutInflater();
                    View alertDialogConfigureBrokerLayout = inflater.inflate(R.layout.alertdialog_configurebroker, null);
                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    builder.setView(alertDialogConfigureBrokerLayout);
                    builder.setTitle("Configure Broker");
                    builder.setPositiveButton("Save",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    //@todo set broker settings
                                    dialog.dismiss();
                                }
                            });
                    builder.setNegativeButton("Exit",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog,
                                                    int which) {
                                    dialog.dismiss();
                                }
                            });
                    AlertDialog alert = builder.create();
                    alert.show();
                    break;

                case R.id.run_broker:
                    if (isPermissionGranted()) {
                        //run broker
                        try {
                            new Server().startServer();
                            Toast.makeText(getApplicationContext(), "Broker started", Toast.LENGTH_SHORT).show();
                        } catch (IOException e) {
                            e.printStackTrace();
                            Log.v("launch broker", "io exception");
                        }
                    }
                    break;

                case R.id.configure_client:

                    break;

                case R.id.run_client:

                    break;
            }
        }
    };
}
