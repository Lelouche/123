package ch.ethz.inf.vs.lubu.cyrptdbmodule.cryptoalg;

import org.spongycastle.crypto.CipherParameters;
import org.spongycastle.crypto.engines.RC4Engine;
import org.spongycastle.crypto.params.KeyParameter;

import java.math.BigInteger;

import javax.crypto.spec.SecretKeySpec;

/**
 * Created by lukas on 27.03.15.
 * A Implementation of a RC4 Pseudo Random number Generator.
 * This Code is partly ported from CryptDB
 * http://css.csail.mit.edu/cryptdb/
 */
public class PRNGRC4Stream implements IPRNG {

    RC4Engine engine;

    public PRNGRC4Stream(byte[] key) {
        initRC4(key);
    }

    private void initRC4(byte[] key) {
        SecretKeySpec secret_key_spec = new SecretKeySpec(key, "RC4");
        this.engine = new RC4Engine();
        CipherParameters params_a = new KeyParameter(secret_key_spec.getEncoded());
        this.engine.init(true, params_a);
        //skip 1024 bytes
        byte[] temp = new byte[1024];
        this.engine.processBytes(temp, 0, temp.length, temp, 0);
    }

    private byte[] randBytes(int size) {
        byte[] rands = new byte[size];
        this.engine.processBytes(rands, 0, rands.length, rands, 0);
        return rands;
    }

    @Override
    public BigInteger getRandPrime(int nbits) {
        for (; ; ) {
            byte[] rands = randBytes((nbits + 7) / 8);
            BigInteger probPrime = new BigInteger(1, rands);
            probPrime = probPrime.setBit(nbits - 1);
            if (probPrime.isProbablePrime(25)) {
                return probPrime;
            }
        }
    }

    @Override
    public BigInteger getRandomNumber(int nbits) {
        BigInteger rand;
        byte[] rands = randBytes((nbits + 7) / 8);
        rand = new BigInteger(1, rands);
        rand = rand.setBit(nbits - 1);
        return rand;
    }

    @Override
    public BigInteger getRandMod(BigInteger maxIt) {
        byte[] rands = randBytes(maxIt.bitLength() / 8 + 1);
        return new BigInteger(rands).mod(maxIt);
    }

    @Override
    public BigInteger getRandModHGD(BigInteger div) {
        byte[] rands = randBytes(div.bitLength() / 8 + 1);
        return new BigInteger(rands).mod(div);
    }

    @Override
    public void nextBytes(byte[] out) {
        byte[] rand = randBytes(out.length);
        System.arraycopy(rand, 0, out, 0, out.length);
    }
}
