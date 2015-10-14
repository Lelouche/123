package ch.ethz.inf.vs.lubu.cyrptdbmodule.util;

import android.content.Context;

/**
 * Created by lukas on 16.04.15.
 * Holds the Application Context during
 * operation.
 */
public class ContextHolder {

    private static Context context;

    public static Context getContext() {
        return context;
    }

    public static void setContext(Context context) {
        ContextHolder.context = context;
    }

}
