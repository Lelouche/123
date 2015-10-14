package ch.ethz.inf.vs.lubu.cyrptdbmodule.crypto;

import java.math.BigInteger;

import ch.ethz.inf.vs.lubu.cyrptdbmodule.cryptoalg.OPE;
import ch.ethz.inf.vs.lubu.cyrptdbmodule.dbscheme.CDBField;
import ch.ethz.inf.vs.lubu.cyrptdbmodule.exceptions.CDBException;
import ch.ethz.inf.vs.lubu.cyrptdbmodule.util.DBType;

/**
 * Created by lukas on 17.04.15.
 */
public class OPEHyp extends EncLayer {

    private static final int FROM_BITS = 32;

    private static final int TO_BITS = 64;

    private OPE alg;

    public OPEHyp(CDBKey key) {
        super();
        alg = new OPE(key.getEncoded(), FROM_BITS, TO_BITS);
    }

    @Override
    public CDBField encrypt(CDBField field) throws CDBException {
        BigInteger plain, cipher;
        plain = field.getSignedBigInteger();
        cipher = alg.encrypt(plain);
        field.setValue(cipher);
        field.setOutputType(CDBField.DBStrType.NUM);
        return field;
    }

    @Override
    public CDBField decrypt(CDBField field) throws CDBException {
        BigInteger plain, cipher;
        cipher = field.getSignedBigInteger();
        plain = alg.decrypt(cipher);
        field.setValue(plain);
        return field;
    }

    @Override
    public CDBField.DBStrType getStrType(DBType type) {
        return CDBField.DBStrType.NUM;
    }


}
