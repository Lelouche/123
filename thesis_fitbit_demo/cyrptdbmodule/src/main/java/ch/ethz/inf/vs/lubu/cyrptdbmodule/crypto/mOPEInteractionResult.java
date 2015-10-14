package ch.ethz.inf.vs.lubu.cyrptdbmodule.crypto;

/**
 * Created by lukas on 26.05.15.
 */
public class mOPEInteractionResult {

    private String queryRes = null;

    private boolean success;

    public mOPEInteractionResult(boolean success) {
        this.success = success;
    }

    public mOPEInteractionResult(boolean success, String queryRes) {
        this.success = success;
        this.queryRes = queryRes;
    }

    public String getResult() {
        return  queryRes;
    }

    public boolean succeeded() {
        return success;
    }


    public boolean hasResult() {
        return queryRes!=null;
    }

}
