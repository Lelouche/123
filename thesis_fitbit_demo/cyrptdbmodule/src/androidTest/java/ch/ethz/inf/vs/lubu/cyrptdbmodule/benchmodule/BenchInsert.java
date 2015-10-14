package ch.ethz.inf.vs.lubu.cyrptdbmodule.benchmodule;

import android.content.Context;
import android.test.InstrumentationTestCase;
import android.util.Log;

import com.google.common.base.Stopwatch;

import java.io.StringReader;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import ch.ethz.inf.vs.lubu.cyrptdbmodule.FileUtil;
import ch.ethz.inf.vs.lubu.cyrptdbmodule.R;
import ch.ethz.inf.vs.lubu.cyrptdbmodule.RandStringUtil;
import ch.ethz.inf.vs.lubu.cyrptdbmodule.exceptions.CDBException;
import ch.ethz.inf.vs.lubu.cyrptdbmodule.main.CDBStatement;
import ch.ethz.inf.vs.lubu.cyrptdbmodule.main.CryptDBConnector;
import ch.ethz.inf.vs.lubu.cyrptdbmodule.util.Setting;

/**
 * Created by lukas on 17.06.15.
 */
public class BenchInsert extends InstrumentationTestCase {

    private static final String DELIM = ";";
    private static final String NL = "\n";

    private static String TEST_NAME = "BenchInsertModule";

    private static final String IP_ADR = "192.168.2.109:8081";

    private Stopwatch watch = Stopwatch.createUnstarted();

    private CryptDBConnector connect = null;

    private static Random rand = new Random();

    private StringBuilder file = new StringBuilder();


    private static final int MAX_TEXT_DATA_LENGTH = 100;

    private static int NUMBER_OF_ROUDNS = 1000;

    public static String[] joinTags;

    static {
        int numTags = 100;
        joinTags = new String[numTags];
        for(int i=0;i<joinTags.length;i++)
            joinTags[i] = "JoinTag"+(i+1);
    }

    private static int getNumforSUM() {
        int upperbound = Integer.MAX_VALUE/NUMBER_OF_ROUDNS/100;
        int res = rand.nextInt(upperbound);
        if(rand.nextBoolean())
            res = -res;
        return res;
    }

    private void log(String name) {
        Log.v(TEST_NAME, name);
    }

    public static String getRandInsertTextTag(String table) {
        return insertQry(table, str(joinTags[rand.nextInt(joinTags.length)]));
    }

    public static String getRandInsertInt(String table) {
        return insertQry(table, String.valueOf(getNumforSUM()));
    }

    public static String getRandInsertText(String table) {
        return insertQry(table,str(RandStringUtil.getRandomNameMaxLength(MAX_TEXT_DATA_LENGTH)));
    }

    public static String insertQry(String table, String val) {
        StringBuilder sb = new StringBuilder();
        sb.append("INSERT INTO ")
                .append(table)
                .append(" VALUES (")
                .append(val)
                .append(");");
        return sb.toString();
    }

    public static String insertSUMTable(String table) {
        StringBuilder sb = new StringBuilder();
        sb.append("INSERT INTO ")
                .append(table)
                .append(" VALUES (")
                .append(str(joinTags[rand.nextInt(joinTags.length)]))
                .append(", ")
                .append(getNumforSUM())
                .append(");");
        return sb.toString();
    }

    private void performInserts(StringBuilder sb, String query) throws Exception {
        log("Perform query: "+ query);

        CDBStatement stmt = connect.createStatement();
        //Encrypted
        Setting.CRYPTO_ON = true;
        watch.start();
        stmt.executeInsert(query);
        watch.stop();
        sb.append(watch.elapsed(TimeUnit.NANOSECONDS)).append(DELIM);
        watch.reset();

        //without crypto
        Setting.CRYPTO_ON = false;
        watch.start();
        stmt.executeInsert(query);
        watch.stop();
        sb.append(watch.elapsed(TimeUnit.NANOSECONDS));
        watch.reset();
    }

    private void performInsertsWithoutTime(String query) throws Exception {
        log("Perform query: "+ query);
        CDBStatement stmt = connect.createStatement();
        //Encrypted
        Setting.CRYPTO_ON = true;
        stmt.executeInsert(query);
        //without crypto
        Setting.CRYPTO_ON = false;
        stmt.executeInsert(query);
    }

    public void testBenchInsert() throws Exception {
        file = new StringBuilder();
        file.append("InsRND_Crypt_on")
                .append(DELIM)
                .append("InsRND_Crypt_off")
                .append(DELIM)
                .append("InsDET_Crypt_on")
                .append(DELIM)
                .append("InsDET_Crypt_off")
                .append(DELIM)
                .append("InsDETjoin1_Crypt_on")
                .append(DELIM)
                .append("InsDETjoin1_Crypt_off")
                .append(DELIM)
                .append("InsDETjoin2_Crypt_on")
                .append(DELIM)
                .append("InsDETjoin2_Crypt_off")
                .append(DELIM)
                .append("InsHOM_Crypt_on")
                .append(DELIM)
                .append("InsHOM_Crypt_off")
                .append(DELIM)
                .append("InsOPE_Crypt_on")
                .append(DELIM)
                .append("InsOPE_Crypt_off")
                .append(NL);

        for(int i=0; i<NUMBER_OF_ROUDNS; i++) {
            performInserts(file, getRandInsertText("TabRND"));
            file.append(DELIM);
            performInserts(file, getRandInsertTextTag("TabDET"));
            file.append(DELIM);
            performInserts(file, getRandInsertTextTag("TabDETjoin1"));
            file.append(DELIM);
            performInserts(file, getRandInsertTextTag("TabDETjoin2"));
            file.append(DELIM);
            performInserts(file, getRandInsertInt("TabHOM"));
            file.append(DELIM);
            performInserts(file, getRandInsertInt("TabOPE"));
            file.append(NL);

            performInsertsWithoutTime(insertSUMTable("TabBenchSum"));
        }

        /*FileUtil fu = new FileUtil(TEST_NAME + ".csv");
        fu.writeToFile(file.toString());*/
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
    }

    @Override
    public void tearDown() throws Exception {
        super.tearDown();
    }

    private static String str(String in) {
        return "'"+in+"'";
    }

}
