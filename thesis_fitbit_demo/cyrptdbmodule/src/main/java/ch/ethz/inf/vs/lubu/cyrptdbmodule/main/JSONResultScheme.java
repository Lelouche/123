package ch.ethz.inf.vs.lubu.cyrptdbmodule.main;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by lukas on 20.04.15.
 * Represents the JSONScheme of the incoming Reply from the WebServer
 */
public class JSONResultScheme {

    public static final String TABLES_KEY = "tables";

    public static final String RESULT_BOOL_KEY = "result";

    public static final String ROWS_KEY = "data";

    public static final String COLS_KEY = "columns";

    public static final String ERROR_MSG_KEY = "msg";

    public static final String HAS_ERROR = "Error";

    public static final String SUCCESS = "success";

    public static final String NUM_ROWS_KEY = "rows";

    public static JSONArray getRows(JSONObject answer) throws JSONException {
        return answer.getJSONArray(ROWS_KEY);
    }

    public static JSONArray getInvolvedCols(JSONObject answer) throws JSONException {
        return answer.getJSONArray(COLS_KEY);
    }

    public static int getNumberOfRows(JSONObject answer) throws JSONException {
        return answer.getInt(NUM_ROWS_KEY);
    }

    public static String getResultStatus(JSONObject answer) throws JSONException {
        return answer.getString(RESULT_BOOL_KEY);
    }

    public static String getErrorMsg(JSONObject answer) throws JSONException {
        return answer.getString(ERROR_MSG_KEY);
    }

    public static JSONArray getInvolvedTables(JSONObject answer) throws JSONException {
        return answer.getJSONArray(TABLES_KEY);
    }
}
