package ch.ethz.inf.vs.lubu.cyrptdbmodule.crypto;

import ch.ethz.inf.vs.lubu.cyrptdbmodule.dbscheme.CDBColumn;
import ch.ethz.inf.vs.lubu.cyrptdbmodule.exceptions.CDBException;

/**
 * Created by lukas on 04.03.15.
 */
public interface IKeyManager {

    public CDBKey getKey(EncLayer.EncLayerType type, CDBColumn col) throws CDBException;
}
