package ch.ethz.inf.vs.lubu.cyrptdbmodule.benchmark;

import android.util.Log;

import com.google.common.base.Stopwatch;

import junit.framework.TestCase;

import java.math.BigInteger;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import ch.ethz.inf.vs.lubu.cyrptdbmodule.FileUtil;
import ch.ethz.inf.vs.lubu.cyrptdbmodule.cryptoalg.ECELGamal;
import ch.ethz.inf.vs.lubu.cyrptdbmodule.cryptoalg.ECELGamalPriv;
import ch.ethz.inf.vs.lubu.cyrptdbmodule.cryptoalg.IPRNG;
import ch.ethz.inf.vs.lubu.cyrptdbmodule.cryptoalg.PRNGImpl;

/**
 * Created by lukas on 02.04.15.
 */
public class BenchECElGamal extends TestCase {

    private static final String DELIM = ";";
    private static final String NL = "\n";

    private static final String TEST_NAME = "BenchECElGamal";

    private static final int NUM_IT = 100;

    private Random rm = new Random();

    private ECELGamalPriv gamal;

    private StringBuilder sb = new StringBuilder();

    private Stopwatch watch = Stopwatch.createUnstarted();

    public BenchECElGamal() {
        super(TEST_NAME);
    }

    private void log(String name) {
        Log.v(TEST_NAME, name);
    }


    public void parttestECElgamal() throws Exception {

        int a = rm.nextInt();
        BigInteger A = BigInteger.valueOf(a);

        watch.start();
        ECELGamal.ELGamalCipher cipherA = gamal.encrypt(A);
        watch.stop();
        sb.append(watch.elapsed(TimeUnit.MILLISECONDS)).append(NL);
        watch.reset();
    }

    public void testBench() throws Exception {
        for (int i = 0; i < NUM_IT; i++) {
            parttestECElgamal();
        }
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        IPRNG rand = new PRNGImpl();
        gamal = new ECELGamalPriv(ECELGamal.generateKeys(rand), rand);
    }

    @Override
    public void tearDown() throws Exception {
        super.tearDown();
        FileUtil fu = new FileUtil(TEST_NAME + ".csv");
        fu.writeToFile(sb.toString());

    }

}
