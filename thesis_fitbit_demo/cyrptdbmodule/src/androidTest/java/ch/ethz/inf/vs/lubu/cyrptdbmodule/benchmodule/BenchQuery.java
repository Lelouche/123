package ch.ethz.inf.vs.lubu.cyrptdbmodule.benchmodule;

import android.content.Context;
import android.test.InstrumentationTestCase;
import android.util.Log;

import com.google.common.base.Stopwatch;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import ch.ethz.inf.vs.lubu.cyrptdbmodule.FileUtil;
import ch.ethz.inf.vs.lubu.cyrptdbmodule.R;
import ch.ethz.inf.vs.lubu.cyrptdbmodule.exceptions.CDBException;
import ch.ethz.inf.vs.lubu.cyrptdbmodule.main.CDBResultSet;
import ch.ethz.inf.vs.lubu.cyrptdbmodule.main.CDBStatement;
import ch.ethz.inf.vs.lubu.cyrptdbmodule.main.CryptDBConnector;
import ch.ethz.inf.vs.lubu.cyrptdbmodule.util.Setting;

/**
 * Created by lukas on 17.06.15.
 */
public class BenchQuery extends InstrumentationTestCase {

    private static final String DELIM = ";";
    private static final String NL = "\n";

    private static String TEST_NAME = "BenchQueryModule";

    private static final String IP_ADR = "192.168.2.109:8081";

    private Stopwatch watch = Stopwatch.createUnstarted();

    private CryptDBConnector connect = null;

    private static Random rand = new Random();

    private StringBuilder file = new StringBuilder();

    private static int MAX_ITERATIONS = 10;

    private static int limit = 5;


    private void log(String name) {
        Log.v(TEST_NAME, name);
    }

    private int count = 0;

    public static String getQueryRand(String tab, String col) {
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT ")
                .append(col)
                .append(" FROM ")
                .append(tab)
                .append(" LIMIT ")
                .append(limit)
                .append(";");
        return sb.toString();
    }

    public static String getQueryEquality(String tab, String col) {
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT ")
                .append(col)
                .append(" FROM ")
                .append(tab)
                .append(" WHERE ")
                .append(col)
                .append(" = ")
                .append(str(BenchInsert.joinTags[rand.nextInt(BenchInsert.joinTags.length)]))
                .append(" LIMIT ")
                .append(limit)
                .append(";");
        return sb.toString();
    }

    public static String getQueryJOIN(String tab1, String col1, String tab2, String col2) {
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT ")
                .append(col1)
                .append(", ")
                .append(col2)
                .append(" FROM ")
                .append(tab1)
                .append(" JOIN ")
                .append(tab2)
                .append(" ON ")
                .append(col1)
                .append(" = ")
                .append(col2)
                .append(" LIMIT ")
                .append(limit)
                .append(";");
        return sb.toString();
    }

    public static String getQueryRange(String tab, String col) {
        int i1,i2, temp;
        i1 = rand.nextInt();
        i2 = rand.nextInt();
        if(i1>i2) {
            temp = i1;
            i1 = i2;
            i2 = temp;
        }
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT ")
                .append(col)
                .append(" FROM ")
                .append(tab)
                .append(" WHERE ")
                .append(col)
                .append(" >= ")
                .append(i1)
                .append(" AND ")
                .append(col)
                .append(" <= ")
                .append(i2)
                .append(" LIMIT ")
                .append(limit)
                .append(";");
        return sb.toString();
    }

