package com.fitbitcrypt;

/**
 * Created by ajpeacock0_desktop on 28/08/2015.
 */
public class Constants {
    // MainActivity/FitbitActivity - result responses
    public static final int GET_AUTH_REQUEST = 1;
    public static final int RESULT_OK = 3;
    public static final int RESULT_CANCELED = 4;
    // MainActivity/FitbitActivity - request tag
    public static final String TAG_REQUEST_CODE = "requestCode";
    // MainActivity/FitbitDataInterface - requests
    public static final String REQUEST_HEART_DATA = "heart_data";
    public static final String REQUEST_SLEEP_DATA = "sleep_data";
    public static final String TAG_ACTIVITIES_HEART = "activities-heart";
    public static final String TAG_ACTIVITIES_DATE_TIME = "dateTime";
    public static final String TAG_INTRADAY = "activities-heart-intraday";
    public static final String TAG_DATASET = "dataset";
    public static final String TAG_DATASET_INTERVAL = "datasetInterval";
    public static final String TAG_DATASET_TYPE = "datasetType";
    public static final String TAG_DATASET_TIME = "time";
    public static final String TAG_DATASET_VALUE = "value";
    // MainActivity - PHP scripts URL to send SQL data
    public static final String BASE_ENDPOINT = "http://127.0.0.1:8080/fitbit_benchmark/PHP/";
    public static final String RUN_SELECT_QUERY_ENDPOINT = BASE_ENDPOINT+"RUN_SELECT_QUERY.php";
    // FitBitActivity - URL ENDPOINTS
    public static String FITBIT_AUTH_ENDPOINT = "https://www.fitbit.com/oauth2/authorize?";
    public static String FITBIT_USER_QUERY_ENDPOINT = "https://api.fitbit.com/1/user/";
    // value can also be "localhost", but it is rumored "127.0.0.1" is faster
    // since a lookup of localhost => 127.0.0.1 is avoided
    public static final String LOCALHOST = "127.0.0.1";
    public static final String CONFIG = "config";
    public static final String QUERY = "query";
    public static final String UNENCRYPTED_CONFIG = "UnCryptDB";
    public static final String ENCRYPTED_CONFIG = "SSDB";

    public static final String SELECT_QUERY = "select";
    public static final String INSERT_QUERY = "insert";
    // FitbitDataAsyncTask
    public static final String START_TIME = "00:00";
    public static final String END_TIME = "23:59";


}
