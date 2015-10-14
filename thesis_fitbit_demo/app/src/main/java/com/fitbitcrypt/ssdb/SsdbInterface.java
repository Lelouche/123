package com.fitbitcrypt.ssdb;

import android.content.Intent;
import android.util.Log;

import com.fitbitcrypt.AppUtil;
import com.fitbitcrypt.AsyncResponse;
import com.fitbitcrypt.Constants;
import com.fitbitcrypt.fitbit.FitbitUtil;
import com.fitbitcrypt.retrieve_data.ResultsActivity;
import com.fitbitcrypt.db_util.DbUtil;
import com.fitbitcrypt.db_util.HeartRateTuple;
import com.fitbitcrypt.fitbit.FitbitHeartJsonParsed;
import com.fitbitcrypt.retrieve_data.RetrieveActivity;
import com.fitbitcrypt.send_data.SendActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import static android.support.v4.app.ActivityCompat.startActivity;

/**
 * Created by ajpeacock0_desktop on 26/09/2015.
 */
public  class SsdbInterface implements AsyncResponse {
    private final String TAG = SsdbInterface.this.getClass().getSimpleName();
    private static JsonParser jsonParser = new JsonParser();
    private static SsdbInterface instance = null;
    private List<SsdbAsyncTask> insertTasks;
    private String lastInteraction;

    /**
     * Singleton design pattern
     * @return
     */
    public static SsdbInterface getInst() {
        if (instance == null) instance = new SsdbInterface();
        return instance;
    }

    /**
     * This method starts a ASyncTask to send the given data to the given URL.
     * Results are given to processFinish
     * @param url
     * @param jsonObjSend
     */
    private SsdbAsyncTask getJsonToUrlTask(String url, JSONObject jsonObjSend) {
        return new SsdbAsyncTask(jsonParser, url, jsonObjSend, this);
    }

    private void executeTasks() {
        for (SsdbAsyncTask task : insertTasks) {
            task.execute();
        }
    }

    private void cancelTasks() {
        for (SsdbAsyncTask task : insertTasks) {
            task.cancel(false);
        }
    }

    /**
     * This method is called by a class to send the given data to the appropriate table
     * @param request
     * @param result
     * @param encryption
     */
    public void sendServerSideSqlData(String request, String result, String encryption) {
        lastInteraction = Constants.INSERT_QUERY;
        insertTasks = new ArrayList<>();

        if (request.equals(Constants.REQUEST_HEART_DATA)) {
            try {
                // gets a list of HeartRateTuples in the form (clientId,date,time,level,value)
                List<HeartRateTuple> heartRateTuples = DbUtil.getHeartRateTuples(new FitbitHeartJsonParsed(result));
                // for each tuple
                for (HeartRateTuple heart : heartRateTuples) {
                    JSONObject json = new JSONObject();
                    json.put(Constants.QUERY, DbUtil.getHeartRateInsertQuery(heart));
                    json.put(Constants.CONFIG, encryption);
                    insertTasks.add(getJsonToUrlTask(Constants.RUN_SELECT_QUERY_ENDPOINT, json));
                }
                executeTasks();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public void stopServerSideSqlData() {
        Log.d(TAG, "stopServerSideSqlData");
        if (insertTasks != null) cancelTasks();
    }

    /**
     * This method is called by a class to execute a query on the database
     * @param query
     * @param encryption
     */
    public void queryServerSideSql(String query, String encryption) {
        lastInteraction = Constants.SELECT_QUERY;
        JSONObject json = new JSONObject();
        try {
            json.put(Constants.CONFIG, encryption);
            json.put(Constants.QUERY, query);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        getJsonToUrlTask(Constants.RUN_SELECT_QUERY_ENDPOINT, json).execute();
    }

    /**
     * Called by SsdbAsyncTask on onPostExecute
     * TODO: notify user if sending data was successful or not
     * TODO: notify user if query was successful or not
     * TODO: Give user query results
     * @param json
     */
    @Override
    public void processFinish(JSONObject json) {
        Log.d(TAG, "processFinish - json: " + json.toString());
        if (lastInteraction.equals(Constants.SELECT_QUERY)) {
            ResultsActivity.updateResults(json);
        }
    }


    @Override
    public void processFinish(String request, String feed) {

    }
}
