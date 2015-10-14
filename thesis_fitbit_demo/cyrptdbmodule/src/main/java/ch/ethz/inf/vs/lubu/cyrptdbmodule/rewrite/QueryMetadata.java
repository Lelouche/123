package ch.ethz.inf.vs.lubu.cyrptdbmodule.rewrite;

import net.sf.jsqlparser.expression.Expression;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import ch.ethz.inf.vs.lubu.cyrptdbmodule.crypto.EncLayer;
import ch.ethz.inf.vs.lubu.cyrptdbmodule.crypto.ICryptoManager;
import ch.ethz.inf.vs.lubu.cyrptdbmodule.crypto.mOPEJob;
import ch.ethz.inf.vs.lubu.cyrptdbmodule.dbscheme.CDBColumn;
import ch.ethz.inf.vs.lubu.cyrptdbmodule.dbscheme.CDBTable;
import ch.ethz.inf.vs.lubu.cyrptdbmodule.dbscheme.DBSchemeManager;
import ch.ethz.inf.vs.lubu.cyrptdbmodule.exceptions.CDBException;
import ch.ethz.inf.vs.lubu.cyrptdbmodule.exceptions.RewriteCDBException;
import ch.ethz.inf.vs.lubu.cyrptdbmodule.util.CDBUtil;

/**
 * Created by lukas on 23.03.15.
 * Keeps track of the query metadata during
 * checking and rewriting of a query
 */
public class QueryMetadata {

    private Map<String, CDBTable> involvedTables;

    private Map<String, CDBColumn> involvedColumns;

    private CDBException checkException = null;

    private ICryptoManager crypto;

    private DBSchemeManager schemeManager;

    private Map<Expression, TypeScope> plainValuesType;

    private List<mOPEJob.mOPEWork> mopeWork = new ArrayList<>();

    //flags
    public boolean inSelect = false;
    public boolean inInsert = false;
    public boolean inAGR = false;
    public boolean isSigned = false;
    public boolean containsmOPE = false;
    public boolean inMAX = false;
    public boolean inMIN = false;

    public QueryMetadata(ICryptoManager crypto, DBSchemeManager schemeManager) {
        this.crypto = crypto;
        this.schemeManager = schemeManager;
        involvedTables = new HashMap<String, CDBTable>();
        involvedColumns = new HashMap<String, CDBColumn>();
        plainValuesType = new HashMap<Expression, TypeScope>();
    }

    public void postmOPEWork(String tag, String schemeName, CDBColumn column, String cipher) {
        mopeWork.add(new mOPEJob.mOPEWork(tag,schemeName,column,cipher));
    }

    public  List<mOPEJob.mOPEWork> getmOPEWork() {
        return mopeWork;
    }

    public boolean addCDBTable(String tab) {
        CDBTable tabScheme = schemeManager.getTable(tab);
        if (tabScheme == null)
            return false;
        if (!involvedTables.containsKey(tab))
            involvedTables.put(tab, tabScheme);
        return true;
    }

    public void setException(CDBException e) {
        this.checkException = e;
    }

    public boolean hasException() {
        return checkException != null;
    }

    public void throwException() throws CDBException {
        if (!hasException())
            return;
        throw checkException;
    }

    public CDBColumn getColumn(String key) {
        return involvedColumns.get(key);
    }

    public boolean addCDBColumn(String tableName, String colname) {
        CDBColumn res = null;
        String key;
        if (tableName == null || tableName.isEmpty()) {
            res = searchUniqueColumn(colname);
            key = colname;
        } else {
            res = schemeManager.getColumn(tableName, colname);
            key = tableName + "." + colname;
        }

        if (res != null) {
            if (!involvedColumns.containsKey(key))
                involvedColumns.put(key, res);
            return true;
        }

        return false;
    }

    public boolean addAllColumnsFromCurrentTables() {
        for (CDBTable tab : involvedTables.values()) {
            addAllColumnsFromTable(tab.getRealName());
        }
        return true;
    }

    public List<CDBColumn> addAllColumnsFromTable(String tableName) {
        ArrayList<CDBColumn> cols = new ArrayList<CDBColumn>();
        CDBTable table = involvedTables.get(tableName);
        for (Iterator<CDBColumn> iter = table.getColIter(); iter.hasNext(); ) {
            CDBColumn cur = iter.next();
            involvedColumns.put(tableName + "." + cur.getRealName(), cur);
            cols.add(cur);
        }
        return cols;
    }

