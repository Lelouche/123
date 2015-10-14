package ch.ethz.inf.vs.lubu.cyrptdbmodule.dbscheme;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by lukas on 25.03.15.
 * Represents the JSON Scheme of the DBScheme description file
 */
public class JSONScheme {

    public static final String TAG_SCHEME_NAME = "SchemeName";

    public static final String TAG_TABLES = "Tables";

    public static final String TAG_TABLE_NAME = "TableName";

    public static final String TAG_TABLE_HASHNAME = "TabHashName";

    public static final String TAG_COLUMNS = "Columns";

    public static final String TAG_COLUMN_NAME = "ColName";

    public static final String TAG_COLUMN_HASHNAME = "ColHashName";

    public static final String TAG_COLUMN_IVNAME = "ColIVName";

    public static final String TAG_COLUMN_TYPE = "Type";

    public static final String TAG_COLUMN_SUBCOLS = "SubCols";

    public static final String TAG_COLUMN_ENCLAYER = "EncLayer";

    public static final String TAG_COLUMN_JOINABLE = "Joinable";

    public static JSONObject getJSON(String in) throws JSONException {
        return new JSONObject(in);
    }

    public static JSONArray getTables(JSONObject scheme) throws JSONException {
        return scheme.getJSONArray(TAG_TABLES);
    }

    public static String getSchemeName(JSONObject scheme) throws JSONException {
        return scheme.getString(TAG_SCHEME_NAME);
    }

    public static JSONArray getColumns(JSONObject table) throws JSONException {
        return table.getJSONArray(TAG_COLUMNS);
    }

    public static String getTableName(JSONObject table) throws JSONException {
        return table.getString(TAG_TABLE_NAME);
    }

    public static String getTableHash(JSONObject table) throws JSONException {
        return table.getString(TAG_TABLE_HASHNAME);
    }

    public static String getColumnName(JSONObject column) throws JSONException {
        return column.getString(TAG_COLUMN_NAME);
    }

    public static String getColumnHash(JSONObject column) throws JSONException {
        return column.getString(TAG_COLUMN_HASHNAME);
    }

    public static String getColumnIVName(JSONObject column) throws JSONException {
        return column.getString(TAG_COLUMN_IVNAME);
    }

    public static String getColumnType(JSONObject column) throws JSONException {
        return column.getString(TAG_COLUMN_TYPE);
    }

    public static JSONArray getColumnSubColumns(JSONObject column) throws JSONException {
        return column.getJSONArray(TAG_COLUMN_SUBCOLS);
    }

    public static String getColumnEncLayer(JSONObject column) throws JSONException {
        return column.getString(TAG_COLUMN_ENCLAYER);
    }

    public static JSONArray getColumnJoinable(JSONObject column) throws JSONException {
        return column.getJSONArray(TAG_COLUMN_JOINABLE);
    }

    public static boolean hasIV(JSONObject column) throws JSONException {
        return column.has(TAG_COLUMN_IVNAME);
    }


}
