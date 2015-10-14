package ch.ethz.inf.vs.lubu.cyrptdbmodule.dbscheme;

import com.google.common.io.CharStreams;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import ch.ethz.inf.vs.lubu.cyrptdbmodule.crypto.EncLayer;
import ch.ethz.inf.vs.lubu.cyrptdbmodule.exceptions.SchemeCDBException;
import ch.ethz.inf.vs.lubu.cyrptdbmodule.util.CDBUtil;
import ch.ethz.inf.vs.lubu.cyrptdbmodule.util.DBType;

/**
 * Created by lukas on 25.03.15.
 * Implements a SchemeManager
 */
public class DBSchemeJSONManager extends DBSchemeManager {

    private static final String LOG_TAG = "JSONMan";

    public DBSchemeJSONManager(Reader f) throws SchemeCDBException {
        super(f);
    }

    @Override
    protected void parseFile(Reader f) throws SchemeCDBException {
        tables = new ArrayList<CDBTable>();
        HashMap<CDBColumn, JSONArray> joinableMap = new HashMap<CDBColumn, JSONArray>();
        String jsonString;
        try {
            jsonString = CharStreams.toString(f);

            JSONObject scheme = JSONScheme.getJSON(jsonString);

            this.schemeName = JSONScheme.getSchemeName(scheme);

            JSONArray tables = JSONScheme.getTables(scheme);

            for (int tabID = 0; tabID < tables.length(); tabID++) {
                JSONObject table = tables.getJSONObject(tabID);

                String name, hashname;
                name = JSONScheme.getTableName(table);
                hashname = JSONScheme.getTableHash(table);
                CDBTable cdbTable = new CDBTable(name, hashname);

                JSONArray columns = JSONScheme.getColumns(table);
                for (int colID = 0; colID < columns.length(); colID++) {
                    JSONObject column = columns.getJSONObject(colID);
                    String colName, colIV = null;
                    DBType type;
                    colName = JSONScheme.getColumnName(column);
                    if (JSONScheme.hasIV(column))
                        colIV = JSONScheme.getColumnIVName(column);
                    type = DBType.getDBType(JSONScheme.getColumnType(column));

                    JSONArray hasJoin = null;
                    HashMap<EncLayer.EncLayerType, String> subcolMap = new HashMap<EncLayer.EncLayerType, String>();
                    JSONArray subCols = JSONScheme.getColumnSubColumns(column);

                    for (int subcolID = 0; subcolID < subCols.length(); subcolID++) {
                        JSONObject subCol = subCols.getJSONObject(subcolID);
                        EncLayer.EncLayerType encType = EncLayer.EncLayerType.getEncType(JSONScheme.getColumnEncLayer(subCol));
                        String hashName = JSONScheme.getColumnHash(subCol);
                        if (encType.equals(EncLayer.EncLayerType.DET_JOIN))
                            hasJoin = JSONScheme.getColumnJoinable(subCol);

                        if (hashName == null)
                            throw new SchemeCDBException("Parse Error in SubColumn");
                        subcolMap.put(encType, hashName);
                    }


                    if (type == null)
                        throw new SchemeCDBException("Parse Error");

                    CDBColumn col = new CDBColumn(colName, colIV, type, cdbTable, subcolMap);

                    if (hasJoin != null)
                        joinableMap.put(col, hasJoin);

                    cdbTable.addColumn(col);
                }
                this.tables.add(cdbTable);
            }

            Set<CDBColumn> processed = joinableMap.keySet();
            HashSet<CDBColumn> done = new HashSet<CDBColumn>();
            for (CDBColumn joinCol : processed) {
                JSONArray joinables = joinableMap.get(joinCol);
                for (int joinID = 0; joinID < joinables.length(); joinID++) {
                    String[] ref = CDBUtil.getTableColRef(joinables.getString(joinID));
                    CDBColumn cur = this.getColumn(ref[0], ref[1]);
                    if (!done.contains(cur)) {
                        joinCol.addJoinableCol(cur);
                        cur.addJoinableCol(joinCol);
                    }
                }
                done.add(joinCol);
            }

        } catch (Exception e) {
            throw new SchemeCDBException("Error parsing JSONSchemeFile");
        } finally {
            if (f != null) {
                try {
                    f.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