    public static String getQuerySUM(String tab, String col, String colTag) {
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT SUM(")
                .append(col)
                .append(")")
                .append(" FROM ")
                .append(tab)
                .append(" WHERE ")
                .append(colTag)
                .append(" = ")
                .append(str(BenchInsert.joinTags[rand.nextInt(BenchInsert.joinTags.length)]))
                .append(" LIMIT ")
                .append(limit)
                .append(";");
        return sb.toString();
    }

    private void performQueries(StringBuilder sb, String query) throws Exception {

        boolean hasResEnc = false;
        boolean hasResPlain = false;
        ArrayList<String> enc = new ArrayList<>(10);
        ArrayList<String> plain = new ArrayList<>(10);
        CDBStatement stmt = connect.createStatement();
        //Encrypted
        Setting.CRYPTO_ON = true;
        watch.start();
        CDBResultSet encRes = stmt.executeQuery(query);
        while(encRes.next()) {
            hasResEnc = true;
            for(int i=0; i<encRes.getNumCols();i++) {
                enc.add(encRes.getString(i));
            }
        }
        watch.stop();
        sb.append(watch.elapsed(TimeUnit.NANOSECONDS)).append(DELIM);
        watch.reset();

        //without crypto
        Setting.CRYPTO_ON = false;
        watch.start();
        CDBResultSet decRes = stmt.executeQuery(query);
        while(decRes.next()) {
            hasResPlain = true;
            for(int i=0; i<decRes.getNumCols();i++) {
                plain.add(decRes.getString(i));
            }
        }
        watch.stop();
        sb.append(watch.elapsed(TimeUnit.NANOSECONDS));
        watch.reset();

        if(!enc.isEmpty() && !plain.isEmpty())
            log("Enc: " + enc.get(0)+ " Dec: "+ plain.get(0));
        else
            log("Enc: empty Dec: empty");

        assertFalse(hasResPlain ^ hasResEnc);
        for(int i=0; i<plain.size();i++) {
            //assertEquals(enc.get(i),plain.get(i))
            if(!enc.contains(plain.get(i)))
                assertFalse(true);
            if(!plain.contains(enc.get(i)))
                assertFalse(true);
        }
    }

    public void testBenchQuery() throws Exception {
        file = new StringBuilder();
        file.append("RND_ALL_Crypt_on")
                .append(DELIM)
                .append("RND_ALL_off")
                .append(DELIM)
                .append("DET_EQ_Crypt_on")
                .append(DELIM)
                .append("DET_EQ_off")
                .append(DELIM)
                .append("DET_JOIN_Crypt_on")
                .append(DELIM)
                .append("DET_JOIN_off")
                .append(DELIM)
                .append("OPE_RANGE_Crypt_on")
                .append(DELIM)
                .append("OPE_RANGE_off")
                .append(DELIM)
                .append("HOM_SUM_Crypt_on")
                .append(DELIM)
                .append("HOM_SUM_off")
                .append(NL);

        for(int i=0; i<MAX_ITERATIONS; i++) {
            performQueries(file, getQueryRand("TabRND","TabRND.colRND"));
            file.append(DELIM);
            performQueries(file, getQueryEquality("TabDET","TabDET.colDET"));
            file.append(DELIM);
            performQueries(file, getQueryJOIN("TabDETjoin1","TabDETjoin1.colJoin1","TabDETjoin2","TabDETjoin2.colJoin2"));
            file.append(DELIM);
            performQueries(file, getQueryRange("TabOPE","TabOPE.colOPE"));
            file.append(DELIM);
            performQueries(file, getQuerySUM("TabBenchSum","TabBenchSum.colSUM", "TabBenchSum.colTag"));
            file.append(NL);
        }

        /*FileUtil ful = new FileUtil(TEST_NAME+".csv");
        ful.writeToFile(file.toString());*/
    }

    private void performWarmup() throws Exception {
        CDBStatement stmt = connect.createStatement();
        Setting.CRYPTO_ON = true;
        stmt.executeQuery(getQuerySUM("TabBenchSum","TabBenchSum.colSUM", "TabBenchSum.colTag"));
    }


    @Override
    public void setUp() throws Exception {
        super.setUp();
        Context con = getInstrumentation().getTargetContext();
        StringReader reader = new StringReader((con.getText(R.string.bench_scheme)).toString());
        try {
            connect = new CryptDBConnector(reader, con,
                    "http://" + IP_ADR,
                    "root",
                    "letmein");
        } catch (CDBException e) {
            e.printStackTrace();
        }

        performWarmup();

    }

    @Override
    public void tearDown() throws Exception {
        super.tearDown();
    }

    private static String str(String in) {
        return "'"+in+"'";
    }

}
