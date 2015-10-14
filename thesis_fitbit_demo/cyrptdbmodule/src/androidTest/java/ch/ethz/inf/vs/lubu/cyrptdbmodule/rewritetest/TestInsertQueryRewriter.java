package ch.ethz.inf.vs.lubu.cyrptdbmodule.rewritetest;

import android.test.InstrumentationTestCase;
import android.util.Log;

import java.io.StringReader;

import ch.ethz.inf.vs.lubu.cyrptdbmodule.R;
import ch.ethz.inf.vs.lubu.cyrptdbmodule.crypto.CryptoManager;
import ch.ethz.inf.vs.lubu.cyrptdbmodule.crypto.ICryptoManager;
import ch.ethz.inf.vs.lubu.cyrptdbmodule.crypto.IKeyManager;
import ch.ethz.inf.vs.lubu.cyrptdbmodule.crypto.KeyManager;
import ch.ethz.inf.vs.lubu.cyrptdbmodule.dbscheme.DBSchemeJSONManager;
import ch.ethz.inf.vs.lubu.cyrptdbmodule.dbscheme.DBSchemeManager;
import ch.ethz.inf.vs.lubu.cyrptdbmodule.exceptions.RewriteCDBException;
import ch.ethz.inf.vs.lubu.cyrptdbmodule.exceptions.SchemeCDBException;
import ch.ethz.inf.vs.lubu.cyrptdbmodule.rewrite.IRewriteResult;
import ch.ethz.inf.vs.lubu.cyrptdbmodule.rewrite.QueryRewriter;
import ch.ethz.inf.vs.lubu.cyrptdbmodule.util.ContextHolder;

/**
 * Created by lukas on 19.04.15.
 */
public class TestInsertQueryRewriter extends InstrumentationTestCase {

    private static String TEST_NAME = "TestInsertQueryRewriter";

    private DBSchemeManager man;

    private IKeyManager manKey;

    private ICryptoManager manCrypt;

    public TestInsertQueryRewriter() {
    }

    private void log(String name) {
        Log.i(TEST_NAME, name);
    }


    private String rewriteQuery(String query) throws Exception {
        log("Before: " + query);
        QueryRewriter rew = new QueryRewriter();
        IRewriteResult res = rew.rewriteInsert(manCrypt, man, query);
        log("After: " + res.getRewrite());
        return res.getRewrite();
    }


    public void testTest1() throws Exception {
        try {
            String query = "INSERT INTO Tab1 VALUES (1,'BLA')";
            rewriteQuery(query);
        } catch (RewriteCDBException e) {
            assertTrue(false);
        }
    }

    public void testTest2() throws Exception {
        try {
            String query = "INSERT INTO Tab2 VALUES (2,'BLA')";
            rewriteQuery(query);
        } catch (RewriteCDBException e) {
            assertTrue(false);
        }
    }


    private void loadScheme() {
        StringReader sr = new StringReader((getInstrumentation().getTargetContext().getText(R.string.TestScheme)).toString());
        try {
            man = new DBSchemeJSONManager(sr);
        } catch (SchemeCDBException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        loadScheme();
        ContextHolder.setContext(this.getInstrumentation().getContext());
        manKey = new KeyManager();
        manCrypt = new CryptoManager(manKey);
    }

    @Override
    public void tearDown() throws Exception {
        super.tearDown();
    }

}
