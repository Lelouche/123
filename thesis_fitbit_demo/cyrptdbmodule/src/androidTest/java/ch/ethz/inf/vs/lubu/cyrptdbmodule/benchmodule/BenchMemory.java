package ch.ethz.inf.vs.lubu.cyrptdbmodule.benchmodule;

import android.content.Context;
import android.test.InstrumentationTestCase;

import com.google.common.base.Stopwatch;

import java.io.StringReader;
import java.util.Random;

import ch.ethz.inf.vs.lubu.cyrptdbmodule.R;
import ch.ethz.inf.vs.lubu.cyrptdbmodule.cryptoalg.FastECElGamal;
import ch.ethz.inf.vs.lubu.cyrptdbmodule.exceptions.CDBException;
import ch.ethz.inf.vs.lubu.cyrptdbmodule.main.CDBResultSet;
import ch.ethz.inf.vs.lubu.cyrptdbmodule.main.CDBStatement;
import ch.ethz.inf.vs.lubu.cyrptdbmodule.main.CryptDBConnector;
import ch.ethz.inf.vs.lubu.cyrptdbmodule.util.ContextHolder;
import ch.ethz.inf.vs.lubu.cyrptdbmodule.util.Setting;

/**
 * Created by lukas on 21.06.15.
 */
public class BenchMemory extends InstrumentationTestCase {

    private static final String DELIM = ";";
    private static final String NL = "\n";

    private static String TEST_NAME = "BenchMemory";

    private static final String IP_ADR = "192.168.2.109:8081";

    private Stopwatch watch = Stopwatch.createUnstarted();

    private CryptDBConnector connect = null;

    private Random rand = new Random();

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

    public String getQueryRange(String tab, String col) {
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
                .append(5)
                .append(";");
        return sb.toString();
    }

    private void loadTable() throws Exception {
        ContextHolder.setContext(getInstrumentation().getTargetContext());
        FastECElGamal.loadLookUpTable();
    }

    // Memory measured with allocation tracker provided by the android sdk
    public void testMemoryCons() throws Exception {
        file = new StringBuilder();
        file.append("MemModule")
                .append(DELIM)
                .append(NL);
        //loadTable();

        Thread.sleep(10000);

        Setting.CRYPTO_ON = true;
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

        CDBStatement statement = connect.createStatement();

        //CDBResultSet result = statement.executeQuery(BenchQuery.getQueryEquality("TabDET", "TabDET.colDET"));
        //CDBResultSet result = statement.executeQuery(BenchQuery.getQueryRange("TabOPE", "TabOPE.colOPE"));
        //CDBResultSet result = statement.executeQuery(BenchQuery.getQueryJOIN("TabDETjoin1", "TabDETjoin1.colJoin1","TabDETjoin2","TabDETjoin2.colJoin2"));
        //CDBResultSet result = statement.executeQuery(BenchQuery.getQuerySUM("TabBenchSum", "TabBenchSum.colSUM", "TabBenchSum.colTag"));
        //CDBResultSet result = statement.executeQuery(BenchQuery.getQueryRand("TabRND", "TabRND.colRND"));
        //statement.executeInsert(BenchInsert.getRandInsertText("TabRND"));
        //statement.executeInsert(BenchInsert.getRandInsertText("TabDET"));
        //statement.executeInsert(BenchInsert.getRandInsertText("TabDETjoin1"));
        //statement.executeInsert(BenchInsert.getRandInsertInt("TabHOM"));
        statement.executeInsert(BenchInsert.getRandInsertInt("TabOPE"));

        //if(result.next()) {
            //String res = result.getString("TabDET.colDET");
            //int res  = result.getInt("TabOPE.colOPE");
            //String res1 = result.getString("TabDETjoin1.colJoin1");
            //String res2 = result.getString("TabDETjoin2.colJoin2");
            //int res  = result.getInt("TabBenchSum.colSUM");
            //String res = result.getString("TabRND.colRND");
        //}


        Thread.sleep(30000);

    }


    @Override
    public void setUp() throws Exception {
        super.setUp();
    }

    @Override
    public void tearDown() throws Exception {
        super.tearDown();
    }

    private static String str(String in) {
        return "'"+in+"'";
    }

}
