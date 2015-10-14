package ch.ethz.inf.vs.lubu.cyrptdbmodule.main;

/**
 * Created by lukas on 20.04.15.
 * Represents a row in a CDBResultSet
 */
interface CDBResultSetRow {

    public String getValue(String label) throws Exception;

}
