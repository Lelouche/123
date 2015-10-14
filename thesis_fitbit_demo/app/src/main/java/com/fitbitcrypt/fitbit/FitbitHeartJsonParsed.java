package com.fitbitcrypt.fitbit;

import com.fitbitcrypt.Constants;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by ajpeacock0_desktop on 2/10/2015.
 */
public class FitbitHeartJsonParsed {

    private JSONArray dataset = null;
    private int datasetInterval = 0;
    private String dateTime = null;

    public FitbitHeartJsonParsed(String data) throws JSONException {
        JSONObject json = new JSONObject(data).getJSONObject(Constants.TAG_INTRADAY);
        this.dataset = json.getJSONArray(Constants.TAG_DATASET);
        this.datasetInterval = json.getInt(Constants.TAG_DATASET_INTERVAL);

        JSONArray jsonArray = new JSONObject(data).getJSONArray(Constants.TAG_ACTIVITIES_HEART);
        this.dateTime = jsonArray.getJSONObject(0).getString(Constants.TAG_ACTIVITIES_DATE_TIME);
    }

    public JSONArray getDataset() {
        return dataset;
    }

    public int getDatasetInterval() {
        return datasetInterval;
    }

    public String getDateTime() {
        return dateTime;
    }
}
