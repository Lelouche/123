package ch.ethz.inf.vs.lubu.cyrptdbmodule.crypto;

import java.util.HashMap;
import java.util.Map;

import ch.ethz.inf.vs.lubu.cyrptdbmodule.cryptoalg.BasicCrypto;
import ch.ethz.inf.vs.lubu.cyrptdbmodule.dbscheme.CDBColumn;
import ch.ethz.inf.vs.lubu.cyrptdbmodule.exceptions.CDBException;
import ch.ethz.inf.vs.lubu.cyrptdbmodule.util.DBType;
import ch.ethz.inf.vs.lubu.cyrptdbmodule.util.Setting;

/**
 * Created by lukas on 30.03.15.
 * Manages the different Encryption Layers
 */
public class CryptoManager implements ICryptoManager {

    private IKeyManager keyMan;

    /**
     * Caches the expensive encLayers
     */
    private static Map<String, EncLayer> cache = new HashMap<>();

    public CryptoManager(IKeyManager keyMan) {
        this.keyMan = keyMan;
    }

    @Override
    public EncLayer getEncLayer(EncLayer.EncLayerType type, CDBColumn col, int sizeByte) throws CDBException {
        DBType dbtype = col.getType();
        EncLayer.EncLayerType encType = type;
        if (!col.supportsType(type) && type.isDeterministic())
            encType = col.getMainType();

        CDBKey key = null;
        if(type.isOPE() && Setting.USE_mOPE)
            key = keyMan.getKey(col.getMainType(), col);
        else
            key = keyMan.getKey(encType, col);

        switch (type) {
            case HOM:
                if (Setting.USE_PAILLIER) {
                    String colHash = col.getHashName(encType);
                    if (cache.containsKey(colHash)) {
                        return cache.get(colHash);
                    } else {
                        HOMPaillier pail = new HOMPaillier(key);
                        cache.put(colHash, pail);
                        return pail;
                    }
                } else {
                    return new HOMECElGamal(key);
                }
            case DET:
                if (dbtype.isInteger()) {
                    return new DETBf(key);
                } else if (sizeByte <= BasicCrypto.AES_BLOCK_BYTES) {
                    return new DETAes(key);
                } else {
                    return new DETAesCMC(key);
                }
            case DET_JOIN:
                if (dbtype.isInteger()) {
                    return new DETBf(key);
                } else if (sizeByte <= BasicCrypto.AES_BLOCK_BYTES) {
                    return new DETAes(key);
                } else {
                    return new DETAesCMC(key);
                }
            case RND:
                if (dbtype.isInteger()) {
                    return new RNDBf(key);
                } else {
                    return new RNDAes(key);
                }
            case OPE:
                if(Setting.USE_mOPE)
                    return new mOPEClient(key);
                else
                    return new OPEHyp(key);
            case PLAIN:
                return new PLAINLayer();
            default:
                throw new CDBException("Not Supportet EncLayer");
        }

    }

    @Override
    public EncLayer getEncLayer(EncLayer.EncLayerType type, CDBColumn col) throws CDBException {
        DBType dbtype = col.getType();
        EncLayer.EncLayerType encType = type;
        if (!col.supportsType(type) && type.isDeterministic())
            encType = col.getMainType();

        CDBKey key = null;
        if(type.isOPE() && Setting.USE_mOPE)
            key = keyMan.getKey(EncLayer.EncLayerType.DET, col);
        else
            key = keyMan.getKey(encType, col);

        switch (type) {
            case HOM:
                if (Setting.USE_PAILLIER) {
                    String colHash = col.getHashName(encType);
                    if (cache.containsKey(colHash)) {
                        return cache.get(colHash);
                    } else {
                        HOMPaillier pail = new HOMPaillier(key);
                        cache.put(colHash, pail);
                        return pail;
                    }
                } else {
                    return new HOMECElGamal(key);
                }
            case DET:
                if (dbtype.isInteger()) {
                    return new DETBf(key);
                } else {
                    return new DETAesCMC(key);
                }
            case DET_JOIN:
                if (dbtype.isInteger()) {
                    return new DETBf(key);
                } else {
                    return new DETAesCMC(key);
                }
            case RND:
                if (dbtype.isInteger()) {
                    return new RNDBf(key);
                } else {
                    return new RNDAes(key);
                }
            case OPE:
                if(Setting.USE_mOPE)
                    return new mOPEClient(key);
                else
                    return new OPEHyp(key);
            case PLAIN:
                return new PLAINLayer();
            default:
                throw new CDBException("Not Supportet EncLayer");
        }
    }

    @Override
    public String getEnclayerTag(EncLayer.EncLayerType type, int size) {
        if (!type.hasMultipleLayers())
            return "";

        switch (type) {
            case DET:
                if (size <= BasicCrypto.AES_BLOCK_BYTES) {
                    return "DETAesBf";
                } else {
                    return "DETAes";
                }
            case DET_JOIN:
                if (size <= BasicCrypto.AES_BLOCK_BYTES) {
                    return "DETAesBf";
                } else {
                    return "DETAes";
                }
            case RND:
                if (size <= BasicCrypto.BF_BLOCK_BYTES) {
                    return "RNDBf";
                } else {
                    return "RNDAes";
                }
            default:
                return "";
        }

    }

    @Override
    public IKeyManager getKeyManager() {
        return keyMan;
    }
}
