package com.oliverwang.fantasia;

import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

/**
 * Created by 48oli on 3/4/2017.
 */

public class MQTTClientHelper extends AsyncTask<String, Void, String[]> {

    public String topicToPublish;
    public String content;
    public String broker;
    public String port;
    public String clientId;
    public String brokerURI;
    public MqttClient client;
    public MqttConnectOptions options;
    public int qos = 0;

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

    // To do after the Async task has finished
    @Override
    protected void onPostExecute(String[] result) {
        super.onPostExecute(result);

        // If execution of the task was successful, the result will not be null
        if (result != null) {
            switch (result[0]) {
                case "connect":
                    Log.d("Connect", "just connected");
                    //Toast.makeText(, "Connected to " + broker + " on Port " + port, Toast.LENGTH_SHORT).show();
                    break;

                case "publish":
                    Log.d("Publish", "just published");
                    //Toast.makeText(getApplicationContext(), "Published " + content + " on Topic " + topicToPublish, Toast.LENGTH_SHORT).show();
                    break;

                case "subscribe":
                    //Toast.makeText(getApplicationContext(), "Subscribed " + content + " to Topic " + topicToPublish, Toast.LENGTH_SHORT).show();
                    break;
            }
        } else {
            Log.d("Error", "could not preform action, check connectivity");
            //Toast.makeText(getApplicationContext(), "Could not perform action, check Connectivity ", Toast.LENGTH_SHORT).show();
        }
    }
}