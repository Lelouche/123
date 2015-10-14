package ch.ethz.inf.vs.lubu.cyrptdbmodule.cryptoalgtest;

import android.util.Log;

import junit.framework.TestCase;

import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

import ch.ethz.inf.vs.lubu.cyrptdbmodule.cryptoalg.OPE;

/**
 * Created by lukas on 18.03.15.
 */
public class TestOPE extends TestCase {

    private static String TEST_NAME = "TestOPE";

    private Random rn = new Random();

    public TestOPE() {
        super(TEST_NAME);
    }

    private void log(String name) {
        Log.v(TEST_NAME, name);
    }


    public void testOpe1() throws Exception {
        OPE ope = new OPE(generateKey(), 32, 64);
        int splain = 1054;
        int bplain = 2000;
        if (splain > bplain) {
            int temp = splain;
            splain = bplain;
            bplain = temp;
        }
        BigInteger plainS = BigInteger.valueOf(splain);
        BigInteger plainB = BigInteger.valueOf(bplain);
        BigInteger cipherS = ope.encrypt(plainS);
        BigInteger cipherB = ope.encrypt(plainB);

        assertTrue(cipherS.compareTo(cipherB) <= 0);

        BigInteger decryptS = ope.decrypt(cipherS);
        BigInteger decryptB = ope.decrypt(cipherB);

        assertEquals(plainS, decryptS);
        assertEquals(plainB, decryptB);
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
    public void setUp() throws Exception {
        super.setUp();
    }

    @Override
    public void tearDown() throws Exception {
        super.tearDown();
    }

}
