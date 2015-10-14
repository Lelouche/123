package com.fitbitcrypt.send_data;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import com.fitbitcrypt.AppUtil;
import com.fitbitcrypt.Constants;
import com.fitbitcrypt.R;
import com.fitbitcrypt.csdb.CsdbInterface;
import com.fitbitcrypt.fitbit.FitBitActivity;
import com.fitbitcrypt.fitbit.FitbitDataInterface;
import com.fitbitcrypt.fitbit.FitbitUtil;
import com.fitbitcrypt.AsyncResponse;
import com.fitbitcrypt.ssdb.SsdbInterface;

import org.json.JSONObject;

import java.util.Properties;

public class SendActivity extends Activity implements AsyncResponse {
    private final String TAG = SendActivity.this.getClass().getSimpleName();
    private Spinner getDataSpinner, sendDataSpinner;
    private Button sendDataBtn, getDataBtn, stopSendBtn;
    private String lastDataRequestType, lastDataRequestValue;
    private TextView metaDataText, currentDate;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send);

        loginToFitbit();
        initButtons();
        initSpinner();
        initText();
    }

    /**
     * TODO: Test going back then into SendActivity
     */
    private void loginToFitbit() {
        Intent intent = new Intent(SendActivity.this, FitBitActivity.class);
        startActivityForResult(intent, Constants.GET_AUTH_REQUEST);
    }

    private void initButtons() {
        getDataBtn = (Button) findViewById(R.id.get_data_btn);
        setGetDatatBtns(false);
        sendDataBtn = (Button) findViewById(R.id.send_data_btn);
        setSendDataBtns(false);
        stopSendBtn = (Button) findViewById(R.id.stop_send_btn);

        getDataBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                String selection = String.valueOf(getDataSpinner.getSelectedItem());
                String[] array = getResources().getStringArray(R.array.get_data_spinner_array);

                Log.d(TAG, "getDataBtn clicked - selection: " + selection);
                // TODO: allow the user to select a previous day for the data
                // REQUEST_HEART_DATA
                if (selection.equals(array[0])) {
                    getData(Constants.REQUEST_HEART_DATA, 0);
                }
                // REQUEST_SLEEP_DATA
                if (selection.equals(array[1])) {
                    getData(Constants.REQUEST_SLEEP_DATA, 0);
                }
            }
        });

        sendDataBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                String selection = String.valueOf(sendDataSpinner.getSelectedItem());
                String[] array = getResources().getStringArray(R.array.send_data_spinner_array);

                Log.d(TAG, "sendDataBtn clicked - selection: " + selection);
                // UnCryptDB
                if (selection.equals(array[0])) {
                    SsdbInterface.getInst().sendServerSideSqlData(lastDataRequestType, lastDataRequestValue, Constants.UNENCRYPTED_CONFIG);
                }
                // SSDB
                if (selection.equals(array[1])) {
                    SsdbInterface.getInst().sendServerSideSqlData(lastDataRequestType, lastDataRequestValue, Constants.ENCRYPTED_CONFIG);
                }
                // CSDB
                if (selection.equals(array[2])) {
                    CsdbInterface.getInst().sendServerSideSqlData(lastDataRequestType, lastDataRequestValue);
                }
            }
        });

        stopSendBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                String selection = String.valueOf(sendDataSpinner.getSelectedItem());
                String[] array = getResources().getStringArray(R.array.send_data_spinner_array);

                Log.d(TAG, "sendDataBtn clicked - selection: " + selection);
                // UnCryptDB
                if (selection.equals(array[0])) {
                    SsdbInterface.getInst().stopServerSideSqlData();
                }
                // SSDB
                if (selection.equals(array[1])) {
                    SsdbInterface.getInst().stopServerSideSqlData();
                }
                // CSDB
                if (selection.equals(array[2])) {
                    CsdbInterface.getInst().stopServerSideSqlData();
                }
            }
        });
    }

    private void initSpinner() {
        ArrayAdapter<CharSequence> adapter;
        // layout_get
        getDataSpinner = (Spinner) findViewById(R.id.get_data_spinner);
        adapter = ArrayAdapter.createFromResource(this,R.array.get_data_spinner_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        getDataSpinner.setAdapter(adapter);
        // layout_send
        sendDataSpinner = (Spinner) findViewById(R.id.send_data_spinner);
        adapter = ArrayAdapter.createFromResource(this,R.array.send_data_spinner_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sendDataSpinner.setAdapter(adapter);
    }

    private void initText() {
        metaDataText = (TextView) findViewById(R.id.meta_data_text);
        currentDate = (TextView) findViewById(R.id.select_date_text);
        currentDate.setText(FitbitUtil.getCurrentDate());
    }

    /**
     * Executes an AsyncTask to request data from the Fitbit API
     * Calls the MainActivity.onFitbitDataResult method to return
     * the data gained and request type
     * @param request
     */
    private void getData(String request, int previousDay) {
        FitbitDataInterface.getInst().getData(request, previousDay);
    }

    /**
     * Return method for startActivityForResult. This is called for
     * - FitBitActivity : GET_AUTH_REQUEST
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (data != null && data.hasExtra("data")) {
            String result = data.getStringExtra("data");
            Log.d("onActivityResult", "GET_AUTH_REQUEST");
            Log.d("onActivityResult", "accessToken: " + result);
            initFitbitData(result);
            setGetDatatBtns(true);
        }
    }

    private void initFitbitData(String result) {
        Properties properties = AppUtil.getProperties("fitbit_cred.properties", getAssets());
        FitbitDataInterface.init(properties.getProperty("user_id"), result, this);
    }

    private void setGetDatatBtns(boolean b) {
        getDataBtn.setEnabled(b);
    }

    private void setSendDataBtns(boolean b) {
        sendDataBtn.setEnabled(b);
    }

    @Override
    public void processFinish(JSONObject json) {

    }

    /**
     * FitbitDataInterface returns result of data request
     * @param request
     * @param feed
     */
    @Override
    public void processFinish(String request, String feed) {
        String metaData = FitbitUtil.getMetaData(request, feed);
        Log.d(TAG, "metaData: "+metaData);
        metaDataText.setText(metaData);
        // saves the data for sending
        lastDataRequestType = request;
        lastDataRequestValue = feed;
        // enable sending of data
        setSendDataBtns(true);
    }
}