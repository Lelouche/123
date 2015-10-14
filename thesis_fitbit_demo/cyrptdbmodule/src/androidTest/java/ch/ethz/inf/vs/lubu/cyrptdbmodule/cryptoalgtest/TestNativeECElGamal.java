package ch.ethz.inf.vs.lubu.cyrptdbmodule.cryptoalgtest;

import android.test.InstrumentationTestCase;
import android.util.Log;

import java.math.BigInteger;
import java.util.Random;

import ch.ethz.inf.vs.lubu.cyrptdbmodule.cryptoalg.NativeECElGamal;
import ch.ethz.inf.vs.lubu.cyrptdbmodule.cryptoalg.PRNGImpl;
import ch.ethz.inf.vs.lubu.cyrptdbmodule.util.ContextHolder;

/**
 * Created by lukas on 05.04.15.
 */
public class TestNativeECElGamal extends InstrumentationTestCase {

    private static String TEST_NAME = "TestNativeECElGamal";

    private Random rand = new Random();

    private NativeECElGamal nec;

    private static final int MAX_INT = Integer.MAX_VALUE;

    private static final int MAX_IT = 10;

    public TestNativeECElGamal() {

    }

    private void log(String name) {
        Log.v(TEST_NAME, name);
    }


    public void testTest1() throws Exception {
        BigInteger plain = BigInteger.valueOf(234234);
        NativeECElGamal.NativeECElgamalCipher cipher = nec.encrypt(plain);
        BigInteger res = nec.decrypt(cipher,4);
        assertEquals(res.toString(), plain.toString());
    }

    public void testTest2() throws Exception {
        for (int i = 0; i < MAX_IT; i++) {
            BigInteger cur = BigInteger.valueOf(rand.nextInt(MAX_INT));
            partTest(cur);
        }
    }

    private void partTest(BigInteger plain) {
        NativeECElGamal.NativeECElgamalCipher cipher = nec.encrypt(plain);
        BigInteger res = nec.decrypt(cipher,4);
        assertEquals(res.toString(), plain.toString());
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        ContextHolder.setContext(this.getInstrumentation().getContext());
        nec = new NativeECElGamal(NativeECElGamal.generateKeys(new PRNGImpl()), new PRNGImpl());
        NativeECElGamal.preLoadFileTable();
        int i = 0;
    }

    @Override
    public void tearDown() throws Exception {
        super.tearDown();
    }
}
