package ch.ethz.inf.vs.lubu.cyrptdbmodule.main;

import android.content.Context;

import java.io.Reader;

import ch.ethz.inf.vs.lubu.cyrptdbmodule.conn.ConnectionManager;
import ch.ethz.inf.vs.lubu.cyrptdbmodule.conn.IDBConnection;
import ch.ethz.inf.vs.lubu.cyrptdbmodule.crypto.CryptoFactory;
import ch.ethz.inf.vs.lubu.cyrptdbmodule.crypto.ICryptoManager;
import ch.ethz.inf.vs.lubu.cyrptdbmodule.cryptoalg.FastECElGamal;
import ch.ethz.inf.vs.lubu.cyrptdbmodule.dbscheme.DBSchemeFactory;
import ch.ethz.inf.vs.lubu.cyrptdbmodule.dbscheme.DBSchemeManager;
import ch.ethz.inf.vs.lubu.cyrptdbmodule.exceptions.CDBException;
import ch.ethz.inf.vs.lubu.cyrptdbmodule.util.ContextHolder;
import ch.ethz.inf.vs.lubu.cyrptdbmodule.util.Logger;


/**
 * Created by lukas on 04.03.15.
 * This class is the main API to create a Connection to the
 * CryptoAppDB.
 */
public class CryptDBConnector {

    private DBSchemeManager dbSchemeManager;

    private ICryptoManager cryptoManager;

    private IDBConnection con;

    /**
     * Create a Connection to the CryptoAppDB
     * @param scheme The Scheme of the DB in JSONFormat
     * @param appContext The application context of the android application
     * @param url the server URL of the CryptoAppDB WebServer
     *            Example: http://localhost:8081
     * @param username only pseudo login name for the WebServer (may be removed)
     * @param pwd only pseudo login password for the WebServer (may be removed)
     * @throws CDBException
     */
    public CryptDBConnector(Reader scheme,
                            Context appContext,
                            String url,
                            String username,
                            String pwd) throws CDBException {
        ContextHolder.setContext(appContext);
        this.con = ConnectionManager.getConnection(url, username, pwd);
        this.dbSchemeManager = DBSchemeFactory.getDBSchemeManager(scheme);
        this.cryptoManager = CryptoFactory.getCryptoManager();

    }

    /**
     * Create a CryptoAppDB statement on which query can be
     * executed
     * @return CryptoAppDB statement
     */
    public CDBStatement createStatement() {
        return new CDBStatement(dbSchemeManager, cryptoManager, con);
    }

    /**
     * Clean NativeMemory !!!!
     * @throws Throwable
     */
    @Override
    protected void finalize() throws Throwable {
        FastECElGamal.TableLoader.freeTables();
        FastECElGamal.tearDOWN();
        Logger.log("Cleanded");
        super.finalize();
    }
}