    /**
     * Checks if the column belongs to a involved Table
     *
     * @param columnName
     * @return null if not found or more than one result exists
     * else the  corresponding CDBColum
     */
    private CDBColumn searchUniqueColumn(String columnName) {
        CDBColumn res = null;
        for (Map.Entry entry : involvedTables.entrySet()) {
            CDBColumn cur = ((CDBTable) entry.getValue()).getColumn(columnName);
            if (cur != null) {
                if (res == null)
                    res = cur;
                else
                    return null;
            }
        }
        return res;
    }

    public String getFullHashTabName(String nameTable) throws CDBException {
        CDBTable table = involvedTables.get(nameTable);
        if (table == null)
            throw new RewriteCDBException("Table not found in Metadata");

        StringBuilder sb = new StringBuilder();
        sb.append(CDBUtil.packDelim(schemeManager.getSchemeName()));
        sb.append(CDBUtil.DOT);
        sb.append(CDBUtil.packDelim(table.getHashName()));

        return sb.toString();
    }

    private String getHashColName(EncLayer.EncLayerType type, CDBColumn col) {
        StringBuilder sb = new StringBuilder();
        sb.append(CDBUtil.packDelim(schemeManager.getSchemeName()));
        sb.append(CDBUtil.DOT);
        sb.append(CDBUtil.packDelim(col.getBelongsTo().getHashName()));
        sb.append(CDBUtil.DOT);
        sb.append(CDBUtil.packDelim(col.getHashName(type)));
        return sb.toString();
    }

    public String getAllColNamesForStar() {
        StringBuilder sb = new StringBuilder();
        for (CDBTable table : involvedTables.values()) {
            Iterator<CDBColumn> iter = table.getColIter();
            while (iter.hasNext()) {
                CDBColumn curCol = iter.next();
                sb.append(getHashColName(curCol.getMainType(), curCol));
                if (curCol.hasIV()) {
                    sb.append(", ");
                    sb.append(CDBUtil.packDelim(schemeManager.getSchemeName()));
                    sb.append(CDBUtil.DOT);
                    sb.append(CDBUtil.packDelim(curCol.getBelongsTo().getHashName()));
                    sb.append(CDBUtil.DOT);
                    sb.append(CDBUtil.packDelim(curCol.getColIVName()));
                }
                if (iter.hasNext())
                    sb.append(", ");
            }
        }
        return sb.toString();
    }

    public String getHashColNameByType(EncLayer.EncLayerType type, String ref) throws CDBException {
        CDBColumn col = involvedColumns.get(ref);
        if (col == null) {
            throw new RewriteCDBException("Column not found in Metadata");
        } else if (col.isPlain()) {
            return getHashColName(EncLayer.EncLayerType.PLAIN, col);
        } else {
            return getHashColName(type, col);
        }
    }

    public String getHashColNameMainType(String ref) throws CDBException {
        CDBColumn col = involvedColumns.get(ref);
        if (col == null)
            throw new RewriteCDBException("Column not found in Metadata");
        return getHashColNameByType(col.getMainType(), ref);
    }

    public String getFullHashColIVName(String ref) throws CDBException {
        CDBColumn col = involvedColumns.get(ref);
        if (col == null)
            throw new RewriteCDBException("Column not found in Metadata");
        if (!col.hasIV())
            return null;

        StringBuilder sb = new StringBuilder();
        sb.append(CDBUtil.packDelim(schemeManager.getSchemeName()));
        sb.append(CDBUtil.DOT);
        sb.append(CDBUtil.packDelim(col.getBelongsTo().getHashName()));
        sb.append(CDBUtil.DOT);
        sb.append(CDBUtil.packDelim(col.getColIVName()));

        return sb.toString();
    }

    public String getAllHashCols(String ref) throws CDBException {
        CDBColumn col = involvedColumns.get(ref);
        StringBuilder sb = new StringBuilder();
        for (EncLayer.EncLayerType type : col.getTypes()) {
            sb.append(getHashColName(type, col)).append(", ");
        }
        if (col.hasIV())
            sb.append(getFullHashColIVName(ref));
        else
            sb.setLength(sb.length() - 2);
        return sb.toString();
    }

    public void addPlainTypePair(Expression e, TypeScope ts) {
        this.plainValuesType.put(e, ts);
    }

    public TypeScope getPlainType(Expression e) {
        return this.plainValuesType.get(e);
    }

    public ICryptoManager getCrypto() {
        return crypto;
    }

    public DBSchemeManager getSchemeManager() {
        return schemeManager;
    }

}
