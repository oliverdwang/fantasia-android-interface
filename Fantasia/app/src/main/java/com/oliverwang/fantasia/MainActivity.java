package com.oliverwang.fantasia;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
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
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.eclipse.moquette.server.Server;

import java.io.IOException;
import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import org.eclipse.moquette.proto.messages.AbstractMessage;
import org.eclipse.moquette.proto.messages.SubscribeMessage;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.eclipse.moquette.server.*;
import org.eclipse.moquette.spi.impl.*;
import org.eclipse.moquette.spi.impl.subscriptions.*;

import java.io.IOException;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    public String brokerString = "127.0.0.1";
    public String portString = "1883";
    public String clientString = "phone";
    public String protocol;
    public String filePath;

    private Button configureBroker;
    private Button runBroker;
    private Button configureClient;
    private Button runClient;
    private Button startGeo;

    //@todo add function to refresh listview on update of log arraylist
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //setup all objects
        configureBroker = (Button) findViewById(R.id.configure_broker);
        runBroker = (Button) findViewById(R.id.run_broker);
        configureClient = (Button) findViewById(R.id.configure_client);
        runClient = (Button) findViewById(R.id.run_client);
        startGeo = (Button) findViewById(R.id.button_startGeo);

        //set onClickListener for buttons
        configureBroker.setOnClickListener(buttonOnClickListener);
        runBroker.setOnClickListener(buttonOnClickListener);
        configureClient.setOnClickListener(buttonOnClickListener);
        runClient.setOnClickListener(buttonOnClickListener);
        startGeo.setOnClickListener(buttonOnClickListener);

        isPermissionGranted();
    }

    private View.OnClickListener buttonOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(final View v) {

            switch (v.getId()) {

                case R.id.configure_broker:
                    Log.v("test","something");
                    LayoutInflater configureBrokerInflater = getLayoutInflater();
                    View alertDialogConfigureBrokerLayout = configureBrokerInflater.inflate(R.layout.alertdialog_configurebroker, null);
                    AlertDialog.Builder configureBrokerBuilder = new AlertDialog.Builder(MainActivity.this);
                    configureBrokerBuilder.setView(alertDialogConfigureBrokerLayout);
                    configureBrokerBuilder.setTitle("Configure Broker");
                    configureBrokerBuilder.setPositiveButton("Save",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    //@todo set broker settings

                                    dialog.dismiss();
                                }
                            });
                    configureBrokerBuilder.setNegativeButton("Exit",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog,
                                                    int which) {
                                    dialog.dismiss();
                                }
                            });
                    final AlertDialog configureBrokerAlert = configureBrokerBuilder.create();
                    configureBrokerAlert.show();
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
                    LayoutInflater configureClientInflater = getLayoutInflater();
                    View alertDialogConfigureClientLayout = configureClientInflater.inflate(R.layout.alertdialog_configureclient, null);
                    final EditText setBroker = (EditText) alertDialogConfigureClientLayout.findViewById(R.id.editText_broker);
                    final EditText setPort = (EditText) alertDialogConfigureClientLayout.findViewById(R.id.editText_port);
                    final EditText setClientID = (EditText) alertDialogConfigureClientLayout.findViewById(R.id.editText_clientid);
                    Button helpBroker = (Button) alertDialogConfigureClientLayout.findViewById(R.id.button_helpBroker);
                    helpBroker.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            setBroker.setError("Enter the IP Address of the broker\nDefault IP for internal MQTT Broker is 127.0.0.1\nFormat for IP is ###.###.###.###");
                        }
                    });
                    Button helpPort = (Button) alertDialogConfigureClientLayout.findViewById(R.id.button_helpPort);
                    helpPort.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            setPort.setError("Enter the port that the broker listens to\nFor standard brokers, the port is 1883\nFor SSL brokers, the port is 8883");
                        }
                    });
                    Button helpClientid = (Button) alertDialogConfigureClientLayout.findViewById(R.id.button_helpClientid);
                    helpClientid.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            setClientID.setError("Enter the client ID for this device\nAny standard descriptive text string works");
                        }
                    });
                    AlertDialog.Builder configureClientBuilder = new AlertDialog.Builder(MainActivity.this);
                    configureClientBuilder.setView(alertDialogConfigureClientLayout);
                    configureClientBuilder.setTitle("Configure Client");
                    configureClientBuilder.setPositiveButton("Save",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    brokerString = setBroker.getText().toString();
                                    portString = setPort.getText().toString();
                                    clientString = setClientID.getText().toString();
                                }
                            });
                    configureClientBuilder.setNegativeButton("Exit",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog,
                                                    int which) {
                                    dialog.dismiss();
                                }
                            });
                    AlertDialog configureClientAlert = configureClientBuilder.create();
                    configureClientAlert.show();
                    break;

                case R.id.run_client:
                    // This String is built depending on the type of connection and data from the UI
                    String URIbroker;

                    URIbroker = "tcp://" + brokerString + ":" + portString;
                    protocol = "tcp";

                    // Bundle the parameters, and call the parent Activity method to start the connection
                    String connectParams[] = {"connect", brokerString, portString,
                            URIbroker, clientString, protocol, filePath};
                    createMQTTClient(connectParams);
                    break;

                case R.id.button_startGeo:
                    startActivity(new Intent(MainActivity.this, MapsActivity.class));
                    break;
            }
        }
    };

    public boolean isPermissionGranted() {
        ArrayList<String> permissions = new ArrayList<>();
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                    //prompt if want to enable again
                } else {
                    permissions.add(Manifest.permission.ACCESS_FINE_LOCATION);
                }
            }

            if (checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_COARSE_LOCATION)) {
                    //prompt if want to enable again
                } else {
                    permissions.add(Manifest.permission.ACCESS_COARSE_LOCATION);
                }
            }

            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    //prompt if want to enable again
                } else {
                    permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
                }
            }

            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    //prompt if want to enable again
                } else {
                    permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE);
                }
            }
            if(permissions.size() > 0) {
                ActivityCompat.requestPermissions(this, permissions.toArray(new String[permissions.size()]), 1);
            }

            if (permissions.size() > 0) {
                return false;
            } else {
                return true;
            }
        } else {
            return false;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        for (int i = 0; i < grantResults.length - 1; i++) {
            if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                if (permissions[i].equals(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    Toast.makeText(getApplicationContext(),"Permissions needed for integrated MQTT broker withheld, please manually change",Toast.LENGTH_SHORT).show();
                } else if (permissions[i].equals(Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    Toast.makeText(getApplicationContext(),"Permissions needed for integrated MQTT broker withheld, please manually change",Toast.LENGTH_SHORT).show();
                } else if (permissions[i].equals(Manifest.permission.ACCESS_FINE_LOCATION)) {
                    Toast.makeText(getApplicationContext(),"Permissions needed for near field integration withheld, please manually change",Toast.LENGTH_SHORT).show();
                } else if (permissions[i].equals(Manifest.permission.ACCESS_COARSE_LOCATION)) {
                    Toast.makeText(getApplicationContext(),"Permissions needed for near field integration withheld, please manually change",Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    public void createMQTTClient(String connectParams[]) {
        // This method passes an array of strings with the information gathered from the GUI to create an MQTT client
        MQTTClientHelper mqttClient = new MQTTClientHelper();
        mqttClient.execute(connectParams);
    }
}
