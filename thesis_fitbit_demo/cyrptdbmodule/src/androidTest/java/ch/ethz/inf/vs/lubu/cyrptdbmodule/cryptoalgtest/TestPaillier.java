package ch.ethz.inf.vs.lubu.cyrptdbmodule.cryptoalgtest;

import android.util.Log;

import com.google.common.base.Stopwatch;

import junit.framework.TestCase;

import java.math.BigInteger;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import ch.ethz.inf.vs.lubu.cyrptdbmodule.cryptoalg.PRNGImpl;
import ch.ethz.inf.vs.lubu.cyrptdbmodule.cryptoalg.PaillierPriv;


/**
 * Created by lukas on 17.03.15.
 */
public class TestPaillier extends TestCase {

    private static final String TEST_NAME = "PaillierTest";

    private static final int NUM_IT = 10;

    private static final int NUM_NBITS = 1024;

    private static final int NUM_ABITS = 256;

    private PaillierPriv pail;

    private Random rm = new Random();

    private Stopwatch stopwatch = Stopwatch.createUnstarted();

    private long timeEncSum = 0;
    private long timeDecSum = 0;

    public TestPaillier() {
        super("PaillierTest");
    }

    private void log(String name) {
        Log.i(TEST_NAME, name);
    }


    private void smallTestPart() throws Exception {
        int a = rm.nextInt();
        int b = rm.nextInt();

        log(String.valueOf(a) + ", " + String.valueOf(b));

        BigInteger A = BigInteger.valueOf(a);
        BigInteger B = BigInteger.valueOf(b);

        stopwatch.start();
        BigInteger cA = pail.encrypt(A);
        stopwatch.stop();
        timeEncSum += stopwatch.elapsed(TimeUnit.NANOSECONDS);
        stopwatch.reset();

        BigInteger cB = pail.encrypt(B);
        BigInteger sum = pail.add(cA, cB);
        sum = pail.add(sum, cB);
        sum = pail.add(sum, cB);

        stopwatch.start();
        BigInteger dA = pail.decrypt(cA);
        stopwatch.stop();
        timeDecSum += stopwatch.elapsed(TimeUnit.NANOSECONDS);
        stopwatch.reset();

        BigInteger dB = pail.decrypt(cB);
        BigInteger dsum = pail.decrypt(sum);

        assertEquals(a, dA.intValue());
        assertEquals(b, dB.intValue());
        assertEquals(dsum, A.add(B).add(B).add(B));
        assertEquals(dsum.toString(), A.add(B).add(B).add(B).toString());
    }

    public void testPaillier() throws Exception {
        for (int i = 0; i < NUM_IT; i++) {
            smallTestPart();
        }
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        PRNGImpl rand = new PRNGImpl();
        pail = new PaillierPriv(PaillierPriv.keygen(rand, NUM_NBITS, NUM_ABITS));
        //pail.rand_gen(NUM_IT+1,NUM_IT+1);

    }

    @Override
    public void tearDown() throws Exception {
        super.tearDown();
        double avgEnc = ((double) timeEncSum) / NUM_IT / 1000000;
        double avgDec = ((double) timeDecSum) / NUM_IT / 1000000;
        log("Average Enc Time: " + String.valueOf(avgEnc) + " ms");
        log("Average Dec Time: " + String.valueOf(avgDec) + " ms");
    }
}
