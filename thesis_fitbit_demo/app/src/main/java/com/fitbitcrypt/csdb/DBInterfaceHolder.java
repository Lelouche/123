package com.fitbitcrypt.csdb;

import android.content.Context;
import java.io.Reader;

import ch.ethz.inf.vs.lubu.cyrptdbmodule.exceptions.CDBException;
import ch.ethz.inf.vs.lubu.cyrptdbmodule.main.CryptDBConnector;

import com.fitbitcrypt.Constants;
import com.fitbitcrypt.csdb.DBStoreInterface;

/**
 * Created by ajpeacock0_desktop on 5/09/2015.
 */
public class DBInterfaceHolder {
    private static DBStoreInterface store = null;
    // TODO: Get the port from csdb_ssh_tunnel.properties
    private static final String port = "8083",
            SERVER_URL = "http://"+ Constants.LOCALHOST+":"+port, UNAME = "root", PWD = "letmein";

    public static void init(Reader in, Context cont) throws CDBException {
        if(store==null) {
            CryptDBConnector con = new CryptDBConnector(in, cont, SERVER_URL, UNAME, PWD);
            store = new DBStoreInterface(con);
        }
    }

    public static DBStoreInterface getCon() {
        return store;
    }
}
