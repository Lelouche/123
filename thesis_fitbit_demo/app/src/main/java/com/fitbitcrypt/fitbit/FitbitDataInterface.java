package com.fitbitcrypt.fitbit;

import com.fitbitcrypt.AsyncResponse;

import org.json.JSONObject;

/**
 * Created by ajpeacock0_desktop on 30/08/2015.
 */
public class FitbitDataInterface implements AsyncResponse {

    private String accessToken, userId;
    private AsyncResponse delegate;

    private static FitbitDataInterface instance = null;

    public FitbitDataInterface(String userId, String accessToken, AsyncResponse delegate) {
        this.accessToken = accessToken;
        this.userId = userId;
        // This will point to null when AsyncResponse is destroyed (if rotation occurs) since it's a Activity
        this.delegate = delegate;
    }

    /**
     * Called by a class wanting data from the Fitbit API
     * The type of data is specified by the request
     * @param request
     * @param prevDay
     */
    public void getData(String request, int prevDay) {
        new FitbitDataAsyncTask(request, prevDay, userId, accessToken, this).execute();
    }

    public static void init(String userId, String accessToken, AsyncResponse delegate) {
        if (instance == null) instance = new FitbitDataInterface(userId, accessToken, delegate);
    }

    /**
     * Singleton design pattern
     * @return
     */
    public static FitbitDataInterface getInst() {
        assert instance != null;
        return instance;
    }

    @Override
    public void processFinish(JSONObject json) {

    }

    @Override
    public void processFinish(String request, String feed) {
        delegate.processFinish(request, feed);
    }
}
