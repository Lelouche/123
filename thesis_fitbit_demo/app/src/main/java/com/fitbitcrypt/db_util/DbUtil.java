package com.fitbitcrypt.db_util;

import com.fitbitcrypt.Constants;
import com.fitbitcrypt.fitbit.FitbitHeartJsonParsed;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ajpeacock0_desktop on 3/10/2015.
 */
public class DbUtil {
    public static String getHeartRateInsertQuery(HeartRateTuple heart) {
        StringBuilder sb = new StringBuilder();
        sb.append("INSERT INTO heart_rate VALUES ('")
                .append(heart.getClientId())
                .append("', '")
                .append(heart.getDate())
                .append("', '")
                .append(heart.getTime())
                .append("', ")
                .append(Integer.toString(heart.getLevel()))
                .append(", ")
                .append(Integer.toString(heart.getValue()))
                .append(");");
        return sb.toString();
    }

    public static String getHeartRateSelectQuery() {
        return "SELECT * FROM heart_rate;";
    }

    /**
     * Creates a list of HeartRateTuples from the given FitbitHeartJsonParsed
     * @param data
     * @return
     * @throws JSONException
     */
    public static List<HeartRateTuple> getHeartRateTuples(FitbitHeartJsonParsed data) throws JSONException{
        List<HeartRateTuple> returnList = new ArrayList<HeartRateTuple>();

        for (int i = 0; i < data.getDataset().length(); ++i) {
            JSONObject object = data.getDataset().getJSONObject(i);

            String clientId = "3F6Y59";
            String date = data.getDateTime();
            String time = object.getString(Constants.TAG_DATASET_TIME);
            int level = data.getDatasetInterval();
            int value = object.getInt(Constants.TAG_DATASET_VALUE);

            returnList.add(new HeartRateTuple(clientId, date, time, level, value));
        }

        return returnList;
    }
}
