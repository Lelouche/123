package ch.ethz.inf.vs.lubu.cyrptdbmodule.main;

import android.util.SparseArray;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

import ch.ethz.inf.vs.lubu.cyrptdbmodule.crypto.EncLayer;
import ch.ethz.inf.vs.lubu.cyrptdbmodule.crypto.ICryptoManager;
import ch.ethz.inf.vs.lubu.cyrptdbmodule.dbscheme.CDBColumn;
import ch.ethz.inf.vs.lubu.cyrptdbmodule.dbscheme.DBSchemeManager;
import ch.ethz.inf.vs.lubu.cyrptdbmodule.exceptions.CDBException;
import ch.ethz.inf.vs.lubu.cyrptdbmodule.util.CDBUtil;
import ch.ethz.inf.vs.lubu.cyrptdbmodule.util.Setting;

/**
 * Created by lukas on 20.04.15.
 * Implementation of CDBResultSet which handles Data in JSON format
 */
public class CDBResultSetJSON extends CDBResultSet {

    private ArrayList<CDBResultSetRow> rowsList = new ArrayList<>();

    public CDBResultSetJSON(ICryptoManager man, DBSchemeManager schemeMan, JSONObject data) throws CDBException {
        this.cryptoMan = man;
        this.schemeMan = schemeMan;
        this.initData(data);
        this.encLayerCache = new HashMap<>();
    }

    private boolean isFuncCol(String col) {
        return col.contains("(");
    }

    /**
     * Parses the JSON data
     * @param data JSON data
     * @throws CDBException
     */
    private void initData(JSONObject data) throws CDBException {
        try {
            JSONArray cols, tables, rows;
            cols = JSONResultScheme.getInvolvedCols(data);
            rows = JSONResultScheme.getRows(data);

            int numHashCols = cols.length();
            ArrayList<CDBColumn> dynColInRow = new ArrayList<>();
            ArrayList<EncLayer.EncLayerType> dynEnccTypeCol = new ArrayList<>();
            this.funcPrefix = new SparseArray<>();
            int funcInd = 0;

            for (int i = 0; i < numHashCols; i++) {
                String curSelCol = cols.getString(i);
                String[] split = null;
                if (isFuncCol(curSelCol)) {
                    split = CDBUtil.getFuncTableColRef(curSelCol);
                } else {
                    split = CDBUtil.getTableColRef(curSelCol);
                }
                if (Setting.CRYPTO_ON) {
                    CDBColumn curCol = schemeMan.getColumnWithHash(split[0], split[1]);
                    if (!curCol.isIV(split[1])) {
                        dynColInRow.add(curCol);
                        dynEnccTypeCol.add(curCol.getEncType(split[1]));
                        if (split.length == 3)
                            funcPrefix.append(funcInd, split[2]);
                        funcInd++;
                    }
                } else {
                    dynColInRow.add(schemeMan.getColumn(split[0], split[1]));
                    if (split.length == 3) {
                        funcPrefix.append(i, split[2]);
                    }
                }
            }

            int numCols = dynColInRow.size();
            this.colInRow = new CDBColumn[numCols];
            this.encTypeCol = new EncLayer.EncLayerType[numCols];
            for (int i = 0; i < dynColInRow.size(); i++) {
                this.colInRow[i] = dynColInRow.get(i);
                if(Setting.CRYPTO_ON)
                    this.encTypeCol[i] = dynEnccTypeCol.get(i);
                else
                    this.encTypeCol[i] = EncLayer.EncLayerType.PLAIN;
            }

            for (int i = 0; i < rows.length(); i++) {
                rowsList.add(new CDBResultSetRowJSON(rows.getJSONObject(i)));
            }
            this.rowIter = rowsList.listIterator();


        } catch (JSONException e) {
            throw new CDBException("Received wrong JSON-Message: " + e.getMessage());
        }

    }


    @Override
    public boolean first() {
        if (!rowsList.isEmpty()) {
            this.curRow = rowsList.get(0);
            this.rowIter = rowsList.listIterator(0);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean last() {
        if (!rowsList.isEmpty()) {
            this.curRow = rowsList.get(rowsList.size() - 1);
            this.rowIter = rowsList.listIterator(rowsList.size() - 1);
            return true;
        } else {
            return false;
        }
    }
}
