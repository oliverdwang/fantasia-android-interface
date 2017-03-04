package com.oliverwang.fantasia;

import android.Manifest;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
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
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.eclipse.moquette.server.Server;

import java.io.IOException;
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

public class MainActivity extends AppCompatActivity {

    public String topicToPublish;
    public String content;
    public String broker;
    public String port;
    public String clientId;
    public String brokerURI;
    public MqttClient client;
    public MqttConnectOptions options;

    public int qos = 0;

    public String brokerString = "127.0.0.1";
    public String portString = "1883";
    public String clientString = "phone";
    public String protocol;
    public String filePath;

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
        runBroker.setOnClickListener(buttonOnClickListener);
        configureClient.setOnClickListener(buttonOnClickListener);
        runClient.setOnClickListener(buttonOnClickListener);

        //setup arraylist
        log = new ArrayList<String>();
        ArrayAdapter<String> logAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, android.R.id.text1, log);
        verboseLog.setAdapter(logAdapter);
    }

    private View.OnClickListener buttonOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(final View v) {

            switch (v.getId()) {

                case R.id.configure_broker:
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
                    // This String is built depending on the type of connection and data from the
                    // UI
                    String URIbroker;

                    URIbroker = "tcp://" + brokerString + ":" + portString;
                    protocol = "tcp";

                    // Bundle the parameters, and call the parent Activity method to start the connection
                    String connectParams[] = {"connect", brokerString, portString,
                            URIbroker, clientString, protocol, filePath};
                    createMQTTClient(connectParams);
                    break;
            }
        }
    };

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

    public void createMQTTClient(String connectParams[]) {

        // This method is called from  MQTTConnectFragment and it passes an array of
        // strings with the information gathered from the GUI to create an MQQT client
        MQTTClientClass mqttClient = new MQTTClientClass();
        mqttClient.execute(connectParams);
    }

    /*
    The AsyncTask is called with <Params, Progress, Result>
    This class contains all the Paho MQTT functionality
    */
    public class MQTTClientClass extends AsyncTask<String, Void, String[]> {

        @Override
        protected String[] doInBackground(String... paramString) {

            // The same Async function will be called from different fragments, keeping the unity
            // of the implementation. For this, the first string of the passed parameters is checked
            // for matches with the cases to process
            switch (paramString[0]) {

                // If called from MQTTConnectFragment
                case "connect":

                    // Retrieve the information from the arguments into the local variables
                    broker = paramString[1];
                    port = paramString[2];
                    brokerURI = paramString[3];
                    clientId = paramString[4];

                    // Add memory persistence to the client
                    MemoryPersistence persistence = new MemoryPersistence();

                    try {
                        // Create client with the given URI, ID and persistence, add options and session type
                        client = new MqttClient(brokerURI, clientId, persistence);
                        options = new MqttConnectOptions();
                        options.setCleanSession(true);
                        options.setConnectionTimeout(60);
                        options.setKeepAliveInterval(60);

                        // TODO: Rid these debugging prints
                        // Connect to the server
                        System.out.println("Connecting to broker: " + broker);
                        client.connect(options);
                        System.out.println("Connected");

                        return paramString;

                    } catch (MqttException me) {
                        // TODO: Rid these debugging prints
                        System.out.println("reason " + me.getReasonCode());
                        System.out.println("msg " + me.getMessage());
                        System.out.println("loc " + me.getLocalizedMessage());
                        System.out.println("cause " + me.getCause());
                        System.out.println("excep " + me);
                        me.printStackTrace();
                    } catch (Exception e) {
                        Log.d("Things Flow I/O", "Error " + e);
                        e.printStackTrace();
                    }
                    break;

                // If called from MQTTPublishFragment
                case "publish":

                    // Retrieve the information from the arguments into the local variables
                    content = paramString[1];
                    topicToPublish = paramString[2];
                    qos = Integer.parseInt(paramString[3]);

                    // Create the message to send and set the Quality of Service
                    // TODO: Rid these debugging prints
                    System.out.println("Publishing message: " + content);
                    MqttMessage message = new MqttMessage(content.getBytes());
                    message.setQos(qos);

                    try {
                        // Publish the msessage
                        client.publish(topicToPublish, message);
                        System.out.println("Message published");
                        // TODO: Rid these debugging prints
                    } catch (MqttException e) {
                        e.printStackTrace();
                        return null;
                    } catch (Exception e) {
                        e.printStackTrace();
                        return null;
                    }
                    return paramString;

                // If called from MQTTSubscribeFragment
                case "subscribe":
                    //TODO: Subscription extra actions, nothing so far
                    break;
            }
            return null;
        }
    }
}
