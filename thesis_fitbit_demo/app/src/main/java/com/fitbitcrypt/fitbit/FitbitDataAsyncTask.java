package com.fitbitcrypt.fitbit;

import android.os.AsyncTask;
import android.util.Log;

import com.fitbitcrypt.Constants;
import com.fitbitcrypt.AsyncResponse;

import java.text.ParseException;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * Created by ajpeacock0_desktop on 3/10/2015.
 */
public class FitbitDataAsyncTask extends AsyncTask<Void, Void, String> {
    private final String TAG = FitbitDataAsyncTask.this.getClass().getSimpleName();

    private AsyncResponse delegate;
    private int prevDays;
    private String request, userId, accessToken, url;

    public FitbitDataAsyncTask(String request, int prevDays,
                               String userId, String accessToken, AsyncResponse delegate) {
        this.request = request;
        this.prevDays = prevDays;
        this.userId = userId;
        this.accessToken = accessToken;
        this.delegate = delegate;
    }

    @Override
    protected void onPreExecute() {
        if (request.equals(Constants.REQUEST_HEART_DATA)) {
            Log.d(TAG, "REQUEST_HEART_DATA");
            url = FitbitUtil.getSingleDayHeartDataUrl(userId,
                    FitbitUtil.getPreviousDaysDate(prevDays),
                    Constants.START_TIME, Constants.END_TIME);
        }
    }

    protected String doInBackground(Void... urls) {
        try {
            return FitbitUtil.sendGet(url, accessToken);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    protected void onPostExecute(String feed) {
        delegate.processFinish(request, feed);
    }
}
