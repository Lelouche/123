package ch.ethz.inf.vs.lubu.cyrptdbmodule.conn;

import ch.ethz.inf.vs.lubu.cyrptdbmodule.exceptions.CDBException;

/**
 * Created by lukas on 23.03.15.
 * Factory for thew Connection
 */
public class ConnectionManager {

    public static IDBConnection getConnection(String url, String username, String secret) throws CDBException {
        WSConnection con = new WSConnection();
        try {
            con.connect(url, username, secret);
        } catch (Exception e) {
            e.printStackTrace();
            throw new CDBException("Connection Failed" + e.getMessage());
        }

        return con;
    }

}
