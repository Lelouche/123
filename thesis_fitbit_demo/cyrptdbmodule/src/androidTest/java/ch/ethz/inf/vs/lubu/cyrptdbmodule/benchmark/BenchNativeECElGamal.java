package ch.ethz.inf.vs.lubu.cyrptdbmodule.benchmark;

import android.test.InstrumentationTestCase;
import android.util.Log;

import com.google.common.base.Stopwatch;

import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

import ch.ethz.inf.vs.lubu.cyrptdbmodule.FileUtil;
import ch.ethz.inf.vs.lubu.cyrptdbmodule.cryptoalg.IPRNG;
import ch.ethz.inf.vs.lubu.cyrptdbmodule.cryptoalg.NativeECElGamal;
import ch.ethz.inf.vs.lubu.cyrptdbmodule.cryptoalg.PRNGRC4Stream;
import ch.ethz.inf.vs.lubu.cyrptdbmodule.util.ContextHolder;

/**
 * Created by lukas on 20.04.15.
 */
public class BenchNativeECElGamal extends InstrumentationTestCase {

    private static final String DELIM = ";";
    private static final String NL = "\n";

    private static final boolean LOG_ON = true;

    private static final String TEST_NAME = "BenchNativeECElGamal";

    private static final int NUM_IT = 1000;

    private Random rm = new Random();

    private NativeECElGamal gamal;

    private StringBuilder sb = new StringBuilder();

    private Stopwatch watch = Stopwatch.createUnstarted();


    private void log(String name) {
        if (LOG_ON)
            Log.v(TEST_NAME, name);
    }

    public int getRandRanged(int range) {
        int i = rm.nextInt(range);
        if (rm.nextBoolean()) {
            i = -i;
        }
        return i;
    }

    public void parttestECElgamal() throws Exception {

        int a = rm.nextInt();
        BigInteger A = BigInteger.valueOf(a);
        log("Num: " + A);

        watch.start();
        NativeECElGamal.NativeECElgamalCipher cipherA = gamal.encrypt(A);
        watch.stop();
        sb.append(watch.elapsed(TimeUnit.NANOSECONDS)).append(DELIM);
        log("EncTime: " + watch.elapsed(TimeUnit.MILLISECONDS));
        watch.reset();

        watch.start();
        BigInteger res = gamal.decrypt(cipherA,4);
        watch.stop();
        sb.append(watch.elapsed(TimeUnit.NANOSECONDS)).append(NL);
        log("DecTime: " + watch.elapsed(TimeUnit.MILLISECONDS));
        watch.reset();


        assertEquals(res.toString(), A.toString());
    }

    public void testBench() throws Exception {
        sb.append("Gamal_Enc").append(DELIM).append("Gamal_Dec").append(NL);
        sb = new StringBuilder();
        for (int i = 0; i < NUM_IT; i++) {
            log("---------" + (i + 1) + "---------");
            parttestECElgamal();
        }
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
        ContextHolder.setContext(this.getInstrumentation().getContext());
        IPRNG rand = new PRNGRC4Stream(generateKey());
        gamal = new NativeECElGamal(NativeECElGamal.generateKeys(rand), rand);
        parttestECElgamal();
        parttestECElgamal();
    }

    @Override
    public void tearDown() throws Exception {
        super.tearDown();
        FileUtil fu = new FileUtil(TEST_NAME + ".csv");
        fu.writeToFile(sb.toString());

    }

}
