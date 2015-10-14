package ch.ethz.inf.vs.lubu.cyrptdbmodule.crypto;

import ch.ethz.inf.vs.lubu.cyrptdbmodule.cryptoalg.BasicCrypto;
import ch.ethz.inf.vs.lubu.cyrptdbmodule.dbscheme.CDBField;
import ch.ethz.inf.vs.lubu.cyrptdbmodule.exceptions.CDBException;
import ch.ethz.inf.vs.lubu.cyrptdbmodule.util.DBType;

/**
 * Created by lukas on 11.05.15.
 * Implements the DET layer with AES
 */
public class DETAes extends EncLayer {

    private final CDBKey key;

    public DETAes(CDBKey key) {
        this.key = key;
        this.type = EncLayer.EncLayerType.DET;
    }

    @Override
    public CDBField encrypt(CDBField field) throws CDBException {
        byte[] iv, plain, res;
        plain = field.getCloneValue();
        try {
            iv = new byte[BasicCrypto.AES_BLOCK_BYTES];
            res = BasicCrypto.encrypt_AES_CBC(plain, key.getEncoded(), iv, true);
        } catch (Exception e) {
            throw new CDBException("Encryption failed " + e.getMessage());
        }

        field.setValue(res);
        field.setOutputType(CDBField.DBStrType.BASE64STR);
        return field;
    }

    @Override
    public CDBField decrypt(CDBField field) throws CDBException {
        byte[] iv, cipher, res;
        cipher = field.getCloneValue();
        try {
            iv = new byte[BasicCrypto.AES_BLOCK_BYTES];
            res = BasicCrypto.decrypt_AES_CBC(cipher, key.getEncoded(), iv, true);
        } catch (Exception e) {
            throw new CDBException("Decryption failed " + e.getMessage());
        }

        field.setValue(res);
        return field;
    }

    @Override
    public CDBField.DBStrType getStrType(DBType type) {
        return CDBField.DBStrType.BASE64STR;
    }

}
