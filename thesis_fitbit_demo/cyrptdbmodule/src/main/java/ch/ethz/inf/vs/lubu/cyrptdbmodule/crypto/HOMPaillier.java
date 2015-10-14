package ch.ethz.inf.vs.lubu.cyrptdbmodule.crypto;

import android.util.Log;

import java.math.BigInteger;

import ch.ethz.inf.vs.lubu.cyrptdbmodule.cryptoalg.IPRNG;
import ch.ethz.inf.vs.lubu.cyrptdbmodule.cryptoalg.PRNGRC4Stream;
import ch.ethz.inf.vs.lubu.cyrptdbmodule.cryptoalg.PaillierPriv;
import ch.ethz.inf.vs.lubu.cyrptdbmodule.dbscheme.CDBField;
import ch.ethz.inf.vs.lubu.cyrptdbmodule.exceptions.CDBException;
import ch.ethz.inf.vs.lubu.cyrptdbmodule.util.CDBUtil;
import ch.ethz.inf.vs.lubu.cyrptdbmodule.util.DBType;

/**
 * Created by lukas on 30.03.15.
 */
public class HOMPaillier extends HOMLayer {

    private static final int N_BITS = 1024;

    private static final int A_BITS = 256;

    private PaillierPriv pail;

    public HOMPaillier(CDBKey key) {
        super();
        IPRNG prng = new PRNGRC4Stream(key.getEncoded());
        this.pail = new PaillierPriv(PaillierPriv.keygen(prng, N_BITS, A_BITS));
    }

    @Override
    public CDBField encrypt(CDBField field) throws CDBException {
        BigInteger plain, res;
        plain = field.getSignedBigInteger();
        res = pail.encrypt(plain);
        field.setValue(res);
        field.setOutputType(CDBField.DBStrType.HEXSTR);
        return field;
    }

    @Override
    public CDBField decrypt(CDBField field) throws CDBException {
        BigInteger cipher, res;
        cipher = field.getUnsignedBigInteger();
        res = pail.decrypt(cipher);
        field.setValue(res);
        field.setOutputType(CDBField.DBStrType.NUM);
        return field;
    }

    @Override
    public String getAgrFunc(String arg) {
        StringBuilder sb = new StringBuilder();
        sb.append(CDBUtil.UDF_AGR_FUNC_PAILLIER).append("( ");
        sb.append(arg).append(", '");
        sb.append(CDBUtil.bytesToHex(pail.hompubkey().toByteArray()));
        Log.d("PAILLIERPUB",CDBUtil.bytesToHex(pail.hompubkey().toByteArray()));
        sb.append("' )");
        return sb.toString();
    }

    @Override
    public CDBField.DBStrType getStrType(DBType type) {
        return CDBField.DBStrType.HEXSTR;
    }
}
