package ch.ethz.inf.vs.lubu.cyrptdbmodule.crypto;

import ch.ethz.inf.vs.lubu.cyrptdbmodule.cryptoalg.BasicCrypto;
import ch.ethz.inf.vs.lubu.cyrptdbmodule.dbscheme.CDBField;
import ch.ethz.inf.vs.lubu.cyrptdbmodule.exceptions.CDBException;
import ch.ethz.inf.vs.lubu.cyrptdbmodule.util.DBType;

/**
 * Created by lukas on 30.03.15.
 * Implements the DET BLOWFISH Layer
 */
public class DETBf extends EncLayer {

    private final CDBKey key;

    public DETBf(CDBKey key) {
        this.key = key;
        this.type = EncLayer.EncLayerType.DET;
    }

    @Override
    public CDBField encrypt(CDBField field) throws CDBException {
        byte[] iv, plain, res;
        plain = field.getCloneValue();
        DBType type = field.getType().getType();
        try {
            iv = new byte[BasicCrypto.BF_BLOCK_BYTES];
            if (type.getSizeInteger() < BasicCrypto.BF_BLOCK_BYTES) {
                res = BasicCrypto.encrypt_BLOWFISH_CBC(plain, key.getEncoded(), iv, true);
            } else {
                res = BasicCrypto.encrypt_BLOWFISH_CBC(plain, key.getEncoded(), iv, false);
            }
        } catch (Exception e) {
            throw new CDBException("Encryption failed " + e.getMessage());
        }

        field.setValue(res);
        field.setOutputType(CDBField.DBStrType.NUM);
        return field;
    }

    @Override
    public CDBField decrypt(CDBField field) throws CDBException {
        byte[] iv, cipher, res;
        cipher = field.getCloneValue();
        if(cipher.length<BasicCrypto.BF_BLOCK_BYTES) {
            byte[] temp = new byte[BasicCrypto.BF_BLOCK_BYTES];
            System.arraycopy(cipher,0,temp,temp.length-cipher.length,cipher.length);
            cipher = temp;
        }

        DBType type = field.getType().getType();
        try {
            iv = new byte[BasicCrypto.BF_BLOCK_BYTES];
            if (type.getSizeInteger() < BasicCrypto.BF_BLOCK_BYTES) {
                res = BasicCrypto.decrypt_BLOWFISH_CBC(cipher, key.getEncoded(), iv, true);
            } else {
                res = BasicCrypto.decrypt_BLOWFISH_CBC(cipher, key.getEncoded(), iv, false);
            }
        } catch (Exception e) {
            throw new CDBException("Decryption failed " + e.getMessage());
        }

        field.setValue(res);
        return field;
    }

    @Override
    public CDBField.DBStrType getStrType(DBType type) {
        return CDBField.DBStrType.NUM;
    }

}
