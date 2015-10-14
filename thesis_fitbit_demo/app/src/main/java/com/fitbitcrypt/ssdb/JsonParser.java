package com.fitbitcrypt.ssdb;

import java.io.IOException;

import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

public class JsonParser {
    private final String TAG = JsonParser.this.getClass().getSimpleName();

    public JsonParser() {}

    public JSONObject makeHttpRequest(String url, JSONObject jsonObjSend) {
        Log.d(TAG, "makeHttpRequest - JSONObject: "+jsonObjSend.toString());
        String json = "";

        try {
            // Create a new HttpClient and Post Header
            DefaultHttpClient httpClient = new DefaultHttpClient();
            HttpPost httpPostRequest = new HttpPost(url);
            // Add your data
            StringEntity se = new StringEntity(jsonObjSend.toString());
            // Set HTTP parameters
            httpPostRequest.setEntity(se);
            httpPostRequest.setHeader("Accept", "application/json");
            httpPostRequest.setHeader("Content-type", "application/json");
            // request method is GET
            ResponseHandler<String> responseHandler=new BasicResponseHandler();
            json = httpClient.execute(httpPostRequest, responseHandler);
        } catch (IOException e) {
            e.printStackTrace();
        }

        Log.d(TAG, "makeHttpRequest - json: "+json.toString());

        JSONObject jObj = null;
        try {
            jObj = new JSONObject(json);
        } catch (JSONException e) {
            Log.e(TAG, "JSON Parser - Error parsing data " + e.toString());
        }

        return jObj;
    }
}