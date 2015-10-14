package ch.ethz.inf.vs.lubu.cyrptdbmodule.cryptoalg;

import java.math.BigInteger;

/**
 * Created by lukas on 10.03.15.
 * Interface for a Pseudo Random Number Generator
 */
public interface IPRNG {

    public void nextBytes(byte[] out);

    public BigInteger getRandPrime(int nbits);

    public  BigInteger getRandomNumber(int nbits);

    public BigInteger getRandMod(BigInteger maxIt);

    public BigInteger getRandModHGD(BigInteger div);

}
