package com.fitbitcrypt.db_util;

import com.fitbitcrypt.Constants;

/**
 * Created by ajpeacock0_desktop on 29/09/2015.
 */
public class HeartRateTuple implements Tuple {
    private String clientId,date,time;
    private int level,value;

    public HeartRateTuple(String clientId, String date, String time, int level, int value) {
        this.clientId = clientId;
        this.date = date;
        this.time = time;
        this.level = level;
        this.value = value;
    }

    public String getClientId() {
        return clientId;
    }

    public String getDate() {
        return date;
    }

    public String getTime() {
        return time;
    }

    public int getLevel() {
        return level;
    }

    public int getValue() {
        return value;
    }

    @Override
    public String getType() {
        return Constants.REQUEST_HEART_DATA;
    }
}
