package ch.ethz.inf.vs.lubu.cyrptdbmodule.cryptoalg;

import org.spongycastle.math.ec.ECPoint;

import java.math.BigInteger;
import java.security.KeyPair;

/**
 * Created by lukas on 27.03.15.
 * Implementation of the private part of the EC-ELGamal Crypto-System.
 * ECC operations from SpongyCastle. (bad performance)
 */
public class ECELGamalPriv extends ECELGamal {

    private BigInteger secret;

    public ECELGamalPriv(KeyPair keypair, IPRNG rnd) {
        super((ELGamalPublicKey) keypair.getPublic(), rnd);
        ELGamalPrivateKey priv = (ELGamalPrivateKey) keypair.getPrivate();
        this.secret = priv.getX();
    }

    public BigInteger decrypt(ELGamalCipher cipher, long maxNum) {
        ECPoint M = (cipher.R.multiply(secret.negate())).add(cipher.S);
        BigInteger res = invMappingFunc(M, maxNum);
        return res;
    }
}
