package ch.ethz.inf.vs.lubu.cyrptdbmodule.rewrite;

import ch.ethz.inf.vs.lubu.cyrptdbmodule.crypto.mOPEJob;

/**
 * Created by lukas on 26.05.15.
 */
public interface IRewriteResult {

    public String getRewrite();

    public boolean hasmOPE();

    public mOPEJob getmOPEJob();

}
