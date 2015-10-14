package ch.ethz.inf.vs.lubu.cyrptdbmodule.cryptoalgtest;

import android.util.Log;

import com.google.common.base.Stopwatch;

import junit.framework.TestCase;

import java.math.BigInteger;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import ch.ethz.inf.vs.lubu.cyrptdbmodule.cryptoalg.ECELGamal;
import ch.ethz.inf.vs.lubu.cyrptdbmodule.cryptoalg.ECELGamalPriv;
import ch.ethz.inf.vs.lubu.cyrptdbmodule.cryptoalg.IPRNG;
import ch.ethz.inf.vs.lubu.cyrptdbmodule.cryptoalg.PRNGImpl;

/**
 * Created by lukas on 17.03.15.
 */
public class TestECElGamal extends TestCase {

    private static final String TEST_NAME = "ECElGamalTest";

    private Random rm = new Random();

    private ECELGamalPriv gamal;

    private Stopwatch watch = Stopwatch.createUnstarted();

    public TestECElGamal() {
        super(TEST_NAME);
    }

    private void log(String name) {
        Log.v(TEST_NAME, name);
    }


    public void testECElgamal() throws Exception {

        int a = 1000;
        int b = -999;
        BigInteger A = BigInteger.valueOf(a);
        BigInteger B = BigInteger.valueOf(b);

        watch.start();
        ECELGamal.ELGamalCipher cipherA = gamal.encrypt(A);
        watch.stop();
        log("EncTime: " + watch.elapsed(TimeUnit.MILLISECONDS));
        watch.reset();

        ECELGamal.ELGamalCipher cipherB = gamal.encrypt(B);
        ECELGamal.ELGamalCipher cipherC = cipherA.add(cipherB);

        watch.start();
        BigInteger decryptA = gamal.decrypt(cipherA, Integer.MAX_VALUE);
        watch.stop();
        log("DecTime: " + watch.elapsed(TimeUnit.MILLISECONDS));
        watch.reset();
        BigInteger decryptB = gamal.decrypt(cipherB, Integer.MAX_VALUE);
        BigInteger decryptC = gamal.decrypt(cipherC, Integer.MAX_VALUE);

        assertEquals(A, decryptA);
        assertEquals(B, decryptB);
        assertEquals(decryptC, BigInteger.valueOf(a + b));
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

    }
}
