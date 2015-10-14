package ch.ethz.inf.vs.lubu.cyrptdbmodule.cryptoalg;

import java.math.BigInteger;
import java.security.SecureRandom;

/**
 * Created by lukas on 12.03.15.
 * Pseudo Random Number Generator with Androids SecureRandom
 */
public class PRNGImpl implements IPRNG {

    private SecureRandom sn = new SecureRandom();

    @Override
    public BigInteger getRandPrime(int nbits) {
        reseed();
        return BigInteger.probablePrime(nbits, sn);
    }

    @Override
    public BigInteger getRandomNumber(int nbits) {
        reseed();
        return (new BigInteger(nbits, sn)).setBit(nbits - 1);
    }

    @Override
    public BigInteger getRandMod(BigInteger num) {
        reseed();
        byte[] buffer = new byte[num.bitLength() / 8 + 1];
        sn.nextBytes(buffer);
        BigInteger temp = new BigInteger(buffer);
        return temp.mod(num);
    }

    @Override
    public BigInteger getRandModHGD(BigInteger max) {
        reseed();
        byte[] buffer = new byte[max.bitLength() / 8 + 1];
        sn.nextBytes(buffer);
        BigInteger num = new BigInteger(buffer);
        return num.mod(max);
    }

    @Override
    public void nextBytes(byte[] out) {
        sn.nextBytes(out);
    }

    private void reseed() {
        sn = new SecureRandom();
    }
}
