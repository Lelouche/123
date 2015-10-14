package ch.ethz.inf.vs.lubu.cyrptdbmodule.benchmark;

import android.util.Log;

import com.google.common.base.Stopwatch;

import junit.framework.TestCase;

import java.math.BigInteger;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import ch.ethz.inf.vs.lubu.cyrptdbmodule.FileUtil;
import ch.ethz.inf.vs.lubu.cyrptdbmodule.cryptoalg.IPRNG;
import ch.ethz.inf.vs.lubu.cyrptdbmodule.cryptoalg.PRNGRC4Stream;
import ch.ethz.inf.vs.lubu.cyrptdbmodule.cryptoalg.PaillierPriv;

/**
 * Created by lukas on 22.03.15.
 */
public class BenchPaillier extends TestCase {

    private static final String TEST_NAME = "BenchPaillier";
    private static final String DELIM = ";";
    private static final String NL = "\n";

    private static final int NUM_IT = 1000;

    private static final int NUM_NBITS = 1024;

    private static final int NUM_ABITS = 256;

    private PaillierPriv pail;

    private StringBuilder sb = new StringBuilder();

    private Random rm = new Random();

    private Stopwatch stopwatch = Stopwatch.createUnstarted();

    public BenchPaillier() {
        super("BenchPaillier");
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
        sb.append(stopwatch.elapsed(TimeUnit.NANOSECONDS)).append(DELIM);
        stopwatch.reset();

        BigInteger cB = pail.encrypt(B);
        BigInteger sum = pail.add(cA, cB);

        stopwatch.start();
        BigInteger dA = pail.decrypt(cA);
        stopwatch.stop();
        sb.append(stopwatch.elapsed(TimeUnit.NANOSECONDS)).append(NL);
        stopwatch.reset();

        BigInteger dB = pail.decrypt(cB);
        BigInteger dsum = pail.decrypt(sum);

        assertEquals(a, dA.intValue());
        assertEquals(b, dB.intValue());
        assertEquals(dsum, A.add(B));
    }

    public void testPaillier() throws Exception {
        for (int i = 0; i < NUM_IT; i++) {
            smallTestPart();
        }
    }

    private void addCollums() {
        sb.append("PAILLER_ENC").append(DELIM).append("PAILLER_DEC").append(NL);
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        addCollums();
        byte[] prngkey  = new byte[16];
        rm.nextBytes(prngkey);
        IPRNG rand = new PRNGRC4Stream(prngkey);

        stopwatch.start();
        pail = new PaillierPriv(PaillierPriv.keygen(rand, NUM_NBITS, NUM_ABITS));
        stopwatch.stop();
        log("Keygentime: " + stopwatch.elapsed(TimeUnit.NANOSECONDS) + " ns");

        stopwatch.reset();
    }

    @Override
    public void tearDown() throws Exception {
        super.tearDown();
        FileUtil fu = new FileUtil(TEST_NAME + ".csv");
        fu.writeToFile(sb.toString());
    }

}
