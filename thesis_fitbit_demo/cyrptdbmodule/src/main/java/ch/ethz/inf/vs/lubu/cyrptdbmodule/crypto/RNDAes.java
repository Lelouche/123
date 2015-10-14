package ch.ethz.inf.vs.lubu.cyrptdbmodule.crypto;

import ch.ethz.inf.vs.lubu.cyrptdbmodule.cryptoalg.BasicCrypto;
import ch.ethz.inf.vs.lubu.cyrptdbmodule.dbscheme.CDBField;
import ch.ethz.inf.vs.lubu.cyrptdbmodule.exceptions.CDBException;
import ch.ethz.inf.vs.lubu.cyrptdbmodule.util.CDBUtil;
import ch.ethz.inf.vs.lubu.cyrptdbmodule.util.DBType;

/**
 * Created by lukas on 30.03.15.
 */
public class RNDAes extends EncLayer {

    private final CDBKey key;

    public RNDAes(CDBKey key) {
        this.key = key;
        this.type = EncLayerType.RND;
    }

    @Override
    public CDBField encrypt(CDBField field) throws CDBException {
        byte[] iv, plain, res;
        iv = field.getSaltBig();
        if (iv == null) {
            field.setSalt(CDBUtil.generateSalt());
            iv = field.getSaltBig();
        }
        plain = field.getCloneValue();

        try {
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
        iv = field.getSaltBig();
        cipher = field.getCloneValue();

        try {
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
