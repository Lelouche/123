package ch.ethz.inf.vs.lubu.cyrptdbmodule.conn;

import ch.ethz.inf.vs.lubu.cyrptdbmodule.crypto.IKeyManager;
import ch.ethz.inf.vs.lubu.cyrptdbmodule.crypto.mOPEJob;
import ch.ethz.inf.vs.lubu.cyrptdbmodule.exceptions.CDBException;

/**
 * Created by lukas on 04.03.15.
 * Defines the API for performing queries on the server
 */
public interface IDBConnection {

    public void connect(String url, String user, String secret) throws Exception;

    public ICDBReply executeQuery(String query) throws CDBException;

    public ICDBReply executemOPEStmt(mOPEJob job, IKeyManager keyManager) throws CDBException;

    public ICDBReply executeUpdate(String query) throws CDBException;

}
