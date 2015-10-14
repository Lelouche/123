package com.fitbitcrypt.retrieve_data;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;

import com.fitbitcrypt.Constants;
import com.fitbitcrypt.R;

import com.fitbitcrypt.csdb.CsdbInterface;
import com.fitbitcrypt.ssdb.SsdbInterface;


/**
 * Created by Dan on 2/10/2015.
 */
public class RetrieveActivity extends Activity {
    private static ListView resultList;
    private final String TAG = RetrieveActivity.this.getClass().getSimpleName();
    private Spinner dbSpinner;
    private ListView queryList;
    private static Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_retrieve);
        context = this;
        initListView();
        initSpinner();
    }

    private void initSpinner() {
        ArrayAdapter<CharSequence> adapter;
        // layout_retrieve
        dbSpinner = (Spinner) findViewById(R.id.spinnerDB);
        adapter = ArrayAdapter.createFromResource(this, R.array.send_data_spinner_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        dbSpinner.setAdapter(adapter);
    }

    private void initListView() {
        queryList = (ListView) findViewById(R.id.query_listview);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, android.R.id.text1,getResources().getStringArray(R.array.query_array));
        queryList.setAdapter(adapter);
        queryList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int pos, long id) {
                String query = queryList.getItemAtPosition(pos).toString();
                Log.d(TAG, "Query: " + query);
                executeQueryToDb(query);
            }
        });
    }

    private void executeQueryToDb(String query) {
        String selection = String.valueOf(dbSpinner.getSelectedItem());
        String[] array = getResources().getStringArray(R.array.send_data_spinner_array);

        Log.d(TAG, "executeQueryToDb to selection: " + selection);
        // UnCryptDB
        if (selection.equals(array[0])) {
            SsdbInterface.getInst().queryServerSideSql(query, Constants.UNENCRYPTED_CONFIG);
        } else
        // SSDB
        if (selection.equals(array[1])) {
            SsdbInterface.getInst().queryServerSideSql(query, Constants.ENCRYPTED_CONFIG);
        } else
        // CSDB
        if (selection.equals(array[2])) {
            // TODO: implement CSDB query execution
            CsdbInterface.getInst().queryServerSideSql(query);
        } else {
            return;
        }
        goToResults();
    }

    public void goToResults() {
        Intent intent = new Intent(context, ResultsActivity.class);
        startActivity(intent);
    }

}
