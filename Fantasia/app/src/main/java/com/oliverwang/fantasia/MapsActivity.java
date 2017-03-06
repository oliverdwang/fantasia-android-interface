package com.oliverwang.fantasia;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
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
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMyLocationButtonClickListener, ActivityCompat.OnRequestPermissionsResultCallback {

    private GoogleMap mMap;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private NearFieldDatabaseHelper myDb;

    private Button returnButton;
    private Button addButton;
    private Button removeButton;

    private int method = -1;
    // -1: no method set
    // 0: use current location
    // 1: use address
    // 2: use coordinates

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        myDb = new NearFieldDatabaseHelper(getApplicationContext());

        returnButton = (Button) findViewById(R.id.button_return);
        returnButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MapsActivity.this, MainActivity.class));
            }
        });
        addButton = (Button) findViewById(R.id.button_add);
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LayoutInflater addObjectInflater = getLayoutInflater();
                View alertDialogAddObject = addObjectInflater.inflate(R.layout.alertdialog_addobject, null);
                final EditText setName = (EditText) alertDialogAddObject.findViewById(R.id.editText_name);
                final EditText setAddress = (EditText) alertDialogAddObject.findViewById(R.id.editText_address);
                final EditText setLat = (EditText) alertDialogAddObject.findViewById(R.id.editText_lat);
                final EditText setLong = (EditText) alertDialogAddObject.findViewById(R.id.editText_long);
                final EditText setRadius = (EditText) alertDialogAddObject.findViewById(R.id.editText_radius);
                final RadioGroup setMethod = (RadioGroup) alertDialogAddObject.findViewById(R.id.radioGroup_setMethod);
                setMethod.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(RadioGroup group, int checkedId) {
                        int id=setMethod.getCheckedRadioButtonId();
                        View radioButton = setMethod.findViewById(id);
                        if(radioButton.getId() == R.id.radioButton_currentLocation) {
                            method = 0;
                            setAddress.setEnabled(false);
                            setLat.setEnabled(false);
                            setLong.setEnabled(false);
                        } else if (radioButton.getId() == R.id.radioButton_address) {
                            method = 1;
                            setLat.setEnabled(false);
                            setLong.setEnabled(false);
                        } else if (radioButton.getId() == R.id.radioButton_coordinates) {
                            method = 2;
                            setAddress.setEnabled(false);
                        }
                    }
                });
                AlertDialog.Builder addObjectBuilder = new AlertDialog.Builder(MapsActivity.this);
                addObjectBuilder.setView(alertDialogAddObject);
                addObjectBuilder.setTitle("Add Object");
                addObjectBuilder.setPositiveButton("Save",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                double newLatitude = 0;
                                double newLongitude = 0;
                                if(setName.getText().toString().equals("")) {
                                    setName.setError("Please add a name");
                                } else if(setRadius.getText().toString().equals("")) {
                                    setRadius.setError("Please add a radius");
                                } else {
                                    switch(method) {
                                        case -1:
                                            Toast.makeText(getApplicationContext(),"Please select a location type",Toast.LENGTH_SHORT).show();
                                            break;

                                        case 0:
                                            if(getCurrentLatitude() != 0) {
                                                newLatitude = getCurrentLatitude();
                                            }
                                            if(getCurrentLongitude() != 0) {
                                                newLongitude = getCurrentLongitude();
                                            }
                                            break;

                                        case 1:
                                            if(setAddress.getText().toString().equals("")) {
                                                setAddress.setError("Please add an address");
                                            } else {
                                                Address temp = getCoordinatesFromAddress(setAddress.getText().toString());
                                                if(temp == null) {
                                                    setAddress.setError("Please use a valid address");
                                                } else {
                                                    newLatitude = temp.getLatitude();
                                                    newLongitude = temp.getLongitude();
                                                }
                                            }
                                            break;

                                        case 2: //@todo fix coordinate system, not showing up correctly
                                            if(setLat.getText().toString().equals("")) {
                                                setLat.setError("Please add a latitude");
                                            } else if(setLong.getText().toString().equals("")) {
                                                setLong.setError("Please add a longitude");
                                            } else {
                                                newLatitude = Double.parseDouble(setLat.getText().toString());
                                                newLongitude = Double.parseDouble(setLat.getText().toString());
                                            }
                                            break;
                                    }
                                    if(newLatitude != 0 && newLongitude != 0) {
                                        myDb.insertData(setName.getText().toString(), newLatitude, newLongitude, Integer.parseInt(setRadius.getText().toString()));
                                        mMap.addMarker(new MarkerOptions().position(new LatLng(newLatitude, newLongitude)).title(setName.getText().toString()));
                                    } else {
                                        //@todo handle if lat or long is 0
                                    }
                                }
                            }
                        });
                addObjectBuilder.setNegativeButton("Exit",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                AlertDialog addObjectAlert = addObjectBuilder.create();
                addObjectAlert.show();
            }
        });
        removeButton = (Button) findViewById(R.id.button_remove);
        removeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LayoutInflater removeObjectInflater = getLayoutInflater();
                View alertDialogconfigureNearFieldLayout = removeObjectInflater.inflate(R.layout.alertdialog_removeobject, null);
                AlertDialog.Builder removeObjectBuilder = new AlertDialog.Builder(MapsActivity.this);
                removeObjectBuilder.setView(alertDialogconfigureNearFieldLayout);
                removeObjectBuilder.setTitle("Remove Object");
                removeObjectBuilder.setPositiveButton("Save",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                //@todo add objects and check if any values are null
                            }
                        });
                removeObjectBuilder.setNegativeButton("Exit",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                AlertDialog removeObjectAlert = removeObjectBuilder.create();
                removeObjectAlert.show();
            }
        });
    }

    public Address getCoordinatesFromAddress(String strAddress){

        Geocoder coder = new Geocoder(this);
        List<Address> address;

        try {
            address = coder.getFromLocationName(strAddress,5);
            if (address==null) {
                return null;
            }
            Address location=address.get(0);
            return location;
        } catch(Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public boolean onMyLocationButtonClick() {
        gotoCurrentPosition();
        // Return false so that we don't consume the event and the default behavior still occurs
        // (the camera animates to the user's current position).
        return false;
    }

    public double getCurrentLatitude() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Criteria criteria = new Criteria();

        //go to current location
        if(checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED || checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            Location location = locationManager.getLastKnownLocation(locationManager.getBestProvider(criteria, false));
            if (location != null) {
                return location.getLatitude();
            }
        } else {
            Toast.makeText(getApplicationContext(),"Location permissions not granted. Please manually enable permissions through settings.",Toast.LENGTH_LONG).show();
        }
        return 0;
    }

    public double getCurrentLongitude() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Criteria criteria = new Criteria();

        //go to current location
        if(checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED || checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            Location location = locationManager.getLastKnownLocation(locationManager.getBestProvider(criteria, false));
            if (location != null) {
                return location.getLongitude();
            }
        } else {
            Toast.makeText(getApplicationContext(),"Location permissions not granted. Please manually enable permissions through settings.",Toast.LENGTH_LONG).show();
        }
        return 0;
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
                        .zoom(17)                   // Sets the zoom
                        .bearing(225)                // Sets the orientation of the camera
                        //.tilt(40)                   // Sets the tilt of the camera to 40 degrees
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

        Cursor objects = myDb.getAllData();
        try {
            while (objects.moveToNext()) {
                String tempName = objects.getString(1);
                Double tempLat = objects.getDouble(2);
                Double tempLong = objects.getDouble(3);
                //int tempRadius = objects.getInt(4);
                mMap.addMarker(new MarkerOptions().position(new LatLng(tempLat, tempLong)).title(tempName));
            }
        } finally {
            objects.close();
        }
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
