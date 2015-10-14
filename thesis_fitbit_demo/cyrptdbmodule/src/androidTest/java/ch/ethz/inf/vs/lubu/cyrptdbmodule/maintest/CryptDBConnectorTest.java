package ch.ethz.inf.vs.lubu.cyrptdbmodule.maintest;

import android.test.InstrumentationTestCase;
import android.util.Log;

import java.io.StringReader;

import ch.ethz.inf.vs.lubu.cyrptdbmodule.R;
import ch.ethz.inf.vs.lubu.cyrptdbmodule.main.CDBResultSet;
import ch.ethz.inf.vs.lubu.cyrptdbmodule.main.CDBStatement;
import ch.ethz.inf.vs.lubu.cyrptdbmodule.main.CryptDBConnector;

/**
 * Created by lukas on 20.04.15.
 */
public class CryptDBConnectorTest extends InstrumentationTestCase {

    private static String TEST_NAME = "CryptDBConnectorTest";

    private static final String IP_ADR = "192.168.1.27:8081";

    private static boolean SKIP_VERY_SLOW_OPE = true;

    private CryptDBConnector connect = null;

    private void log(String name) {
        Log.v(TEST_NAME, name);
    }

    private String insertTab1(int num, String str) {
        return "INSERT INTO Tab1 VALUES (" + String.valueOf(num) + ",'" + str + "')";
    }

    private String insertTab2(int num, String str) {
        return "INSERT INTO Tab2 VALUES (" + String.valueOf(num) + ",'" + str + "')";
    }

    private String insertOPE(int num) {
        return "INSERT INTO TabOPE VALUES (" + String.valueOf(num) + ")";
    }

    private String insertTabPers(int num, String name) {
        return "INSERT INTO TabPerson VALUES (" + String.valueOf(num) + ",'" + name + "')";
    }

    private String insertTab3(String text) {
        return "INSERT INTO Tab3 VALUES ('" + text + "')";
    }


    public void testT01() throws Exception {
        CDBStatement stmt = connect.createStatement();
        if (stmt.executeInsert(insertTab1(1, "BLA")))
            log("success");
        else
            assertTrue(false);

    }

    public void testT02() throws Exception {
        CDBStatement stmt = connect.createStatement();
        CDBResultSet res = stmt.executeQuery("SELECT Tab1.col1, Tab1.col2 FROM Tab1 WHERE Tab1.col2='BLA';");
        res.next();
        String bla = res.getString("col2");
        int b = res.getInt("col1");
        assertEquals("BLA", bla);
        assertEquals(b, 1);

    }

    public void testT03() throws Exception {
        CDBStatement stmt = connect.createStatement();
        if (stmt.executeInsert(insertTab1(345345, "Dies ist ein sehr langer Text, der in die Dataenbank kommt")))
            log("success");
        else
            assertTrue(false);

    }

    public void testT04() throws Exception {
        String str = "Dies ist ein sehr langer Text, der in die Dataenbank kommt";
        CDBStatement stmt = connect.createStatement();
        CDBResultSet res = stmt.executeQuery("SELECT Tab1.col1, Tab1.col2 FROM Tab1 WHERE Tab1.col2='" + str + "';");
        res.next();
        String bla = res.getString("col2");
        int b = res.getInt("col1");
        assertEquals(str, bla);
        assertEquals(345345, b);

    }

    public void testT05() throws Exception {
        CDBStatement stmt = connect.createStatement();
        CDBResultSet res = stmt.executeQuery("SELECT SUM(Tab1.col1) FROM Tab1;");
        res.next();
        int b = res.getInt("SUM(Tab1.col1)");
        log("Resulting Sum: " + String.valueOf(b));
        assertEquals(345346, b);
    }

    public void testT06() throws Exception {
        CDBStatement stmt = connect.createStatement();
        if (stmt.executeInsert(insertTab2(65, "BLA")))
            log("success");
        else
            assertTrue(false);

        CDBResultSet res = stmt.executeQuery("SELECT Tab1.col1, Tab1.col2, Tab2.col1, Tab2.col2 FROM Tab1, Tab2 WHERE Tab1.col2=Tab2.col2;");
        res.next();
        int x = res.getInt("Tab2.col1");
        assertEquals(x, 65);
        int y = res.getInt("Tab1.col1");
        assertEquals(y, 1);
    }

    public void testT07() throws Exception {
        CDBStatement stmt = connect.createStatement();

        CDBResultSet res = stmt.executeQuery("SELECT Tab1.col1, Tab2.col1 FROM Tab1 JOIN Tab2 ON Tab1.col2=Tab2.col2;");
        res.next();
        int x = res.getInt("Tab2.col1");
        assertEquals(x, 65);
        int y = res.getInt("Tab1.col1");
        assertEquals(y, 1);
    }

    public void testT08() throws Exception {
        CDBStatement stmt = connect.createStatement();

        for (int i = 0; i < 4; i++) {
            if (!stmt.executeInsert(insertTab2(i + 20, "TEST7")))
                assertTrue(false);
        }

        CDBResultSet res = stmt.executeQuery("SELECT COUNT(Tab2.col2) FROM Tab2 GROUP BY Tab2.col2;");
        res.next();
        int x = res.getInt("COUNT(Tab2.col2)");
        assertTrue(x == 4 || x == 1);
        res.next();
        int y = res.getInt("COUNT(Tab2.col2)");
        assertTrue(x == 4 || x == 1);
    }

