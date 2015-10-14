package ch.ethz.inf.vs.lubu.cyrptdbmodule.benchmark;

import android.test.InstrumentationTestCase;
import android.util.Log;

import com.google.common.base.Stopwatch;

import java.io.StringReader;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import ch.ethz.inf.vs.lubu.cyrptdbmodule.FileUtil;
import ch.ethz.inf.vs.lubu.cyrptdbmodule.R;
import ch.ethz.inf.vs.lubu.cyrptdbmodule.exceptions.CDBException;
import ch.ethz.inf.vs.lubu.cyrptdbmodule.main.CDBStatement;
import ch.ethz.inf.vs.lubu.cyrptdbmodule.main.CryptDBConnector;

/**
 * Created by lukas on 08.06.15.
 */
public class BenchmOPE extends InstrumentationTestCase {

    private static String TEST_NAME = "BenchmOPE";

    private static final String IP_ADR = "192.168.1.27:8081";

    private CryptDBConnector connect = null;

    private StringBuilder sb = new StringBuilder();

    private Stopwatch watch = Stopwatch.createUnstarted();

    private Random rand = new Random();

    private static final int MAX_IT = 1000;

    private void log(String name) {
        Log.v(TEST_NAME, name);
    }

    private String insertTabOpe(int num ) {
        return "INSERT INTO BenchOPE VALUES (" + String.valueOf(num) + ");";
    }

    public void testBench() throws Exception {
        for(int i=0; i<MAX_IT; i++) {
            int curInt = rand.nextInt();
            partInsert(curInt);
            if(i%50==0) {
                log("Percentage Done: " + String.valueOf((int)(((float)i)/((float)MAX_IT) * 100)) +"%");
            }
        }
    }

    private void partInsert(int num) throws CDBException {
        CDBStatement stmt = connect.createStatement();
        String query = insertTabOpe(num);
        watch.start();
        stmt.executeInsert(query);
        watch.stop();
        sb.append(watch.elapsed(TimeUnit.NANOSECONDS))
            .append(";\n");
        watch.reset();
    }


    private void addHeader() {
        sb.append("mOPE_INSERT_TIME;\n");
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        if(connect == null) {
            addHeader();
            StringReader sr = new StringReader((getInstrumentation().getTargetContext().getText(R.string.TestScheme)).toString());
            connect = new CryptDBConnector(sr,
                    this.getInstrumentation().getContext(),
                    "http://" + IP_ADR,
                    "root",
                    "letmein");
        }
    }

    @Override
    public void tearDown() throws Exception {
        super.tearDown();
        FileUtil fu = new FileUtil(TEST_NAME +"_START10000"+ ".csv");
        fu.writeToFile(sb.toString());
    }
}
