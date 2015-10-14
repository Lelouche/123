package ch.ethz.inf.vs.lubu.cyrptdbmodule.main;

import ch.ethz.inf.vs.lubu.cyrptdbmodule.conn.ICDBReply;
import ch.ethz.inf.vs.lubu.cyrptdbmodule.conn.IDBConnection;
import ch.ethz.inf.vs.lubu.cyrptdbmodule.crypto.ICryptoManager;
import ch.ethz.inf.vs.lubu.cyrptdbmodule.dbscheme.DBSchemeManager;
import ch.ethz.inf.vs.lubu.cyrptdbmodule.exceptions.CDBException;
import ch.ethz.inf.vs.lubu.cyrptdbmodule.rewrite.IRewriteResult;
import ch.ethz.inf.vs.lubu.cyrptdbmodule.rewrite.QueryRewriter;
import ch.ethz.inf.vs.lubu.cyrptdbmodule.util.Logger;
import ch.ethz.inf.vs.lubu.cyrptdbmodule.util.Setting;

/**
 * Created by lukas on 23.03.15.
 * A CryptoAppDB Statement the main API to execute querys on
 * the encrypted database
 */
public class CDBStatement {

    private DBSchemeManager scheme;

    private ICryptoManager cryptoManager;

    private IDBConnection con;

    public CDBStatement(DBSchemeManager schemeManager, ICryptoManager cryptoManager, IDBConnection con) {
        this.scheme = schemeManager;
        this.cryptoManager = cryptoManager;
        this.con = con;
    }

    /**
     * Execute a select query on the CryptoAppDB
     * @param query A valid SQL SELECT query
     * @return the result of the query as a CDBResultSet
     * @throws CDBException
     */
    public CDBResultSet executeQuery(String query) throws CDBException {
        IRewriteResult rewQuery = null;
        Logger.log("-----executeQuery------");
        Logger.log("QueryBefore: " + query);
        if (Setting.CRYPTO_ON) {
            QueryRewriter rew = new QueryRewriter();
            rewQuery = rew.rewriteQuery(cryptoManager, scheme, query);
            Logger.log("QueryAfter: " + rewQuery.getRewrite());
        }

        ICDBReply reply = null;
        if(!Setting.CRYPTO_ON) {
            reply = con.executeQuery(query);
        } else if(rewQuery.hasmOPE()) {
            Logger.log("Start mOPE Query");
            reply = con.executemOPEStmt(rewQuery.getmOPEJob(), cryptoManager.getKeyManager());
        } else {
            reply = con.executeQuery(rewQuery.getRewrite());
        }

        if (reply!=null && reply.hasError()) {
            throw new CDBException("QueryFailed " + reply.getErrorMessage());
        }

        CDBResultSetJSON resultSet;

        resultSet = new CDBResultSetJSON(cryptoManager, scheme, reply.getDataMessage());
        Logger.log("-----------------------");

        return resultSet;
    }

    /**
     * Execute a insert query on the CryptoAppDB
     * @param insert A valid SQL INSERT query
     * @return true if it succeeded or false if an error occurred
     * @throws CDBException
     */
    public boolean executeInsert(String insert) throws CDBException {
        Logger.log("-----executeInsert-----");
        Logger.log("InsertBefore: " + insert);

        IRewriteResult rewInsert = null;
        if (Setting.CRYPTO_ON) {
            QueryRewriter rew = new QueryRewriter();
            rewInsert = rew.rewriteInsert(cryptoManager, scheme, insert);
            Logger.log("InsertAfter: " + rewInsert.getRewrite());
        }

        ICDBReply reply = null;
        if(!Setting.CRYPTO_ON) {
            reply = con.executeUpdate(insert);
        } else if(rewInsert.hasmOPE()) {
            Logger.log("Start mOPE Insert");
            reply = con.executemOPEStmt(rewInsert.getmOPEJob(), cryptoManager.getKeyManager());
        } else {
            reply = con.executeUpdate(rewInsert.getRewrite());
        }

        if (reply!=null && reply.hasError()) {
            throw new CDBException("InsertFailed " + reply.getErrorMessage());
        }

        Logger.log("-----------------------");
        return true;
    }

}
