package com.oliverwang.fantasia;

import android.app.Service;
import android.content.Intent;
import android.database.Cursor;
import android.net.wifi.WifiManager;
import android.os.Binder;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.v4.content.LocalBroadcastManager;
import android.text.format.Formatter;
import android.util.Log;
import android.widget.Chronometer;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteOrder;

/**
 * Created by 48oli on 3/15/2017.
 */

public class ClientEmulatorService extends Service {

    private NearFieldDatabaseHelper myDb;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        new Thread(new Runnable() {
            @Override
            public void run() {

                while(true) {
                    try {
                        Thread.sleep(60000);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    //get ip address
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
                        Log.e("WIFI IP", "Unable to get host address.");
                        ipAddressString = null;
                    }
                    //open object database
                    myDb = new NearFieldDatabaseHelper(getApplicationContext());
                    Cursor objects = myDb.getAllData(); //get all objects
                    try {
                        while (objects.moveToNext()) {
                            if(objects.getString(9) != null) {
                                if (objects.getString(9).equals(ipAddressString)) {
                                    //TODO emulate object ping

                                    Intent intent = new Intent("PING_AS");
                                    // add data
                                    intent.putExtra("CLIENT", "message");
                                    LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
                                }
                            } else {
                                Log.v("client emulator service", objects.getString(1) + " does not have an ip in the database, so skipping...");
                            }
                        }
                    } finally {
                        objects.close(); //close cursor to save resources
                    }
                }

                //stopSelf();
            }
        }).start();
        return Service.START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        //TODO for communication return IBinder implementation
        return null;
    }
}