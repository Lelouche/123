package com.fitbitcrypt;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import com.fitbitcrypt.csdb.DBInterfaceHolder;
import com.fitbitcrypt.retrieve_data.RetrieveActivity;
import com.fitbitcrypt.send_data.SendActivity;

import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Properties;

import ch.ethz.inf.vs.lubu.cyrptdbmodule.exceptions.CDBException;

public class MainActivity extends Activity {
    private final String TAG = MainActivity.this.getClass().getSimpleName();
    private Spinner deviceSpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initButtons();
        initSpinner();

        setupSshTunnel();
        initClientSideDbConnection();
    }

    /**
     * Set up a SSH Tunnel to the Server Side Proxy DB
     */
    private void setupSshTunnel() {
        Properties serverSideSshTunnelProperties = AppUtil.getProperties("ssdb_ssh_tunnel.properties", getAssets());
        Properties clientSideSshTunnelProperties = AppUtil.getProperties("csdb_ssh_tunnel.properties", getAssets());

        new ShhTunnel(serverSideSshTunnelProperties).execute();
        new ShhTunnel(clientSideSshTunnelProperties).execute();
    }

    private void initButtons() {
        Button sendDataBtn = (Button) findViewById(R.id.main_btn1);
        Button retrieveDataBtn = (Button) findViewById(R.id.main_btn2);

        sendDataBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                startSendData();
            }
        });

        retrieveDataBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                startRequestData();
            }
        });
    }

    private void initSpinner() {
        deviceSpinner = (Spinner) findViewById(R.id.device_spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,R.array.select_device_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        deviceSpinner.setAdapter(adapter);
    }

    private void initClientSideDbConnection() {
        Context con = this.getApplicationContext();
        // open settings file /raw/scheme.json
        int schemeID = con.getResources().getIdentifier("scheme", "raw", con.getPackageName());
        Reader reader = new InputStreamReader(con.getResources().openRawResource(schemeID));
        try {
            DBInterfaceHolder.init(reader, con);
        } catch (CDBException e) {
            e.printStackTrace();
        }
    }

    private void startSendData() {
        String selection = String.valueOf(deviceSpinner.getSelectedItem());
        String[] array = getResources().getStringArray(R.array.select_device_array);

        if (selection.equals(array[0])) {
            Log.d(TAG, "Selected Fitbit");
            Intent intent = new Intent(this, SendActivity.class);
            startActivity(intent);
        }
        if (selection.equals(array[1])) {
            Log.d(TAG, "Selected MSBand");
            // TODO: add support for MSBand
        }
    }

    private void startRequestData() {
        Intent intent = new Intent(this, RetrieveActivity.class);
        startActivity(intent);
    }
}