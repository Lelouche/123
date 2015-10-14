package ch.ethz.inf.vs.lubu.cyrptdbmodule.benchmark;

import android.util.Log;

import com.google.common.base.Stopwatch;

import junit.framework.TestCase;

import java.util.Random;
import java.util.concurrent.TimeUnit;

import ch.ethz.inf.vs.lubu.cyrptdbmodule.FileUtil;
import ch.ethz.inf.vs.lubu.cyrptdbmodule.cryptoalg.FastECElGamal;
import ch.ethz.inf.vs.lubu.cyrptdbmodule.cryptoalg.IPRNG;
import ch.ethz.inf.vs.lubu.cyrptdbmodule.cryptoalg.PRNGRC4Stream;

/**
 * Created by lukas on 15.06.15.
 */
public class BenchECElGamalKeyGen extends TestCase {

    private static final String TEST_NAME = "BenchECElGamalKeyGen";
    private static final String DELIM = ";";
    private static final String NL = "\n";

    private static final int NUM_IT = 1000;

    private StringBuilder sb = new StringBuilder();

    private Random rm = new Random();

    private Stopwatch stopwatch = Stopwatch.createUnstarted();

    public BenchECElGamalKeyGen() {
        super(TEST_NAME);
    }

    private void log(String name) {
        Log.i(TEST_NAME, name);
    }


    private void generateKey() throws Exception {
        byte[] prngkey  = new byte[16];
        rm.nextBytes(prngkey);
        IPRNG prng = new PRNGRC4Stream(prngkey);

        stopwatch.start();
        FastECElGamal.generateKeys(prng);
        stopwatch.stop();

        sb.append(stopwatch.elapsed(TimeUnit.NANOSECONDS)).append(NL);
        stopwatch.reset();
    }

    public void testEcElGamalKeyGen() throws Exception {
        for (int i = 0; i < NUM_IT; i++) {
            generateKey();
        }
    }
    private void addCollums() {
        sb.append("ECELGAMAL_KEYGEN").append(NL);
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        addCollums();
    }

    @Override
    public void tearDown() throws Exception {
        super.tearDown();
        FileUtil fu = new FileUtil(TEST_NAME + ".csv");
        fu.writeToFile(sb.toString());
    }

}
