package ch.ethz.inf.vs.lubu.cyrptdbmodule.rewrite;

import ch.ethz.inf.vs.lubu.cyrptdbmodule.crypto.mOPEJob;

/**
 * Created by lukas on 26.05.15.
 */
public class RewriteResult implements IRewriteResult {

    private String query;

    private QueryMetadata meta;

    private boolean isQuery;

    public RewriteResult(String query, QueryMetadata meta, boolean isQuery) {
        this.query = query;
        this.meta = meta;
        this.isQuery = isQuery;
    }

    @Override
    public String getRewrite() {
        return query;
    }

    @Override
    public boolean hasmOPE() {
        return meta.containsmOPE;
    }

    @Override
    public mOPEJob getmOPEJob() {
        mOPEJob job = new mOPEJob(query,!isQuery);
        for(mOPEJob.mOPEWork work : meta.getmOPEWork()) {
            job.addWork(work);
        }
        return job;
    }
}
