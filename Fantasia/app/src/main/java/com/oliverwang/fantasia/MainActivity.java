package com.oliverwang.fantasia;

import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.view.ContextThemeWrapper;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

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

    //@todo add custom alert dialogs for configureBroker, runBroker, configureClient, and runClient
    private View.OnClickListener buttonOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(final View v) {

            switch(v.getId()){

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

                    break;

                case R.id.configure_client:

                    break;

                case R.id.run_client:

                    break;
            }
        }
    };
}
