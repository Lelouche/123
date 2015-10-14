package ch.ethz.inf.vs.lubu.cyrptdbmodule.cryptotest;

import android.test.InstrumentationTestCase;
import android.util.Log;

import ch.ethz.inf.vs.lubu.cyrptdbmodule.crypto.IKeyManager;
import ch.ethz.inf.vs.lubu.cyrptdbmodule.crypto.KeyManager;
import ch.ethz.inf.vs.lubu.cyrptdbmodule.util.ContextHolder;

/**
 * Created by lukas on 30.03.15.
 */
public class TestKeyManager extends InstrumentationTestCase {

    private static String TEST_NAME = "TestKeyManager";

    private IKeyManager man;


    public TestKeyManager() {
    }

    private void log(String name) {
        Log.i(TEST_NAME, name);
    }


    public void testTest1() throws Exception {
        /*CDBColumn col1 = createTestColumn("Col1", "ZNFQVHBOXC");
        CDBColumn col2 = createTestColumn("Col2", "JRVNJYSOZR");
        CDBKey k1 = man.getKey(col1);
        CDBKey k2 = man.getKey(col1);
        CDBKey k3 = man.getKey(col2);

        log("Generated key1: "+ CDBUtil.bytesToHex(k1.getEncoded()));
        log("Generated key2: "+ CDBUtil.bytesToHex(k3.getEncoded()));

        assertEquals(new String(k1.getEncoded()),new String(k2.getEncoded()));*/
    }

    /*private CDBColumn createTestColumn(String name, String hash) {
        //return new CDBColumn(name, hash, null, null, null, null,null);
    }*/

    @Override
    public void setUp() throws Exception {
        super.setUp();
        ContextHolder.setContext(this.getInstrumentation().getContext());
        man = new KeyManager();
    }

    @Override
    public void tearDown() throws Exception {
        super.tearDown();
    }
}
