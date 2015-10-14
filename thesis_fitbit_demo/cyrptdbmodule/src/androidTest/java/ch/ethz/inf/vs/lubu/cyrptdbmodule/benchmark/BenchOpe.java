package ch.ethz.inf.vs.lubu.cyrptdbmodule.benchmark;

import android.util.Log;

import com.google.common.base.Stopwatch;

import junit.framework.TestCase;

import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

import ch.ethz.inf.vs.lubu.cyrptdbmodule.FileUtil;
import ch.ethz.inf.vs.lubu.cyrptdbmodule.cryptoalg.OPE;

/**
 * Created by lukas on 22.03.15.
 */
public class BenchOpe extends TestCase {

    private static final String TEST_NAME = "BenchOpe";
    private static final String DELIM = ";";
    private static final String NL = "\n";

    private static final int NUM_IT = 20;

    private static final int NUM_BITSPLAIN = 32;

    private static final int NUM_BITSCIPHER = 64;

    private OPE ope;

    private StringBuilder sb = new StringBuilder();

    private Random rm = new Random();

    private Stopwatch stopwatch = Stopwatch.createUnstarted();

    public BenchOpe() {
        super(TEST_NAME);
    }

    private void log(String name) {
        Log.i(TEST_NAME, name);
    }


    private void smallTestPart() throws Exception {

        //int splain = (int) (Math.random()*100000);
        int splain = rm.nextInt();
        if (splain < 0)
            splain = -splain;
        /*int bplain = (int) (Math.random()*100000);
        if(splain>bplain) {
            int temp = splain;
            splain = bplain;
            bplain = temp;
        }*/

        BigInteger plainS = BigInteger.valueOf(splain);
        //BigInteger plainB = BigInteger.valueOf(bplain);

        stopwatch.start();
        BigInteger cipherS = ope.encrypt(plainS);
        stopwatch.stop();
        sb.append(stopwatch.elapsed(TimeUnit.NANOSECONDS)).append(DELIM);
        stopwatch.reset();

        /*BigInteger cipherB = ope.encrypt(plainB);

        assertTrue(cipherS.compareTo(cipherB) <= 0);*/

        stopwatch.start();
        BigInteger decryptS = ope.decrypt(cipherS);
        stopwatch.stop();
        sb.append(stopwatch.elapsed(TimeUnit.NANOSECONDS)).append(NL);
        stopwatch.reset();

        //BigInteger decryptB = ope.decrypt(cipherB);

        assertEquals(plainS, decryptS);
        log("Plain: " + splain + " Enc: " + cipherS.toString());
        //assertEquals(plainB,decryptB);
    }

    public void testOPE() throws Exception {
        for (int i = 0; i < NUM_IT; i++) {
            smallTestPart();
        }
    }

    private void addCollums() {
        sb.append("OPE_ENC").append(DELIM).append("OPE_DEC").append(NL);
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
        addCollums();

        ope = new OPE(generateKey(), NUM_BITSPLAIN, NUM_BITSCIPHER);
    }

    @Override
    public void tearDown() throws Exception {
        super.tearDown();
        FileUtil fu = new FileUtil(TEST_NAME + ".csv");
        fu.writeToFile(sb.toString());
    }
}
