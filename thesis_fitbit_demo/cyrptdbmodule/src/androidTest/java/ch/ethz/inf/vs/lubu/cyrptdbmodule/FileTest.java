package ch.ethz.inf.vs.lubu.cyrptdbmodule;

import android.util.Log;

import junit.framework.TestCase;

/**
 * Created by lukas on 22.03.15.
 */
public class FileTest extends TestCase {

    private static final String TEST_NAME = "FileTest";

    private long timeDecSum = 0;

    public FileTest() {
        super(TEST_NAME);
    }

    private void log(String name) {
        Log.i(TEST_NAME, name);
    }


    public void testFileExtern() throws Exception {
        FileUtil fu = new FileUtil("TestFile");
        StringBuilder sb = new StringBuilder();
        sb.append("Hello World");
        fu.writeToFile(sb.toString());
    }


    @Override
    public void setUp() throws Exception {
        super.setUp();


    }

    @Override
    public void tearDown() throws Exception {
        super.tearDown();

    }
}
