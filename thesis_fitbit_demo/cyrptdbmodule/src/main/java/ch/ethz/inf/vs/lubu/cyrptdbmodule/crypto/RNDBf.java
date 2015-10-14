package ch.ethz.inf.vs.lubu.cyrptdbmodule.crypto;

import ch.ethz.inf.vs.lubu.cyrptdbmodule.cryptoalg.BasicCrypto;
import ch.ethz.inf.vs.lubu.cyrptdbmodule.dbscheme.CDBField;
import ch.ethz.inf.vs.lubu.cyrptdbmodule.exceptions.CDBException;
import ch.ethz.inf.vs.lubu.cyrptdbmodule.util.CDBUtil;
import ch.ethz.inf.vs.lubu.cyrptdbmodule.util.DBType;

/**
 * Created by lukas on 30.03.15.
 */
public class RNDBf extends EncLayer {

    private final byte[] key;

    public RNDBf(CDBKey key) {
        byte[] bfkey = new byte[BasicCrypto.BF_BLOCK_BYTES];
        System.arraycopy(key.getEncoded(), 0, bfkey, 0, bfkey.length);
        this.key = bfkey;
        this.type = EncLayerType.RND;
    }

    @Override
    public CDBField encrypt(CDBField field) throws CDBException {
        byte[] iv, plain, res;
        DBType type = field.getType().getType();

        iv = field.getSaltSmall();
        if (iv == null) {
            field.setSalt(CDBUtil.generateSalt());
            iv = field.getSaltSmall();
        }

        plain = field.getCloneValue();

        try {
            if (type.getSizeInteger() < BasicCrypto.BF_BLOCK_BYTES) {
                res = BasicCrypto.encrypt_BLOWFISH_CBC(plain, key, iv, true);
            } else {
                res = BasicCrypto.encrypt_BLOWFISH_CBC(plain, key, iv, false);
            }
        } catch (Exception e) {
            throw new CDBException("Encryption failed " + e.getMessage());
        }

        field.setValue(res);
        if (field.getType().getType().isInteger())
            field.setOutputType(CDBField.DBStrType.NUM);
        else {
            field.setOutputType(CDBField.DBStrType.BASE64STR);
        }
        return field;
    }

    @Override
    public CDBField decrypt(CDBField field) throws CDBException {
        byte[] iv, cipher, res;
        DBType type = field.getType().getType();
        iv = field.getSaltSmall();

        cipher = field.getCloneValue();
        if(cipher.length<BasicCrypto.BF_BLOCK_BYTES) {
            byte[] temp = new byte[BasicCrypto.BF_BLOCK_BYTES];
            System.arraycopy(cipher,0,temp,temp.length-cipher.length,cipher.length);
            cipher = temp;
        }

        try {
            if (type.getSizeInteger() < BasicCrypto.BF_BLOCK_BYTES) {
                res = BasicCrypto.decrypt_BLOWFISH_CBC(cipher, key, iv, true);
            } else {
                res = BasicCrypto.decrypt_BLOWFISH_CBC(cipher, key, iv, false);
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
