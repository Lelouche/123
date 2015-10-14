package ch.ethz.inf.vs.lubu.cyrptdbmodule.cryptoalgtest;

import android.util.Log;

import junit.framework.TestCase;

import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

import ch.ethz.inf.vs.lubu.cyrptdbmodule.cryptoalg.PRNGAesBlock;
import ch.ethz.inf.vs.lubu.cyrptdbmodule.cryptoalg.PRNGImpl;
import ch.ethz.inf.vs.lubu.cyrptdbmodule.util.CDBUtil;

/**
 * Created by lukas on 17.03.15.
 */
public class TestBigInteger extends TestCase {

    private static String TEST_NAME = "TestBigInteger";

    public TestBigInteger() {
        super(TEST_NAME);
    }

    private void log(String name) {
        Log.v(TEST_NAME, name);
    }


    public void testTest1() throws Exception {
        int nbits = 128;
        PRNGImpl rand = new PRNGImpl();
        BigInteger a = rand.getRandomNumber(nbits);
        int actual = a.bitLength();
        assertEquals(actual, nbits);
    }

    public void testTest2() throws Exception {
        int nbits = 128;
        PRNGAesBlock rand = new PRNGAesBlock(generateKey());
        BigInteger a = rand.getRandomNumber(nbits);
        BigInteger b = rand.getRandMod(new BigInteger("10000000000000000000000"));
        BigInteger c = rand.getRandPrime(128);


    }

    public void testTest3() throws Exception {
        BigInteger num = BigInteger.valueOf(-234234);
        byte[] bytes = num.toByteArray();
        BigInteger num2 = new BigInteger(bytes);
        log(num.toString() + ", " + num2.toString());
        assertEquals(num.toString(), num2.toString());
    }

    public void testTest4() throws Exception {
        BigInteger x = new BigInteger("29726271854653250078653436661872325557");
        byte[] bytes = x.toByteArray();
        log(String.valueOf(bytes.length));
    }

    public void testTest5() throws Exception {
        String hex1 = "0098182befc75f78db00f7c0c5354114531bb7c368a0a00d36cc26075428901abe2a073b71956776f9935905f027e445be99a6e49933ab8281a97857218ed6be4f2ee21529aef22ef1dfcf09121f7cb5b9fa5d9e6945fb7ea8e046b0b284d5074f231c3eea423250947d4244a0e13f97da1ae7837e96d4e0f7418c3b6ae663ce0bbe86fe27cbbac5d1f82ba4cec3a637b9d0ff39a68957a4e6ef054a76c63c0893ed3cd2af1ee703e311457edb9899672a81e697a5441ae9d4e819daa641b0dbc947ed8ccfc921915fccf3cf0ad7162bedfbd9e82ed44ec3fbeac26a17b903e403ed1b73d0a8d69717bc69866797769d4560052ba6b2361c682113f99ff18dfe49";
        String hex2 = "0098182BEFC75F78DB00F7C0C5354114531BB7C368A0A00D36CC26075428901ABE2A073B71956776F9935905F027E445BE99A6E49933AB8281A97857218ED6BE4F2EE21529AEF22EF1DFCF09121F7CB5B9FA5D9E6945FB7EA8E046B0B284D5074F231C3EEA423250947D4244A0E13F97DA1AE7837E96D4E0F7418C3B6AE663CE0BBE86FE27CBBAC5D1F82BA4CEC3A637B9D0FF39A68957A4E6EF054A76C63C0893ED3CD2AF1EE703E311457EDB9899672A81E697A5441AE9D4E819DAA641B0DBC947ED8CCFC921915FCCF3CF0AD7162BEDFBD9E82ED44EC3FBEAC26A17B903E403ED1B73D0A8D69717BC69866797769D4560052BA6B2361C682113F99FF18DFE49";
        BigInteger first = new BigInteger(CDBUtil.HexToBytes(hex1));
        BigInteger second = new BigInteger(CDBUtil.HexToBytes(hex2));
        assertEquals(first.toString(), second.toString());
        ;
    }

    public void testTest6() throws Exception {
        BigInteger in = new BigInteger("3456345634576234123451345234523452354523452435");
        String hex = CDBUtil.bytesToHex(in.toByteArray());
        BigInteger res = new BigInteger(CDBUtil.HexToBytes(hex));
        assertEquals(in.toString(), res.toString());
        ;
    }

    public void testTest7() throws Exception {
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
    }

    @Override
    public void tearDown() throws Exception {
        super.tearDown();
    }
}
