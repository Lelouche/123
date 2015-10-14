package ch.ethz.inf.vs.lubu.cyrptdbmodule.crypto;

import ch.ethz.inf.vs.lubu.cyrptdbmodule.dbscheme.CDBColumn;
import ch.ethz.inf.vs.lubu.cyrptdbmodule.exceptions.CDBException;

/**
 * Created by lukas on 04.03.15.
 */
public interface ICryptoManager {

    public EncLayer getEncLayer(EncLayer.EncLayerType type, CDBColumn col, int sizeByte) throws CDBException;

    public EncLayer getEncLayer(EncLayer.EncLayerType type, CDBColumn col) throws CDBException;

    public String getEnclayerTag(EncLayer.EncLayerType type, int size);

    public IKeyManager getKeyManager();
}
