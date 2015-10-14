package ch.ethz.inf.vs.lubu.cyrptdbmodule.dbscheme;

import java.io.Reader;
import java.util.List;

import ch.ethz.inf.vs.lubu.cyrptdbmodule.exceptions.SchemeCDBException;

/**
 * Created by lukas on 04.03.15.
 * Manages the Scheme of the Database
 */
public abstract class DBSchemeManager {

    protected String schemeName;

    protected List<CDBTable> tables;

    public DBSchemeManager(Reader scheme) throws SchemeCDBException {
        parseFile(scheme);
    }

    protected abstract void parseFile(Reader f) throws SchemeCDBException;

    public CDBColumn getColumn(String tableName, String colName) {
        CDBTable table = getTable(tableName);
        if (table == null)
            return null;
        return table.getColumn(colName);
    }

    public CDBTable getTable(String tableName) {
        for (CDBTable tableIt : tables) {
            if (tableIt.getRealName().toLowerCase().equals(tableName.toLowerCase()))
                return tableIt;
        }
        return null;
    }

    public CDBTable getTableWithHash(String hashName) {
        for (CDBTable tableIt : tables) {
            if (tableIt.getHashName().equals(hashName))
                return tableIt;
        }
        return null;
    }

    public CDBColumn getColumnWithHash(String tableHash, String colHash) {
        CDBTable table = getTableWithHash(tableHash);
        if (table == null)
            return null;
        return table.getColumnWithHash(colHash);
    }

    public boolean containsTable(String tableName) {
        return (getTable(tableName) != null);
    }

    public boolean containsColumn(String tableName, String columName) {
        return (getColumn(tableName, columName) != null);
    }

    public String getSchemeName() {
        return schemeName;
    }
}
