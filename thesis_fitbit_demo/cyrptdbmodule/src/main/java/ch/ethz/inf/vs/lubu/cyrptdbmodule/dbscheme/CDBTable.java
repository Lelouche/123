package ch.ethz.inf.vs.lubu.cyrptdbmodule.dbscheme;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by lukas on 04.03.15.
 * Represents a Table in the DBScheme
 */
public class CDBTable {

    private String realName;

    private String hashName;

    private List<CDBColumn> columns;

    public CDBTable() {
        columns = new ArrayList<CDBColumn>();
    }

    public CDBTable(String realName, String hashName) {
        columns = new ArrayList<CDBColumn>();
        this.realName = realName;
        this.hashName = hashName;
    }

    public void addColumn(CDBColumn col) {
        columns.add(col);
    }

    public CDBColumn getColumn(String colName) {
        for (CDBColumn col : columns)
            if (col.getRealName().equals(colName))
                return col;
        return null;
    }

    public CDBColumn getColumnWithHash(String colHash) {
        for (CDBColumn col : columns) {
            for (String hash : col.getHashNames()) {
                if (hash.equals(colHash))
                    return col;
            }
            if (col.isIV(colHash))
                return col;
        }

        return null;
    }

    public String getRealName() {
        return realName;
    }

    public void setRealName(String realName) {
        this.realName = realName;
    }

    public String getHashName() {
        return hashName;
    }

    public void setHashName(String hashName) {
        this.hashName = hashName;
    }

    public boolean contains(CDBColumn col) {
        return columns.contains(col);
    }

    public Iterator<CDBColumn> getColIter() {
        return columns.iterator();
    }

}
