package ch.ethz.inf.vs.lubu.cyrptdbmodule.cryptoalgtest;

import android.util.Base64;
import android.util.Log;

import junit.framework.TestCase;

import java.security.NoSuchAlgorithmException;
import java.util.Random;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

import ch.ethz.inf.vs.lubu.cyrptdbmodule.cryptoalg.BasicCrypto;

/**
 * Created by lukas on 28.03.15.
 */
public class TestBasicCrypto extends TestCase {

    private static String TEST_NAME = "TestBasicCrypto";

    private byte[] key = null;

    private static final int MAX_IT = 1000;

    private Random rand = new Random();

    public TestBasicCrypto() {
        super(TEST_NAME);
    }

    private void log(String name) {
        Log.v(TEST_NAME, name);
    }


    public void testTest1() throws Exception {
        byte[] plain = new byte[128 / 8];
        rand.nextBytes(plain);
        byte[] cipher = BasicCrypto.encrypt_AES(plain, key);
        byte[] decrypt = BasicCrypto.decrypt_AES(cipher, key);
        checkArrayEqual(plain, decrypt);
    }

    public void testTest3() throws Exception {
        String plain = "Dies ist ein sehr langer Text, der in die Dataenbank kommt";
        byte[] plainb = plain.getBytes("UTF-8");
        byte[] iv = new byte[128 / 8];
        byte[] cipher = BasicCrypto.encrypt_AES_CMC(plainb, key, iv);
        String encoded = Base64.encodeToString(cipher, android.util.Base64.NO_WRAP);
        byte[] decoded = Base64.decode(encoded, Base64.NO_WRAP);
        checkArrayEqual(cipher, decoded);
        byte[] decrypt = BasicCrypto.decrypt_AES_CMC(decoded, key, iv);
        checkArrayEqual(plainb, decrypt);
    }

    private void checkArrayEqual(byte[] a, byte[] b) {
        for (int i = 0; i < a.length; i++) {
            assertEquals(a[i], b[i]);
        }
    }

    public void testTest2() throws Exception {
        for (int i = 0; i < MAX_IT; i++) {
            testTest1();
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
        this.key = generateKey();
    }

    @Override
    public void tearDown() throws Exception {
        super.tearDown();
    }
}
