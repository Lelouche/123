package ch.ethz.inf.vs.lubu.cyrptdbmodule.crypto;

import java.math.BigInteger;

import ch.ethz.inf.vs.lubu.cyrptdbmodule.cryptoalg.FastECElGamal;
import ch.ethz.inf.vs.lubu.cyrptdbmodule.cryptoalg.IPRNG;
import ch.ethz.inf.vs.lubu.cyrptdbmodule.cryptoalg.NativeECElGamal;
import ch.ethz.inf.vs.lubu.cyrptdbmodule.cryptoalg.NativeECElGamalCrypto;
import ch.ethz.inf.vs.lubu.cyrptdbmodule.cryptoalg.PRNGImpl;
import ch.ethz.inf.vs.lubu.cyrptdbmodule.cryptoalg.PRNGRC4Stream;
import ch.ethz.inf.vs.lubu.cyrptdbmodule.dbscheme.CDBField;
import ch.ethz.inf.vs.lubu.cyrptdbmodule.exceptions.CDBException;
import ch.ethz.inf.vs.lubu.cyrptdbmodule.util.CDBUtil;
import ch.ethz.inf.vs.lubu.cyrptdbmodule.util.DBType;
import ch.ethz.inf.vs.lubu.cyrptdbmodule.util.Logger;

/**
 * Created by lukas on 17.04.15.
 */
public class HOMECElGamal extends HOMLayer {

    private NativeECElGamalCrypto alg;

    public HOMECElGamal(CDBKey key) {
        super();
        IPRNG prng = new PRNGRC4Stream(key.getEncoded());
        alg = new FastECElGamal(NativeECElGamal.generateKeys(prng), new PRNGImpl());
    }

    @Override
    public String getAgrFunc(String arg) {
        StringBuilder sb = new StringBuilder();
        sb.append(CDBUtil.UDF_AGR_FUNC_ELGAMAL).append("( ");
        sb.append(arg).append(")");
        return sb.toString();

    }

    @Override
    public CDBField encrypt(CDBField field) throws CDBException {
        BigInteger plain;
        NativeECElGamalCrypto.NativeECElgamalCipher cipher;
        plain = field.getSignedBigInteger();
        try {
            cipher = alg.encrypt(plain);
        } catch (Exception e) {
            throw new CDBException("Encryption failed: "+ e.getMessage());
        }
        field.setValue(cipher.getForDB());
        field.setOutputType(CDBField.DBStrType.ECPOINTSTR);
        return field;
    }

    @Override
    public CDBField decrypt(CDBField field) throws CDBException {
        BigInteger plain;
        NativeECElGamal.NativeECElgamalCipher cipher;
        cipher = new NativeECElGamal.NativeECElgamalCipher(field.getStrRep());
        try {
            plain = alg.decrypt(cipher, field.getType().getType().getSizeInteger());
        } catch (Exception e) {
            throw new CDBException("Decryption failed: "+ e.getMessage());
        }
        Logger.log("Decryped "+plain);
        field.setValue(plain);
        field.setOutputType(CDBField.DBStrType.NUM);
        return field;
    }

    @Override
    public CDBField.DBStrType getStrType(DBType type) {
        return CDBField.DBStrType.ECPOINTSTR;
    }
}
