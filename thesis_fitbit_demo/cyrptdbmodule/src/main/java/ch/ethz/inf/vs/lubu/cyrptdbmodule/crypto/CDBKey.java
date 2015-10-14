package ch.ethz.inf.vs.lubu.cyrptdbmodule.crypto;


import java.security.Key;

/**
 * Created by lukas on 30.03.15.
 * Represents a Key for a column in CryptDB
 */
public class CDBKey implements Key {

    private byte[] key;

    public CDBKey(byte[] bytes) {
        key = bytes;
    }

    @Override
    public String getAlgorithm() {
        return "AES";
    }

    @Override
    public String getFormat() {
        return null;
    }

    @Override
    public byte[] getEncoded() {
        return key.clone();
    }

}
