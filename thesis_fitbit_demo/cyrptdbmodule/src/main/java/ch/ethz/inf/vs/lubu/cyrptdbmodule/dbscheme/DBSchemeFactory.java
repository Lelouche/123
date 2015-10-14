package ch.ethz.inf.vs.lubu.cyrptdbmodule.dbscheme;

import java.io.Reader;

import ch.ethz.inf.vs.lubu.cyrptdbmodule.exceptions.CDBException;

/**
 * Created by lukas on 20.04.15.
 * Factory for getting a DBSchemeManager
 */
public class DBSchemeFactory {

    public static DBSchemeManager getDBSchemeManager(Reader r) throws CDBException {
        return new DBSchemeJSONManager(r);
    }

}
