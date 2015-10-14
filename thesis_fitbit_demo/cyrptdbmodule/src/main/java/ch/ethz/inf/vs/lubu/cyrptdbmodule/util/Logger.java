package ch.ethz.inf.vs.lubu.cyrptdbmodule.util;

import android.util.Log;

/**
 * Created by lukas on 24.03.15.
 * Default class for the module for logging in the LOG
 */
public class Logger {

    /**
     * Default TAG
     */
    public static final String DEFAULT_TAG = "CDB";

    public static void log(String TAG, String msg) {
        if (Setting.LOGGING_ON)
            Log.i(TAG, msg);
    }

    public static void log(String msg) {
        if (Setting.LOGGING_ON)
            Log.i(DEFAULT_TAG, msg);
    }

    public static void logDEBUG(String msg) {
        if (Setting.LOGGING_ON)
            Log.d(DEFAULT_TAG, msg);
    }

}
