package ch.ethz.inf.vs.lubu.cyrptdbmodule;

import android.app.Instrumentation;
import android.test.InstrumentationTestCase;
import android.util.Log;

import com.google.common.base.Stopwatch;

import java.math.BigInteger;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import ch.ethz.inf.vs.lubu.cyrptdbmodule.cryptoalg.ECELGamal;
import ch.ethz.inf.vs.lubu.cyrptdbmodule.cryptoalg.ECELGamalPriv;
import ch.ethz.inf.vs.lubu.cyrptdbmodule.cryptoalg.FastECElGamal;
import ch.ethz.inf.vs.lubu.cyrptdbmodule.cryptoalg.IPRNG;
import ch.ethz.inf.vs.lubu.cyrptdbmodule.cryptoalg.PRNGImpl;

/**
 * Created by lukas on 23.06.15.
 */
public class TableFileCreator extends InstrumentationTestCase {

    private static final String DELIM = ";";
    private static final String NL = "\n";

    private static final String TEST_NAME = "TableFileCreator";

    private static final String TableFileName = "table2pow19_20";

    private static final int fromPow2 = 19;

    private static final int toPow2 = 20;

    public TableFileCreator() {

    }

    private void log(String name) {
        Log.v(TEST_NAME, name);
    }


    public void testCreateTable() throws Exception {
        StringBuilder sb = new StringBuilder();
        int from = 1 << fromPow2;
        int to = 1 << toPow2;
        int length = to-from;

        for(int i=from+1; i<=to; i++) {
            String val = String.valueOf(i);
            sb.append(FastECElGamal.computeGenTimes(val)).append(NL);
            if(i%1000 == 0) {
                double perc = ((double)(i-from))/((double)length);
                log("Percentage Done: " + String.valueOf(perc*100)+"%");
            }
        }
        FileUtil fu = new FileUtil(TableFileName);
        fu.writeToFile(sb.toString());
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
