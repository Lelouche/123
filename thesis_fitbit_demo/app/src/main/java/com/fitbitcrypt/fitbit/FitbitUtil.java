package com.fitbitcrypt.fitbit;

import android.util.Log;

import com.fitbitcrypt.Constants;

import org.json.JSONException;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by ajpeacock0_desktop on 30/09/2015.
 */
public class FitbitUtil {

    /**
     * Generates a string to display information about the received data
     * such as number of fields, size in Bytes, example format
     * @param request
     * @param feed
     * @return
     */
    public static String getMetaData(String request, String feed) {
        if (request.equals(Constants.REQUEST_HEART_DATA)) {
            FitbitHeartJsonParsed data = null;
            try {
                data = new FitbitHeartJsonParsed(feed);
            } catch (JSONException e) {
                e.printStackTrace();
                return null;
            }
            int size = data.getDataset().length();
            // TODO: calculate the size in bytes of the data to send
            int bytes = 0;
            // TODO: get the first field from the data
            String example = "\"time\": \"00:00:45\", \"value\": 65";
            return generateMetaDataString(size, bytes, example);
        }

        return null;
    }

    public static String sendGet(String url, String accessToken) throws Exception {
        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();

        // optional default is GET
        con.setRequestMethod("GET");
        con.addRequestProperty("Authorization", getAuthorizationHeader(accessToken));

        Log.d("Send URL request:", url);
        int responseCode = con.getResponseCode();
        Log.d("Response Code :", Integer.toString(responseCode));

        BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        return response.toString();
    }

    public static String getSingleDayHeartDataUrl(String userId, String date, String startTime, String endTime) {
        return Constants.FITBIT_USER_QUERY_ENDPOINT+userId+"/activities/heart/date/"+date+"/1d/1sec/time/"+startTime+"/"+endTime+".json";
    }

    public static String getAuthorizationHeader(String accessToken) {
        return "Bearer "+accessToken;
    }

    public static String generateMetaDataString(int size, int bytes, String example) {
        return "Size: "+Integer.toString(size)+" Bytes: "+Integer.toString(bytes)+" Format: "+example;
    }

    public static String getAuthorizeUrl(String response_type, String client_id, String redirect_uri, String scope, String expires_in) {
        return Constants.FITBIT_AUTH_ENDPOINT +
                "response_type="+response_type+"&"+
                "client_id="+client_id+"&"+
                "redirect_uri="+redirect_uri+"&"+
                "scope="+scope+"&"+
                "expires_in="+expires_in;
    }

    public static String getCurrentDate() {
        Calendar c = Calendar.getInstance();
        SimpleDateFormat df = getDateFormat();
        return df.format(c.getTime());
    }

    public static String getPreviousDaysDate(int days) {
        Calendar c = Calendar.getInstance();
        c.add(Calendar.DAY_OF_MONTH, -days);
        SimpleDateFormat df = getDateFormat();
        return df.format(c.getTime());
    }

    private static SimpleDateFormat getDateFormat() {
        return new SimpleDateFormat("yyyy-MM-dd");
    }

    public static Map<String, String> getQueryMap(String url) {
        Map<String, String> map = new HashMap<String, String>();

        if (!url.contains("=")) return map;

        String[] params = url.split("&");
        for (String param : params) {
            String name = param.split("=")[0];
            String value = param.split("=")[1];
            map.put(name, value);
        }
        return map;
    }
}
