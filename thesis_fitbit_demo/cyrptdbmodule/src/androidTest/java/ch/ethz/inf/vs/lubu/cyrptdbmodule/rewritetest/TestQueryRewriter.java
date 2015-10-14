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
 * Created by lukas on 29.03.15.
 */
public class TestQueryRewriter extends InstrumentationTestCase {

    private static String TEST_NAME = "TestQueryRewriter";

    private DBSchemeManager man;

    private IKeyManager manKey;

    private ICryptoManager manCrypt;

    public TestQueryRewriter() {
    }

    private void log(String name) {
        Log.i(TEST_NAME, name);
    }


    private String rewriteQuery(String query) throws Exception {
        log("Before: " + query);
        QueryRewriter rew = new QueryRewriter();
        IRewriteResult res = rew.rewriteQuery(manCrypt, man, query);
        log("After: " + res.getRewrite());
        return res.getRewrite();
    }


    public void testTest1() throws Exception {
        try {
            String query = "SELECT Tab1.col1, Tab1.col2 FROM Tab1, Tab2 WHERE Tab2.col1=Tab1.col2;";
            rewriteQuery(query);
        } catch (RewriteCDBException e) {
            return;
        }
        assertTrue(false);
    }

    public void testTest2() throws Exception {
        try {
            String query = "SELECT Tab1.col1, Tab1.col2 FROM Tab1, Tab2 WHERE Tab2.col2=Tab1.col2;";
            rewriteQuery(query);
        } catch (RewriteCDBException e) {
            assertTrue(false);
        }
    }

    public void testTest3() throws Exception {
        try {
            String query = "SELECT Tab1.col1, Tab1.col2 FROM Tab1, Tab2 WHERE Tab2.col2='HelloWorld';";
            rewriteQuery(query);
        } catch (RewriteCDBException e) {
            assertTrue(false);
        }
    }

    public void testTest4() throws Exception {
        String query = "SELECT Tab1.col1, Tab1.col2 FROM Tab1, Tab2 WHERE Tab2.col2=33;";
        rewriteQuery(query);

    }

    public void testTest5() throws Exception {
        String query = "SELECT SUM(Tab1.col1), Tab1.col2 FROM Tab1, Tab2;";
        rewriteQuery(query);

    }

    public void testTest6() throws Exception {
        String query = "SELECT * FROM Tab1, Tab2 GROUP BY Tab1.col2;";
        rewriteQuery(query);
    }

    public void testTest7() throws Exception {
        String query = "SELECT Tab1.col2, COUNT(Tab1.col2) FROM Tab1, Tab2 GROUP BY Tab1.col2;";
        rewriteQuery(query);
    }

    public void testTest8() throws Exception {
        String query = "SELECT * FROM Tab1 JOIN Tab2 ON Tab2.col2=Tab1.col2;";
        rewriteQuery(query);
    }

    public void testTest9() throws Exception {
        try {
            String query = "SELECT Tab1.col1 FROM Tab1 JOIN Tab2;";
            rewriteQuery(query);
        } catch (RewriteCDBException e) {
            return;
        }
        assertTrue(false);
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
