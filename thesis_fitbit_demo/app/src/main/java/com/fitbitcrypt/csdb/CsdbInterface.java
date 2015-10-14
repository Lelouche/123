package com.fitbitcrypt.csdb;

import android.util.Log;

import com.fitbitcrypt.AsyncResponse;
import com.fitbitcrypt.Constants;
import com.fitbitcrypt.db_util.DbUtil;
import com.fitbitcrypt.db_util.HeartRateTuple;
import com.fitbitcrypt.db_util.Tuple;
import com.fitbitcrypt.fitbit.FitbitHeartJsonParsed;
import com.fitbitcrypt.retrieve_data.ResultsActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ajpeacock0_desktop on 26/09/2015.
 */
public class CsdbInterface implements AsyncResponse {
    private final String TAG = CsdbInterface.this.getClass().getSimpleName();
    private static CsdbInterface instance = null;
    private List<CsdbInsertAsyncTask> insertTasks;
    private String lastInteraction;

    /**
     * Singleton design pattern
     * @return
     */
    public static CsdbInterface getInst() {
        if (instance == null) instance = new CsdbInterface();
        return instance;
    }

    /**
     * This method is called by a class to send the given data to the appropriate table
     * @param request
     * @param result
     */
    public void sendServerSideSqlData(String request, String result) {
        lastInteraction = Constants.INSERT_QUERY;
        Log.d(TAG, "sendServerSideSqlData request: " + request + " result: " + result);
        insertTasks = new ArrayList<>();

        if (request.equals(Constants.REQUEST_HEART_DATA)) {
            List<HeartRateTuple> heartRateTuples = null;
            try {
                // gets a list of HeartRateTuples in the form (clientId,date,time,level,value)
                heartRateTuples = DbUtil.getHeartRateTuples(new FitbitHeartJsonParsed(result));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            assert heartRateTuples != null;
            // send each heart rate tuple to the CSDB
            for (HeartRateTuple heart : heartRateTuples) {
                Log.d(TAG, "Send heart " + Integer.toString(heart.getValue()));
                insertTasks.add(new CsdbInsertAsyncTask(heart));
            }
            executeTasks();
        }
    }

    public void stopServerSideSqlData() {
        Log.d(TAG, "stopServerSideSqlData");
        if (insertTasks != null) cancelTasks();
    }

    /**
     * This method is called by a class to execute a query on the database
     * @param query
     */
    public void queryServerSideSql(String query) {
        lastInteraction = Constants.SELECT_QUERY;
        new CsdbSelectAsyncTask(query, this).execute();
    }

    public void executeSelectAllFromHeartRate() {
        // TODO: run store.selectAllFromHeartRate
    }

    private void executeTasks() {
        for (CsdbInsertAsyncTask task : insertTasks) {
            task.execute();
        }
    }

    private void cancelTasks() {
        for (CsdbInsertAsyncTask task : insertTasks) {
            task.cancel(false);
        }
    }

    @Override
    public void processFinish(JSONObject json) {
        Log.d(TAG, "processFinish - lastInteraction: " + lastInteraction);
        Log.d(TAG, "processFinish - json: " + json.toString());
        if (lastInteraction.equals(Constants.SELECT_QUERY)) {
            ResultsActivity.updateResults(json);
        }
    }

    @Override
    public void processFinish(String request, String feed) {

    }
}
