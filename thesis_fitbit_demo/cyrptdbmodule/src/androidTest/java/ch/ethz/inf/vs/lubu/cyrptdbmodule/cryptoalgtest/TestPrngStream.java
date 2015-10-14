package ch.ethz.inf.vs.lubu.cyrptdbmodule.cryptoalgtest;

import android.util.Log;

import junit.framework.TestCase;

import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

import ch.ethz.inf.vs.lubu.cyrptdbmodule.cryptoalg.IPRNG;
import ch.ethz.inf.vs.lubu.cyrptdbmodule.cryptoalg.PRNGRC4Stream;

/**
 * Created by lukas on 27.03.15.
 */
public class TestPrngStream extends TestCase {

    private static String TEST_NAME = "TestPrngStream";

    public TestPrngStream() {
        super(TEST_NAME);
    }

    private static final int NUM_IT = 10;

    private IPRNG prng;

    private void log(String name) {
        Log.i(TEST_NAME, name);
    }


    public void testTest1() throws Exception {
        for (int i = 0; i < NUM_IT; i++) {
            BigInteger prime1, prime2;
            prime1 = prng.getRandPrime(100);
            assertTrue(prime1.isProbablePrime(25));
            prime2 = prng.getRandPrime(100);
            assertTrue(prime2.isProbablePrime(25));

            assertTrue(prime1.compareTo(prime2) != 0);
            log("Prime1: " + prime1.toString() + " Prime2: " + prime2.toString());
        }
    }

    public void testTest2() throws Exception {
        for (int i = 0; i < NUM_IT; i++) {
            BigInteger num1, num2;
            num1 = prng.getRandomNumber(100);
            num2 = prng.getRandomNumber(100);

            assertTrue(num1.compareTo(num2) != 0);
            log("Num1: " + num1.toString() + " Num2: " + num2.toString());
        }
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        this.prng = new PRNGRC4Stream(generateKey());
    }

    private byte[] generateKey() {
        KeyGenerator keyGen = null;
        try {
            keyGen = KeyGenerator.getInstance("AES");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        keyGen.init(128);
        SecretKey secretKey = keyGen.generateKey();
        return secretKey.getEncoded();
    }


    @Override
    public void tearDown() throws Exception {
        super.tearDown();
    }
}
