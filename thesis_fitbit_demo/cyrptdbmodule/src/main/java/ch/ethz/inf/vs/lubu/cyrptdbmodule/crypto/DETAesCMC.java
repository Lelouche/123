package ch.ethz.inf.vs.lubu.cyrptdbmodule.crypto;

import ch.ethz.inf.vs.lubu.cyrptdbmodule.cryptoalg.BasicCrypto;
import ch.ethz.inf.vs.lubu.cyrptdbmodule.dbscheme.CDBField;
import ch.ethz.inf.vs.lubu.cyrptdbmodule.exceptions.CDBException;
import ch.ethz.inf.vs.lubu.cyrptdbmodule.util.DBType;

/**
 * Created by lukas on 30.03.15.
 * Implements the DET Layer with AES/CMC
 */
public class DETAesCMC extends EncLayer {

    private final CDBKey key;

    public DETAesCMC(CDBKey key) {
        this.key = key;
        this.type = EncLayerType.DET;
    }

    @Override
    public CDBField encrypt(CDBField field) throws CDBException {
        byte[] iv, plain, res;
        iv = new byte[BasicCrypto.AES_BLOCK_BYTES];
        plain = field.getCloneValue();

        try {
            res = BasicCrypto.encrypt_AES_CMC(plain, key.getEncoded(), iv);
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
        iv = new byte[BasicCrypto.AES_BLOCK_BYTES];
        cipher = field.getCloneValue();

        try {
            res = BasicCrypto.decrypt_AES_CMC(cipher, key.getEncoded(), iv);
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
