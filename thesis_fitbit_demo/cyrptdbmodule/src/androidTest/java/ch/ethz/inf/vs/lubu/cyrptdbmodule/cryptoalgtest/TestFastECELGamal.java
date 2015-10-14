package ch.ethz.inf.vs.lubu.cyrptdbmodule.cryptoalgtest;

import android.test.InstrumentationTestCase;
import android.util.Log;

import java.math.BigInteger;
import java.util.Random;

import ch.ethz.inf.vs.lubu.cyrptdbmodule.cryptoalg.FastECElGamal;
import ch.ethz.inf.vs.lubu.cyrptdbmodule.cryptoalg.NativeECElGamal;
import ch.ethz.inf.vs.lubu.cyrptdbmodule.cryptoalg.NativeECElGamalCrypto;
import ch.ethz.inf.vs.lubu.cyrptdbmodule.cryptoalg.PRNGImpl;
import ch.ethz.inf.vs.lubu.cyrptdbmodule.util.ContextHolder;

/**
 * Created by lukas on 05.04.15.
 */
public class TestFastECELGamal extends InstrumentationTestCase {

    private static String TEST_NAME = "TestFastECELGamal";

    private Random rand = new Random();

    private FastECElGamal nec = null;

    private static final int MAX_INT = Integer.MAX_VALUE;

    private static final int MAX_IT = 10;

    public TestFastECELGamal() {

    }

    private void log(String name) {
        Log.v(TEST_NAME, name);
    }


    public void testTest1() throws Exception {
        BigInteger plain = BigInteger.valueOf(234234);
        NativeECElGamalCrypto.NativeECElgamalCipher cipher = nec.encrypt(plain);
        BigInteger res = nec.decrypt(cipher,4);
        assertEquals(res.toString(), plain.toString());
    }

    public void testTest2() throws Exception {
        for (int i = 0; i < MAX_IT; i++) {
            BigInteger cur = BigInteger.valueOf(rand.nextInt());
            partTest(cur);
        }
    }

    public void testTest3() throws Exception {
        FastECElGamal.TableLoader.freeTables();
        FastECElGamal.tearDOWN();
    }

    private void partTest(BigInteger plain) throws Exception {
        NativeECElGamal.NativeECElgamalCipher cipher = nec.encrypt(plain);
        BigInteger res = nec.decrypt(cipher,4);
        assertEquals(res.toString(), plain.toString());
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        if(nec==null) {
            ContextHolder.setContext(this.getInstrumentation().getContext());
            nec = new FastECElGamal(FastECElGamal.generateKeys(new PRNGImpl()), new PRNGImpl());
        }
    }

    @Override
    public void tearDown() throws Exception {
        //FastECElGamal.TableLoader.freeTables();
        //FastECElGamal.tearDOWN();
        super.tearDown();
    }
}
