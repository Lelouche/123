package ch.ethz.inf.vs.lubu.cyrptdbmodule.cryptoalg;

import java.math.BigInteger;

/**
 * Created by lukas on 18.03.15.
 * A implementation of a AES Pseudo Random Number Generator
 * This code is partly ported from CryptDB
 * http://css.csail.mit.edu/cryptdb/
 */
public class PRNGAesBlock implements IPRNG {

    private byte[] ctr;

    private byte[] key;

    public PRNGAesBlock(byte[] key) {
        ctr = new byte[BasicCrypto.AES_BLOCK_BYTES];
        this.key = key;
    }

    private byte[] randBytes(int nbytes) {
        byte buff[] = new byte[nbytes];
        for (int i = 0; i < nbytes; i += ctr.length) {
            for (int j = 0; j < ctr.length; j++) {
                ctr[j]++;
                if (ctr[j] != 0)
                    break;
            }

            byte[] ct = null;
            byte[] iv = new byte[ctr.length];
            try {
                ct = BasicCrypto.encrypt_AES_CBC(ctr, key, iv, false);
            } catch (Exception e) {
                e.printStackTrace();
            }
            int length = ctr.length;
            if (!(ctr.length < (nbytes - i))) {
                length = (nbytes - i);
            }

            System.arraycopy(ct, 0, buff, i, length);
        }
        return buff;
    }

    public void setCtr(byte[] in) {
        System.arraycopy(in, 0, ctr, 0, ctr.length);
    }

    @Override
    public void nextBytes(byte[] out) {
        byte[] rand = randBytes(out.length);
        System.arraycopy(rand, 0, out, 0, out.length);
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
}
