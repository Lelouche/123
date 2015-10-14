package com.fitbitcrypt.csdb;

import android.os.AsyncTask;
import android.util.Log;

import com.fitbitcrypt.AsyncResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import ch.ethz.inf.vs.lubu.cyrptdbmodule.exceptions.CDBException;
import ch.ethz.inf.vs.lubu.cyrptdbmodule.main.CDBResultSet;

/**
 * Created by ajpeacock0_desktop on 4/10/2015.
 */
public class CsdbSelectAsyncTask extends AsyncTask<Void, Void,JSONObject> {
    private final String TAG = CsdbSelectAsyncTask.this.getClass().getSimpleName();
    private AsyncResponse delegate;
    private String query;

    public CsdbSelectAsyncTask(String query, AsyncResponse delegate) {
        this.query = query;
        this.delegate = delegate;
    }

    @Override
    protected JSONObject doInBackground(Void... value) {
        DBStoreInterface store = DBInterfaceHolder.getCon();
        Log.d(TAG, "query: " + query);
        JSONObject returnJson = new JSONObject();
        JSONArray jsonArray = new JSONArray();
        try {
            CDBResultSet res = store.executeSelectQuery(query);

            while (res.next()) {
                JSONObject json = new JSONObject();
                for (int i = 0; i < res.getNumCols(); i++) {
                    json.put(res.getColumnName(i), res.getString(i));
                }
                jsonArray.put(json);
            }
            returnJson.put("result", jsonArray);
        } catch (CDBException | JSONException e) {
            e.printStackTrace();
        }

        return returnJson;
    }

    @Override
    protected void onPostExecute(JSONObject json) {
        Log.d(TAG, "json: " + json.toString());
        delegate.processFinish(json);
    }
}