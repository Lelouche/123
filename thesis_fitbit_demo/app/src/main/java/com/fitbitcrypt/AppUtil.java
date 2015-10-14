package com.fitbitcrypt;

import android.content.res.AssetManager;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Created by ajpeacock0_desktop on 26/09/2015.
 */
public class AppUtil {
    /**
     * Opens the given properties file
     * @param FileName
     * @param assetManager
     * @return The properties file
     */
    public static Properties getProperties(String FileName, AssetManager assetManager) {
        Properties properties = new Properties();
        try {
            InputStream inputStream = assetManager.open(FileName);
            properties.load(inputStream);
        } catch (IOException e) {
            Log.e("getProperties", "Error reading properties file " + e.toString());
        }
        return properties;
    }
}
