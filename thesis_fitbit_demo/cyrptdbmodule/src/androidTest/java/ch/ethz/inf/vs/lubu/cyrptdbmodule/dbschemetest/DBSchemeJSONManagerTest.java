package ch.ethz.inf.vs.lubu.cyrptdbmodule.dbschemetest;

import android.test.InstrumentationTestCase;
import android.util.Log;

import java.io.StringReader;
import java.math.BigInteger;

import ch.ethz.inf.vs.lubu.cyrptdbmodule.R;
import ch.ethz.inf.vs.lubu.cyrptdbmodule.dbscheme.CDBColumn;
import ch.ethz.inf.vs.lubu.cyrptdbmodule.dbscheme.CDBField;
import ch.ethz.inf.vs.lubu.cyrptdbmodule.dbscheme.DBSchemeJSONManager;

/**
 * Created by lukas on 25.03.15.
 */
public class DBSchemeJSONManagerTest extends InstrumentationTestCase {

    private static String TEST_NAME = "DBSchemeJSONManagerTest";

    DBSchemeJSONManager jMan;

    public DBSchemeJSONManagerTest() {
    }

    private void log(String name) {
        Log.v(TEST_NAME, name);
    }


    public void setUPLocal() throws Exception {
        StringReader sr = new StringReader((getInstrumentation().getTargetContext().getText(R.string.TestScheme)).toString());
        jMan = new DBSchemeJSONManager(sr);
    }

    public void testTest1() throws Exception {
        BigInteger x = new BigInteger("20034");
        String y = "ThisismyTest";
        CDBColumn col = jMan.getColumn("Tab2", "col1");
        CDBField field1 = new CDBField(col);
        CDBField field2 = new CDBField(col);
        field1.setValue(x);
        field2.setValue(y);
        String xR = field1.getNumRep();
        String yR = field2.getBase64StringRep();
        assertEquals(x.toString(), xR);
        assertEquals(yR, y);
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        setUPLocal();
    }

    @Override
    public void tearDown() throws Exception {
        super.tearDown();
    }
}
