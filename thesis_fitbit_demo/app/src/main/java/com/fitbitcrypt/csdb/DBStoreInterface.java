package com.fitbitcrypt.csdb;

import com.fitbitcrypt.db_util.DbUtil;
import com.fitbitcrypt.db_util.HeartRateTuple;

import ch.ethz.inf.vs.lubu.cyrptdbmodule.exceptions.CDBException;
import ch.ethz.inf.vs.lubu.cyrptdbmodule.main.CDBResultSet;
import ch.ethz.inf.vs.lubu.cyrptdbmodule.main.CDBStatement;
import ch.ethz.inf.vs.lubu.cyrptdbmodule.main.CryptDBConnector;

/**
 * Created by ajpeacock0_desktop on 5/09/2015.
 */
public class DBStoreInterface {

    private CryptDBConnector connection;

    public DBStoreInterface(CryptDBConnector connection) {
        this.connection = connection;
    }

    public CDBResultSet executeSelectQuery(String query) throws CDBException{
        CDBStatement stmt = connection.createStatement();
        return stmt.executeQuery(query);
    }

    public boolean storeHeartRate(HeartRateTuple heart) throws CDBException{
        CDBStatement stmt = connection.createStatement();
        return stmt.executeInsert(DbUtil.getHeartRateInsertQuery(heart));
    }

    private static String str(String in) {
        return "'"+in+"'";
    }

}
