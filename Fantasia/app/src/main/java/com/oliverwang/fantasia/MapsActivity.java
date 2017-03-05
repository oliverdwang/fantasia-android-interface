package com.oliverwang.fantasia;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMyLocationButtonClickListener, ActivityCompat.OnRequestPermissionsResultCallback {

    private GoogleMap mMap;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private NearFieldDatabaseHelper myDb;

    private Button returnButton;
    private Button manageButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        returnButton = (Button) findViewById(R.id.button_return);
        returnButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MapsActivity.this, MainActivity.class));
            }
        });
        manageButton = (Button) findViewById(R.id.button_manage);
        manageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LayoutInflater configureNearFieldInflater = getLayoutInflater();
                View alertDialogconfigureNearFieldLayout = configureNearFieldInflater.inflate(R.layout.alertdialog_configurenearfielddatabase, null);
                /*
                final EditText setBroker = (EditText) alertDialogconfigureNearFieldLayout.findViewById(R.id.editText_broker);
                final EditText setPort = (EditText) alertDialogconfigureNearFieldLayout.findViewById(R.id.editText_port);
                final EditText setClientID = (EditText) alertDialogconfigureNearFieldLayout.findViewById(R.id.editText_clientid);
                Button helpBroker = (Button) alertDialogconfigureNearFieldLayout.findViewById(R.id.button_helpBroker);
                helpBroker.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        setBroker.setError("Enter the IP Address of the broker\nDefault IP for internal MQTT Broker is 127.0.0.1\nFormat for IP is ###.###.###.###");
                    }
                });
                Button helpPort = (Button) alertDialogconfigureNearFieldLayout.findViewById(R.id.button_helpPort);
                helpPort.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        setPort.setError("Enter the port that the broker listens to\nFor standard brokers, the port is 1883\nFor SSL brokers, the port is 8883");
                    }
                });
                Button helpClientid = (Button) alertDialogconfigureNearFieldLayout.findViewById(R.id.button_helpClientid);
                helpClientid.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        setClientID.setError("Enter the client ID for this device\nAny standard descriptive text string works");
                    }
                });
                */
                AlertDialog.Builder configureNearFieldBuilder = new AlertDialog.Builder(MapsActivity.this);
                configureNearFieldBuilder.setView(alertDialogconfigureNearFieldLayout);
                configureNearFieldBuilder.setTitle("Configure Objects");
                configureNearFieldBuilder.setPositiveButton("Done",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                AlertDialog configureDatabaseAlert = configureNearFieldBuilder.create();
                configureDatabaseAlert.show();
            }
        });

        myDb = new NearFieldDatabaseHelper(getApplicationContext());


    }

    @Override
    public boolean onMyLocationButtonClick() {
        gotoCurrentPosition();
        // Return false so that we don't consume the event and the default behavior still occurs
        // (the camera animates to the user's current position).
        return false;
    }

    public void gotoCurrentPosition() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Criteria criteria = new Criteria();

        //go to current location
        if(checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED || checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            Location location = locationManager.getLastKnownLocation(locationManager.getBestProvider(criteria, false));
            if (location != null) {
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 13));

                CameraPosition cameraPosition = new CameraPosition.Builder()
                        .target(new LatLng(location.getLatitude(), location.getLongitude()))      // Sets the center of the map to location user
                        .zoom(20)                   // Sets the zoom
                        .bearing(225)                // Sets the orientation of the camera
                        .tilt(40)                   // Sets the tilt of the camera to 40 degrees
                        .build();                   // Creates a CameraPosition from the builder
                mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
            }
        } else {
            Toast.makeText(getApplicationContext(),"Location permissions not granted. Please manually enable permissions through settings.",Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        statusCheck(); //check if location service is enabled
        mMap.setOnMyLocationButtonClickListener(this);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
        }
        gotoCurrentPosition();
    }

    public void statusCheck() {
        final LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("This feature requires location services to be enabled. Do you want to enable it?")
                    .setCancelable(false)
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        public void onClick(final DialogInterface dialog, final int id) {
                            startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                        }
                    })
                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                        public void onClick(final DialogInterface dialog, final int id) {
                            dialog.cancel();
                            startActivity(new Intent(MapsActivity.this, MainActivity.class));
                        }
                    });
            final AlertDialog alert = builder.create();
            alert.show();

        }
    }
}
