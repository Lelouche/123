package com.fitbitcrypt.retrieve_data;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.fitbitcrypt.R;
import com.fitbitcrypt.ssdb.SsdbInterface;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Dan on 3/10/2015.
 */
public class ResultsActivity extends Activity {
    private final String TAG = ResultsActivity.this.getClass().getSimpleName();
    private static Context context;
    private static ListView resultList;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_results);
        context = this;
        initListview();
    }

    private void initListview() {
        resultList = (ListView) findViewById(R.id.results_listview);
    }
    public static void updateResults(JSONObject json) {
        if (resultList == null) return;

        List<String> list = new ArrayList<String>();
        JSONArray array;
        try {
            array = json.getJSONArray("result");
        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }

        for(int i = 0 ; i < array.length() ; i++){
            try {
                list.add(array.getJSONObject(i).toString());
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        // Break program when sending data
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(context, android.R.layout.simple_list_item_1, android.R.id.text1, list);
        resultList.setAdapter(adapter);
    }

}
