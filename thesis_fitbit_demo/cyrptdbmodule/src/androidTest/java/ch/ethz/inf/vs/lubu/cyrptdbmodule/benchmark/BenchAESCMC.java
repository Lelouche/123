package ch.ethz.inf.vs.lubu.cyrptdbmodule.benchmark;

import android.util.Log;

import com.google.common.base.Stopwatch;

import junit.framework.TestCase;

import java.security.NoSuchAlgorithmException;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

import ch.ethz.inf.vs.lubu.cyrptdbmodule.cryptoalg.BasicCrypto;

/**
 * Created by lukas on 26.03.15.
 */
public class BenchAESCMC extends TestCase {

    private static final String TEST_NAME = "BenchAESCMC";
    private static final String DELIM = ";";
    private static final String NL = "\n";

    private static final int NUM_IT = 1000;

    private StringBuilder sb = new StringBuilder();

    private Random rm = new Random();

    private Stopwatch stopwatch = Stopwatch.createUnstarted();

    private byte[] key;
    private byte[] curIV;

    public BenchAESCMC() {
        super(TEST_NAME);
    }

    private void log(String name) {
        Log.i(TEST_NAME, name);
    }


    private void smallTestPart(int size) throws Exception {
        byte[] plain = new byte[size];
        rm.nextBytes(plain);

        stopwatch.start();
        byte[] cipher = BasicCrypto.encrypt_AES_CMC(plain, key, curIV);
        stopwatch.stop();
        sb.append(stopwatch.elapsed(TimeUnit.NANOSECONDS)).append(DELIM);
        stopwatch.reset();

        stopwatch.start();
        byte[] decrypt = BasicCrypto.decrypt_AES_CMC(cipher, key, curIV);
        stopwatch.stop();
        sb.append(stopwatch.elapsed(TimeUnit.NANOSECONDS)).append(DELIM);
        stopwatch.reset();

        assertEquals(new String(plain), new String(decrypt));
    }

    public void testAll() throws Exception {
        for (int i = 0; i < NUM_IT; i++) {
            smallTestPart(16);
            smallTestPart(32);
            smallTestPart(64);
            smallTestPart(128);
            sb.deleteCharAt(sb.length() - 1);
            sb.append(NL);
        }
    }

    private void addCollums() {
        sb.append("DET_AES_ENC_16").append(DELIM).append("DET_AES_DEC_16").append(DELIM);
        sb.append("DET_AES_ENC_32").append(DELIM).append("DET_AES_DEC_32").append(DELIM);
        sb.append("DET_AES_ENC_64").append(DELIM).append("DET_AES_DEC_64").append(DELIM);
        sb.append("DET_AES_ENC_128").append(DELIM).append("DET_AES_DEC_128").append(NL);
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

    private byte[] generateEmptyIV() {
        byte[] iv = new byte[128 / 8];
        for (int i = 0; i < iv.length; i++)
            iv[i] = 0;
        return iv;
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        addCollums();
        key = generateKey();
        curIV = generateEmptyIV();
    }

    @Override
    public void tearDown() throws Exception {
        super.tearDown();
        //FileUtil fu = new FileUtil(TEST_NAME+".csv");
        //fu.writeToFile(sb.toString());
    }
}
