package ch.ethz.inf.vs.lubu.cyrptdbmodule.benchmark;

import android.util.Log;

import com.google.common.base.Stopwatch;

import junit.framework.TestCase;

import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

import ch.ethz.inf.vs.lubu.cyrptdbmodule.FileUtil;
import ch.ethz.inf.vs.lubu.cyrptdbmodule.cryptoalg.BasicCrypto;

/**
 * Created by lukas on 26.03.15.
 */
public class BenchBFCBC extends TestCase {

    private static final String TEST_NAME = "BenchBFCBC";
    private static final String DELIM = ";";
    private static final String NL = "\n";

    private static final int NUM_IT = 1000;

    private StringBuilder sb = new StringBuilder();

    private Random rm = new Random();

    private Stopwatch stopwatch = Stopwatch.createUnstarted();

    private byte[] key;
    private byte[] curIV;

    public BenchBFCBC() {
        super(TEST_NAME);
    }

    private void log(String name) {
        Log.i(TEST_NAME, name);
    }


    private void smallTestPart() throws Exception {
        //byte[] plain = new byte[size];
        int plainInt = rm.nextInt();

        byte[] plain = BigInteger.valueOf(plainInt).toByteArray();

        stopwatch.start();
        byte[] cipher = BasicCrypto.encrypt_BLOWFISH_CBC(plain, key, curIV, true);
        stopwatch.stop();
        sb.append(stopwatch.elapsed(TimeUnit.NANOSECONDS)).append(DELIM);
        stopwatch.reset();

        stopwatch.start();
        byte[] decrypt = BasicCrypto.decrypt_BLOWFISH_CBC(cipher, key, curIV, true);
        stopwatch.stop();
        sb.append(stopwatch.elapsed(TimeUnit.NANOSECONDS)).append(DELIM);
        stopwatch.reset();

        curIV = generateIV();

        assertEquals(new String(plain), new String(decrypt));
    }

    public void testAll() throws Exception {
        for (int i = 0; i < NUM_IT; i++) {
            smallTestPart();
            sb.deleteCharAt(sb.length() - 1);
            sb.append(NL);
        }
    }

    private void addCollums() {
        sb.append("BF_ENC").append(DELIM).append("BF_DEC").append(NL);
    }

    private byte[] generateKey() {
        KeyGenerator keyGen = null;
        try {
            keyGen = KeyGenerator.getInstance("AES");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        keyGen.init(64);
        SecretKey secretKey = keyGen.generateKey();
        return secretKey.getEncoded();
    }

    private byte[] generateIV() {
        SecureRandom sr = new SecureRandom();
        byte[] iv = new byte[64 / 8];
        sr.nextBytes(iv);
        return iv;
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        addCollums();
        key = generateKey();
        curIV = generateIV();
    }

    @Override
    public void tearDown() throws Exception {
        super.tearDown();
        FileUtil fu = new FileUtil(TEST_NAME + ".csv");
        fu.writeToFile(sb.toString());
    }
}
