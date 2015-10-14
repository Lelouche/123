package ch.ethz.inf.vs.lubu.cyrptdbmodule.maintest;

import android.test.InstrumentationTestCase;
import android.util.Log;

import java.io.StringReader;
import java.util.Random;

import ch.ethz.inf.vs.lubu.cyrptdbmodule.R;
import ch.ethz.inf.vs.lubu.cyrptdbmodule.main.CDBResultSet;
import ch.ethz.inf.vs.lubu.cyrptdbmodule.main.CDBStatement;
import ch.ethz.inf.vs.lubu.cyrptdbmodule.main.CryptDBConnector;

/**
 * Created by lukas on 26.05.15.
 */
public class mOPETest extends InstrumentationTestCase {

    private static String TEST_NAME = "mOPETest";

    private static final String IP_ADR = "192.168.1.27:8081";

    private CryptDBConnector connect = null;

    private void log(String name) {
        Log.v(TEST_NAME, name);
    }

    private String insertTabOpe(int intplainnum1, int num1, int num2) {
        return "INSERT INTO TabmOpe VALUES (" + String.valueOf(intplainnum1) + ", " + String.valueOf(num1) + ", " + String.valueOf(num2)  + ")";
    }

    public void testT01() throws Exception {
        for(int i=0; i<100; i++){
            CDBStatement stmt = connect.createStatement();
            if (stmt.executeInsert(insertTabOpe(1+i,1+i, 100+i)))
                log("success");
            else
                assertTrue(false);
        }
    }

    public void testT02() throws Exception {
        CDBStatement stmt = connect.createStatement();
        String query = "SELECT TabmOpe.colOpe1 FROM TabmOpe WHERE colOpe1<2;";
        CDBResultSet res = stmt.executeQuery(query);
        res.next();
        int a = res.getInt("colOpe1");
        assertEquals(1,a);
    }

    public void testT03() throws Exception {
        CDBStatement stmt = connect.createStatement();
        String query = "SELECT TabmOpe.colOpe1 FROM TabmOpe WHERE colOpe1>=10 AND colOpe1<=15;";
        CDBResultSet res = stmt.executeQuery(query);
        int count = 0;
        while (res.next()) {
            count++;
        }
        assertEquals(6,count);
    }

    public void testT04() throws Exception {
        CDBStatement stmt = connect.createStatement();
        String query = "SELECT MIN(TabmOpe.colOpe1), MAX(TabmOpe.colOpe1) FROM TabmOpe;";
        CDBResultSet res = stmt.executeQuery(query);
        res.next();
        int min = res.getInt("MIN(TabmOpe.colOpe1)");
        int max = res.getInt("MAX(TabmOpe.colOpe1)");
        assertEquals(1,min);
        assertEquals(100,max);

    }

    public void testT05() throws Exception {
        Random rand = new Random();
        for(int i=0; i<100; i++){
            CDBStatement stmt = connect.createStatement();
            int temp = rand.nextInt();
            if (stmt.executeInsert(insertTabOpe(temp,temp,rand.nextInt())))
                log("success");
            else
                assertTrue(false);
        }
    }

    public void testT06() throws Exception {
        CDBStatement stmt = connect.createStatement();
        String query = "SELECT TabmOpe.colOpe1 FROM TabmOpe WHERE colOpe1>=200 ORDER BY TabmOpe.colOpe1 ASC LIMIT 10;";
        CDBResultSet res = stmt.executeQuery(query);
        if(res.next()) {
            int last = res.getInt("TabmOpe.colOpe1");
            while (res.next()) {
                int cur = res.getInt("TabmOpe.colOpe1");
                assertTrue(last<=cur);
                last=cur;
            }
        }
    }

    public void testT07() throws Exception {
        CDBStatement stmt = connect.createStatement();
        String query = "SELECT TabmOpe.colOpe1 FROM TabmOpe WHERE colOpe1<0 ORDER BY TabmOpe.colOpe1 ASC LIMIT 10;";
        CDBResultSet res = stmt.executeQuery(query);
        if(res.next()) {
            int last = res.getInt("TabmOpe.colOpe1");
            while (res.next()) {
                int cur = res.getInt("TabmOpe.colOpe1");
                assertTrue(last<=cur);
                last=cur;
            }
        }
    }





    @Override
    public void setUp() throws Exception {
        super.setUp();
        if(connect == null) {
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
    }

}
