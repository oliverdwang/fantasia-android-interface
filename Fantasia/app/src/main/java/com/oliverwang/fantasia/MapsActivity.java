package com.oliverwang.fantasia;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.wifi.WifiManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteOrder;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMyLocationButtonClickListener, ActivityCompat.OnRequestPermissionsResultCallback, LocationListener {

    private GoogleMap mMap;

    private NearFieldDatabaseHelper myDb;

    private LocationManager mqttLocationManager;

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

        //start location manager used for checking if any objects within range
        mqttLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if(checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED || checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED ) {
            mqttLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 0, this); //every 5 seconds check for near field objects within range
        }

        //instantiate database with all near field objects
        myDb = new NearFieldDatabaseHelper(getApplicationContext());

        returnButton = (Button) findViewById(R.id.button_return);
        returnButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MapsActivity.this, MainActivity.class));
            }
        });
        addButton = (Button) findViewById(R.id.button_add); //
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { //if add new near field object button selected
                //create layout
                LayoutInflater addObjectInflater = getLayoutInflater();
                View alertDialogAddObject = addObjectInflater.inflate(R.layout.alertdialog_addobject, null);
                final EditText setName = (EditText) alertDialogAddObject.findViewById(R.id.editText_name);
                final EditText setAddress = (EditText) alertDialogAddObject.findViewById(R.id.editText_address);
                final EditText setLat = (EditText) alertDialogAddObject.findViewById(R.id.editText_lat);
                final EditText setLong = (EditText) alertDialogAddObject.findViewById(R.id.editText_long);
                final EditText setRadius = (EditText) alertDialogAddObject.findViewById(R.id.editText_radius);
                final EditText setTopic = (EditText) alertDialogAddObject.findViewById(R.id.editText_topic);
                final EditText setActivateMessage = (EditText) alertDialogAddObject.findViewById(R.id.editText_activateMessage);
                final EditText setDeactivateMessage = (EditText) alertDialogAddObject.findViewById(R.id.editText_deactivateMessage);
                final RadioGroup setMethod = (RadioGroup) alertDialogAddObject.findViewById(R.id.radioGroup_setMethod);
                final CheckBox localDebugging = (CheckBox) alertDialogAddObject.findViewById(R.id.checkBox_localDebug);
                setMethod.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(RadioGroup group, int checkedId) { //check which method selected in radio group to selection location method
                        int id=setMethod.getCheckedRadioButtonId();
                        View radioButton = setMethod.findViewById(id);
                        if(radioButton.getId() == R.id.radioButton_currentLocation) {
                            Log.v("radio group", "current loc");
                            method = 0;
                            setAddress.setEnabled(false);
                            setLat.setEnabled(false);
                            setLong.setEnabled(false);
                        } else if (radioButton.getId() == R.id.radioButton_address) {
                            Log.v("radio group", "address");
                            method = 1;
                            setAddress.setEnabled(true);
                            setLat.setEnabled(false);
                            setLong.setEnabled(false);
                        } else if (radioButton.getId() == R.id.radioButton_coordinates) {
                            Log.v("radio group", "coords");
                            method = 2;
                            setLat.setEnabled(true);
                            setLong.setEnabled(true);
                            setAddress.setEnabled(false);
                        }
                    }
                });
                AlertDialog.Builder addObjectBuilder = new AlertDialog.Builder(MapsActivity.this);
                addObjectBuilder.setView(alertDialogAddObject);
                addObjectBuilder.setTitle("Add Object");
                addObjectBuilder.setPositiveButton("Save", null);
                addObjectBuilder.setNegativeButton("Exit",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                final AlertDialog addObjectAlert = addObjectBuilder.create();
                addObjectAlert.setOnShowListener(new DialogInterface.OnShowListener() { //"SAVE" button must be done through onshowlistener so if any required fields are empty then the dialog doesn;t close
                    @Override
                    public void onShow(DialogInterface dialog) {
                        Button button = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE);
                        button.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                //if the user decides the save the new object
                                //set default temp lat & long as 0
                                double newLatitude = 0;
                                double newLongitude = 0;
                                boolean canSubmit = true;
                                if(setName.getText().toString().equals("")) {
                                    //if name field not filled in, prompt user to fill in before submitting again
                                    setName.setError("Please add a name");
                                    canSubmit = false;
                                } else if(setRadius.getText().toString().equals("")) {
                                    //if radius field not filled in, prompt user to fill in before submitting again
                                    setRadius.setError("Please add a radius");
                                    canSubmit = false;
                                } else if(setTopic.getText().toString().equals("")) {
                                    //if topic field not filled in, prompt user to fill in before submitting again
                                    setTopic.setError("Please add a topic");
                                    canSubmit = false;
                                } else if(setActivateMessage.getText().toString().equals("")) {
                                    //if message field not filled in, prompt user to fill in before submitting again
                                    setActivateMessage.setError("Please add a message");
                                    canSubmit = false;
                                } else if(setDeactivateMessage.getText().toString().equals("")) {
                                    //if message field not filled in, prompt user to fill in before submitting again
                                    setDeactivateMessage.setError("Please add a message");
                                    canSubmit = false;
                                } else {
                                    switch(method) {
                                        case -1:
                                            Log.v("location method", "moone");
                                            //if no location method selected
                                            Toast.makeText(getApplicationContext(),"Please select a location type",Toast.LENGTH_SHORT).show();
                                            canSubmit = false;
                                            break;

                                        case 0:
                                            Log.v("location method", "current location");
                                            if (getCurrentLocation() != null) {
                                                Location temp = getCurrentLocation();
                                                newLatitude = temp.getLatitude();
                                                newLongitude = temp.getLongitude();
                                            } else {
                                                canSubmit = false;
                                            }
                                            break;
                                            /*
                                            //if use current location method selected
                                            if(getCurrentLatitude() != 0) {
                                                newLatitude = getCurrentLatitude();
                                            } else {
                                                //something went wrong retrieving current position
                                                canSubmit = false;
                                            }
                                            if(getCurrentLongitude() != 0) {
                                                newLongitude = getCurrentLongitude();
                                            } else {
                                                //something went wrong retrieving current position
                                                canSubmit = false;
                                            }
                                            break;
                                            */

                                        case 1:
                                            Log.v("location method", "address");
                                            //if use address as location method selected
                                            if(setAddress.getText().toString().equals("")) {
                                                //if address field is blank
                                                setAddress.setError("Please add an address");
                                                canSubmit = false;
                                            } else {
                                                Address temp = getCoordinatesFromAddress(setAddress.getText().toString());
                                                if(temp == null) {
                                                    //if address to coordinate api does not recognize input as address
                                                    setAddress.setError("Please use a valid address");
                                                    canSubmit = false;
                                                } else {
                                                    newLatitude = temp.getLatitude();
                                                    newLongitude = temp.getLongitude();
                                                }
                                            }
                                            break;

                                        case 2: //@todo fix coordinate system, not showing up correctly
                                            Log.v("location method", "coordinates");
                                            //if use coordinates as location method selection
                                            if(setLat.getText().toString().equals("")) {
                                                //if latitude field not filled in, prompt user to fill in before submitting again
                                                setLat.setError("Please add a latitude");
                                                canSubmit = false;
                                            } else if(setLong.getText().toString().equals("")) {
                                                //if longitude field not filled in, prompt user to fill in before submitting again
                                                setLong.setError("Please add a longitude");
                                                canSubmit = false;
                                            } else {
                                                newLatitude = Double.parseDouble(setLat.getText().toString());
                                                newLongitude = Double.parseDouble(setLong.getText().toString());
                                            }
                                            break;
                                    }
                                    Log.v("new object added", "canSubmit value: " + canSubmit + "\n newLat: " + newLatitude + "\nnewLong: " + newLongitude);
                                    if(newLatitude != 0 && newLongitude != 0 && canSubmit == true) { //if all the information needed is valid
                                        //debug stuff
                                        Log.v("new object added","setLat value: " + setLat.getText().toString() + "\nsetLong value: " + setLong.getText().toString());
                                        Log.v("new object added","Name: " + setName.getText().toString() + "\nLat: " + newLatitude + "\nLong: " + newLongitude + "\nRadius: " + setRadius.getText().toString() + "\nTopic: " + setTopic.getText().toString() + "\nMessage: " + setActivateMessage.getText().toString());
                                        //(i) insert into database for future loads
                                        boolean success = false;
                                        if(localDebugging.isChecked() == true) { //if local debugging enabled, add the current ip
                                            success = myDb.insertData(setName.getText().toString(), newLatitude, newLongitude, Integer.parseInt(setRadius.getText().toString()), setTopic.getText().toString(), setActivateMessage.getText().toString(), setDeactivateMessage.getText().toString(), getIp());
                                        } else { // if local debugging disabled, pass null as current ip
                                            success = myDb.insertData(setName.getText().toString(), newLatitude, newLongitude, Integer.parseInt(setRadius.getText().toString()), setTopic.getText().toString(), setActivateMessage.getText().toString(), setDeactivateMessage.getText().toString(), null);
                                        }
                                        if (success) {
                                            Log.v("new object added","successfully added to database");
                                        } else {
                                            Log.v("new object added","NOT successfully added to database");
                                        }
                                        //(ii) add a marker to the map where the new object is
                                        mMap.addMarker(new MarkerOptions().position(new LatLng(newLatitude, newLongitude)).title(setName.getText().toString()));
                                        addObjectAlert.dismiss();
                                    } else {
                                        //@todo handle if lat or long is 0
                                    }
                                }
                            }
                        });
                    }
                });
                addObjectAlert.show();
            }
        });
        removeButton = (Button) findViewById(R.id.button_remove);
        removeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String connectParams[] = {"publish", "lol text", "phone/activateLED","0"};
                publishMQTTmessage(connectParams);

                LayoutInflater removeObjectInflater = getLayoutInflater();
                View alertDialogconfigureNearFieldLayout = removeObjectInflater.inflate(R.layout.alertdialog_removeobject, null);
                AlertDialog.Builder removeObjectBuilder = new AlertDialog.Builder(MapsActivity.this);
                removeObjectBuilder.setView(alertDialogconfigureNearFieldLayout);
                removeObjectBuilder.setTitle("Remove Object");
                removeObjectBuilder.setPositiveButton("Save",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                //@todo remove objects from list view OR use google maps and allow to delete markers corresponding to their respective object
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

    public String getIp() {
        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
        int ipAddress = wifiManager.getConnectionInfo().getIpAddress();

        // Convert little-endian to big-endianif needed
        if (ByteOrder.nativeOrder().equals(ByteOrder.LITTLE_ENDIAN)) {
            ipAddress = Integer.reverseBytes(ipAddress);
        }

        byte[] ipByteArray = BigInteger.valueOf(ipAddress).toByteArray();

        String ipAddressString;
        try {
            ipAddressString = InetAddress.getByAddress(ipByteArray).getHostAddress();
        } catch (UnknownHostException ex) {
            Log.e("WIFIIP", "Unable to get host address.");
            ipAddressString = null;
        }
        return ipAddressString;
    }

    @Override
    public void onLocationChanged(Location location) { //called every 5 seconds
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        Cursor objects = myDb.getAllData();
        //get all entries in database
        try {
            //for new objects
            while (objects.moveToNext()) {
                Location tempObject = new Location(locationManager.getBestProvider(criteria, false));
                tempObject.setLatitude(objects.getDouble(2));
                tempObject.setLongitude(objects.getDouble(3));
                if(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    if(tempObject.distanceTo(getCurrentLocation()) <= objects.getInt(4) + getCurrentLocation().getAccuracy()) {
                        if(objects.getInt(8) == 0) { //activate object
                            Toast.makeText(getApplicationContext(),objects.getString(1) + " activated!",Toast.LENGTH_SHORT).show();
                            String connectParams[] = {"publish", objects.getString(6),objects.getString(5),"0"};
                            publishMQTTmessage(connectParams);
                        }
                        myDb.updateState(objects.getInt(0), 1);
                    } else if(objects.getInt(7) == 1) { //if disconnected for 5 sec, keep attached      //ANTITHRASH SERVICE (ATS): 10 second hysteresis
                        //keep object activated
                        myDb.updateState(objects.getInt(0), 2);
                    } else if(objects.getInt(7) == 2) { //if disconnected for 10 sec, keep attached
                        //keep object activated
                        myDb.updateState(objects.getInt(0), 3);
                    } else if(objects.getInt(7) == 3) { //if disconnected for 15 sec, deactivate
                        if(objects.getInt(8) == 0) { //deactivate object
                            Toast.makeText(getApplicationContext(),objects.getString(1) + " deactivated!",Toast.LENGTH_SHORT).show();
                            String connectParams[] = {"publish", objects.getString(7),objects.getString(5),"0"};
                            publishMQTTmessage(connectParams);
                        }
                        myDb.updateState(objects.getInt(0), 0);
                    }
                } else {
                    if(tempObject.distanceTo(getCurrentLocation()) <= objects.getInt(4)) {
                        if(objects.getInt(8) == 0) { //activate object
                            Toast.makeText(getApplicationContext(),objects.getString(1) + " activated!",Toast.LENGTH_SHORT).show();
                            String connectParams[] = {"publish", objects.getString(6),objects.getString(5),"0"};
                            publishMQTTmessage(connectParams);
                        }
                        myDb.updateState(objects.getInt(0), 1);
                    } else if(objects.getInt(7) == 1) { //if disconnected for 5 sec, keep attached      //ANTITHRASH SERVICE (ATS): 10 second hysteresis
                        //keep object activated
                        myDb.updateState(objects.getInt(0), 2);
                    } else if(objects.getInt(7) == 2) { //if disconnected for 10 sec, keep attached
                        //keep object activated
                        myDb.updateState(objects.getInt(0), 3);
                    } else if(objects.getInt(7) == 3) { //if disconnected for 15 sec, deactivate
                        if(objects.getInt(8) == 0) { //deactivate object
                            Toast.makeText(getApplicationContext(),objects.getString(1) + " deactivated!",Toast.LENGTH_SHORT).show();
                            String connectParams[] = {"publish", objects.getString(7),objects.getString(5),"0"};
                            publishMQTTmessage(connectParams);
                        }
                        myDb.updateState(objects.getInt(0), 0);
                    }
                }
            }
        } finally {
            //close cursor in end to conserve resources
            objects.close();
        }
    }

    // Interface to call the construction of MQTT client from fragments.
    public void createMQTTClient(String connectParams[]) {

        // This method is called from  MQTTConnectFragment and it passes an array of
        // strings with the information gathered from the GUI to create an MQQT client
        MQTTClientHelper mqttClient = new MQTTClientHelper();
        mqttClient.execute(connectParams);
    }

    public void publishMQTTmessage(String publishParams[]) {

        MQTTClientHelper mqttClient = new MQTTClientHelper();
        // This method is called from  MQTTPublishFragment and it passes an array of
        // strings with the information gathered from the GUI to create an MQQT message

        createMQTTClient();

        mqttClient.execute(publishParams);
    }

    public void createMQTTClient() {

        //TODO: get shared prefs

        // This String is built depending on the type of connection and data from the UI
        String URIbroker;

        URIbroker = "tcp://" + brokerString + ":" + portString;
        protocol = "tcp";

        // Bundle the parameters, and call the parent Activity method to start the connection
        String connectParams[] = {"connect", brokerString, portString,
                URIbroker, clientString, protocol, filePath};
        // This method passes an array of strings with the information gathered from the GUI to create an MQTT client
        MQTTClientHelper mqttClient = new MQTTClientHelper();
        mqttClient.execute(connectParams);
    }

    @Override
    public void onProviderDisabled(String provider) { //called if no GPS
        final AlertDialog.Builder builder = new AlertDialog.Builder(this); //create alert dialog to prompt turn on location services
        builder.setMessage("This feature requires location services to be enabled. Do you want to enable it?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS)); //brings user to settings so that they can enable location services
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) { //else if the user doesn't want to send them back to the main activity that doesn't require location services
                        dialog.cancel();
                        startActivity(new Intent(MapsActivity.this, MainActivity.class));
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }

    @Override
    public void onProviderEnabled(String provider) { //called if has GPS
        //do nothing
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        // TODO Auto-generated method stub

    }

    public Address getCoordinatesFromAddress(String strAddress){ //method to parse user input string into an address, requires internet

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
    public boolean onMyLocationButtonClick() { //if topright button click
        gotoCurrentPosition(); //bring camera back to suer position
        // Return false so that we don't consume the event and the default behavior still occurs
        // (the camera animates to the user's current position).
        return false;
    }

    public Location getCurrentLocation() { //sends current location
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        if(checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED || checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) { //if has permissions to use location services
            Location location = locationManager.getLastKnownLocation(locationManager.getBestProvider(criteria, false));
            if (location != null) { //if current location isn't nonexistent, return it
                return location;
            }
            Log.v("getCurrentLocation", "is null");
        } else { //if no permissions
            Toast.makeText(getApplicationContext(),"Location permissions not granted. Please manually enable permissions through settings.",Toast.LENGTH_LONG).show(); //tell the user
        }
        return null; //if did not successfully send location, send back null
    }

    public double getCurrentLatitude() { //similar to get current location, but only sends the latitude of the current location
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Criteria criteria = new Criteria();

        //go to current location
        if(checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED || checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) { //if has permissions to use location services
            Location location = locationManager.getLastKnownLocation(locationManager.getBestProvider(criteria, false));
            if (location != null) { //if current location isn't nonexistent, return it
                return location.getLatitude();
            } else {
                Log.v("getCurrentLatitude","currnet location is null and no permissions were granted"); //tell the user
            }
        } else { //if no permissions
            Toast.makeText(getApplicationContext(),"Location permissions not granted. Please manually enable permissions through settings.",Toast.LENGTH_LONG).show(); //tell the user
        }
        return 0;
    }

    public double getCurrentLongitude() { //similar to get current location, but only sends the longitude of the current location
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Criteria criteria = new Criteria();

        //go to current location
        if(checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED || checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) { //if has permissions to use location services
            Location location = locationManager.getLastKnownLocation(locationManager.getBestProvider(criteria, false));
            if (location != null) { //if current location isn't nonexistent, return it
                return location.getLongitude();
            }
        } else { //if no permissions
            Toast.makeText(getApplicationContext(),"Location permissions not granted. Please manually enable permissions through settings.",Toast.LENGTH_LONG).show(); //tell the user
        }
        return 0;
    }

    public void gotoCurrentPosition() { //method to automove map to the current position of the user
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Criteria criteria = new Criteria();

        //go to current location
        if(checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED || checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            Location location = locationManager.getLastKnownLocation(locationManager.getBestProvider(criteria, false));
            if (location != null) { //if location isn't nonexistent
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 13));

                CameraPosition cameraPosition = new CameraPosition.Builder()
                        .target(new LatLng(location.getLatitude(), location.getLongitude()))      // Sets the center of the map to location user
                        .zoom(19)                   // Sets the zoom
                        //.bearing(225)                // Sets the orientation of the camera
                        .tilt(20)                   // Sets the tilt of the camera to 40 degrees
                        .build();                   // Creates a CameraPosition from the builder
                mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
            }
        } else { //if no permissions
            Toast.makeText(getApplicationContext(),"Location permissions not granted. Please manually enable permissions through settings.",Toast.LENGTH_LONG).show(); //tell the user
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
        mMap = googleMap; //instantiate map
        mMap.setOnMyLocationButtonClickListener(this);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true); //enable my location dot
        }

        gotoCurrentPosition(); //set camera to current position

        Cursor objects = myDb.getAllData(); //get all objects
        try {
            while (objects.moveToNext()) {
                String tempName = objects.getString(1);
                Double tempLat = objects.getDouble(2);
                Double tempLong = objects.getDouble(3);
                //int tempRadius = objects.getInt(4);
                mMap.addMarker(new MarkerOptions().position(new LatLng(tempLat, tempLong)).title(tempName)); //add all objects to map
            }
        } finally {
            objects.close(); //close cursor to save resources
        }

        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(getApplicationContext(),"For best results, wait 10 seconds for high accuracy GPS to activate",Toast.LENGTH_LONG).show();
        }
    }
}