    public void testT09() throws Exception {
        CDBStatement stmt = connect.createStatement();

        CDBResultSet res = stmt.executeQuery("SELECT COUNT(Tab2.col2) FROM Tab2;");
        res.next();
        int x = res.getInt("COUNT(Tab2.col2)");

        int y = 0;
        StringBuilder sb = new StringBuilder();
        CDBResultSet res2 = stmt.executeQuery("SELECT * FROM Tab2;");
        while (res2.next()) {
            y++;
            if (y <= 2) {
                for (int i = 0; i < res2.getNumCols(); i++) {
                    sb.append(res2.getString(i)).append(", ");
                }
                sb.setLength(sb.length() - 2);
                sb.append(" | ");
            }
        }
        sb.setLength(sb.length() - 3);
        assertEquals(x, y);
        assertEquals("65, BLA | 20, TEST7", sb.toString());
    }

    public void testT10() throws Exception {
        if (!SKIP_VERY_SLOW_OPE) {
            CDBStatement stmt = connect.createStatement();
            if (stmt.executeInsert(insertOPE(10)))
                log("success");
            else
                assertTrue(false);
            if (stmt.executeInsert(insertOPE(20)))
                log("success");
            else
                assertTrue(false);

            CDBResultSet res = stmt.executeQuery("SELECT TabOPE.colope FROM TabOPE WHERE TabOPE.colope > 10;");
            res.next();
            int num = res.getInt("TabOPE.colope");
            assertEquals(num, 20);
        }
    }

    public void testT11() throws Exception {
        CDBStatement stmt = connect.createStatement();

        for (int i = 0; i < 4; i++) {
            if (!stmt.executeInsert(insertTab1(10, "TEST11")))
                assertTrue(false);
        }

        CDBResultSet res = stmt.executeQuery("SELECT COUNT(Tab1.col1), SUM(Tab1.col1), Tab1.col2 FROM Tab1 GROUP BY Tab1.col2;");

        while (res.next()) {
            String name = res.getString("Tab1.col2");
            int sum = res.getInt("SUM(Tab1.col1)");
            if (name.equals("TEST11")) {
                assertEquals(40, sum);
            } else if (name.equals("BLA")) {
                assertEquals(1, sum);
            } else {
                assertEquals(345345, sum);
            }
        }
    }

    public void testT12() throws Exception {
        CDBStatement stmt = connect.createStatement();


        if (!stmt.executeInsert(insertTabPers(0, "Pingu")))
            assertTrue(false);

        if (!stmt.executeInsert(insertTabPers(0, "Homer")))
            assertTrue(false);

        if (!stmt.executeInsert(insertTabPers(0, "King")))
            assertTrue(false);

        if (!stmt.executeInsert(insertTabPers(0, "SpongeBob")))
            assertTrue(false);


        CDBResultSet res = stmt.executeQuery("SELECT TabPerson.persName FROM TabPerson WHERE TabPerson.persID > 3;");
        res.next();
        assertEquals("SpongeBob", res.getString("TabPerson.persName"));
    }

    public void testT13() throws Exception {
        CDBStatement stmt = connect.createStatement();

        if (!stmt.executeInsert(insertTab3("ab")))
            assertTrue(false);

        if (!stmt.executeInsert(insertTab3("acbd")))
            assertTrue(false);

        if (!stmt.executeInsert(insertTab3("abcdefgh")))
            assertTrue(false);

        if (!stmt.executeInsert(insertTab3("abcdefghabcdefgh")))
            assertTrue(false);

        if (!stmt.executeInsert(insertTab3("abcdefghabcdefghabcdefghabcdefgh")))
            assertTrue(false);

    }

    public void testT14() throws Exception {
        CDBStatement stmt = connect.createStatement();


        if (!stmt.executeInsert(insertTab1(7, "77Padding")))
            assertTrue(false);

        CDBResultSet res = stmt.executeQuery("SELECT Tab1.col1, Tab1.col2 FROM Tab1 WHERE Tab1.col2='77Padding';");
        res.next();
        String pad = res.getString("col2");
        int b = res.getInt("col1");
        assertEquals("77Padding", pad);
        assertEquals(7, b);

        for (int i = 250; i < 270; i++) {
            if (!stmt.executeInsert(insertTab1(i, "IDT14" + i)))
                assertTrue(false);
            CDBResultSet resCur = stmt.executeQuery("SELECT Tab1.col1, Tab1.col2 FROM Tab1 WHERE Tab1.col2='" + "IDT14" + i + "';");
            resCur.next();
            int value = resCur.getInt("col1");
            assertEquals(i, value);
        }

    }

    public void testT15() throws Exception {
        CDBStatement stmt = connect.createStatement();


        if (!stmt.executeInsert(insertTab1(-33, "NegativeNumber")))
            assertTrue(false);

        CDBResultSet res = stmt.executeQuery("SELECT Tab1.col1, Tab1.col2 FROM Tab1 WHERE Tab1.col2='NegativeNumber';");
        res.next();
        int b = res.getInt("col1");
        assertEquals(-33, b);

        if (!stmt.executeInsert(insertTab1(50, "NegativeNumber")))
            assertTrue(false);
        if (!stmt.executeInsert(insertTab1(-1, "NegativeNumber")))
            assertTrue(false);
        if (!stmt.executeInsert(insertTab1(-2, "NegativeNumber")))
            assertTrue(false);

        CDBResultSet res2 = stmt.executeQuery("SELECT SUM(Tab1.col1) FROM Tab1 WHERE Tab1.col2='NegativeNumber';");
        res2.next();
        int sum = res2.getInt("SUM(Tab1.col1)");
        assertEquals(14, sum);
    }

    public void testT16() throws Exception {
        CDBStatement stmt = connect.createStatement();

        CDBResultSet res = stmt.executeQuery("SELECT * FROM Tab1 LIMIT 5;");
        int count = 0;
        while (res.next()) {
            count++;
        }
        assertEquals(5, count);
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
