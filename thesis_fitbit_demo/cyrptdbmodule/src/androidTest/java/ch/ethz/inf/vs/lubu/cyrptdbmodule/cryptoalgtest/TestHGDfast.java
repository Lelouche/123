package ch.ethz.inf.vs.lubu.cyrptdbmodule.cryptoalgtest;

import android.util.Log;

import junit.framework.TestCase;

import org.apfloat.Apfloat;
import org.apfloat.ApfloatMath;

import java.math.BigInteger;

import ch.ethz.inf.vs.lubu.cyrptdbmodule.cryptoalg.HypGeoDistFast;
import ch.ethz.inf.vs.lubu.cyrptdbmodule.cryptoalg.PRNGImpl;

/**
 * Created by lukas on 23.03.15.
 */
public class TestHGDfast extends TestCase {
    private static String TEST_NAME = "HGDfastTest";

    public TestHGDfast() {
        super(TEST_NAME);
    }

    private HypGeoDistFast ft = new HypGeoDistFast();

    private void log(String name) {
        Log.v(TEST_NAME, name);
    }


    public void testTest1() throws Exception {
        BigInteger a, b, c;
        PRNGImpl rnd = new PRNGImpl();
        a = new BigInteger("10000000000");
        b = new BigInteger("15000000000");
        c = new BigInteger("10000000000");
        log("Values: " + a.toString() + ", " + b.toString() + ", " + c.toString());
        BigInteger res = ft.HGD(a, b, c, rnd);
        log("Res: " + res.toString());
        assertTrue(res.compareTo(b) <= 0 && res.compareTo(BigInteger.ZERO) > 0);
    }

    public void testTest2() throws Exception {
        BigInteger a, b, c;
        PRNGImpl rnd = new PRNGImpl();
        a = new BigInteger("10");
        b = new BigInteger("15");
        c = new BigInteger("10");
        log("Values: " + a.toString() + ", " + b.toString() + ", " + c.toString());
        BigInteger res = ft.HGD(a, b, c, rnd);
        log("Res: " + res.toString());
        assertTrue(res.compareTo(b) <= 0 && res.compareTo(BigInteger.ZERO) > 0);
    }

    public void testTest3() throws Exception {
        Apfloat a = new Apfloat(1000000, 10);
        Apfloat b = ApfloatMath.log(a);
        String c = b.toString(true);
        String x = b.toString(false);
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
