package com.fitbitcrypt;

import org.json.JSONObject;

/**
 * Created by ajpeacock0_desktop on 3/10/2015.
 */
public interface AsyncResponse {
    void processFinish(JSONObject json);
    void processFinish(String request, String feed);
}
