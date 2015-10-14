package com.fitbitcrypt.ssdb;

import android.os.AsyncTask;

import com.fitbitcrypt.AsyncResponse;

import org.json.JSONObject;

/**
 * Created by ajpeacock0_desktop on 24/09/2015.
 *
 * This class is used to run the jsonParser class on
 * a seperate thread from the UI thread
 */
public class SsdbAsyncTask extends AsyncTask<Void, Void, JSONObject> {
    private JsonParser jsonParser;
    private String url;
    private JSONObject jsonObjSend;
    private AsyncResponse delegate;

    public SsdbAsyncTask(JsonParser jsonParser, String url, JSONObject jsonObjSend, AsyncResponse delegate) {
        this.jsonParser = jsonParser;
        this.url = url;
        this.jsonObjSend = jsonObjSend;
        this.delegate = delegate;
    }

    protected JSONObject doInBackground(Void... data) {
        return jsonParser.makeHttpRequest(url, jsonObjSend);
    }

    protected void onPostExecute(JSONObject json) {
        delegate.processFinish(json);
    }
}
