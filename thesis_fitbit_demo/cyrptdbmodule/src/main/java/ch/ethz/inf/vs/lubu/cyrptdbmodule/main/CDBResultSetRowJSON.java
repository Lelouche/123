package ch.ethz.inf.vs.lubu.cyrptdbmodule.main;

import org.json.JSONObject;

import java.util.Iterator;

/**
 * Created by lukas on 20.04.15.
 * Implements a Row in the CDBResultSet
 */
public class CDBResultSetRowJSON implements CDBResultSetRow {

    private JSONObject row;

    public CDBResultSetRowJSON(JSONObject row) {
        this.row = row;
    }

    @Override
    public String getValue(String label) throws Exception {
        return row.getString(label);
    }

}
