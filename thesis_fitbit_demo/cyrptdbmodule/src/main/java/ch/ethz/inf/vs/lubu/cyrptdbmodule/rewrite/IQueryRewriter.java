package ch.ethz.inf.vs.lubu.cyrptdbmodule.rewrite;

import ch.ethz.inf.vs.lubu.cyrptdbmodule.crypto.ICryptoManager;
import ch.ethz.inf.vs.lubu.cyrptdbmodule.dbscheme.DBSchemeManager;
import ch.ethz.inf.vs.lubu.cyrptdbmodule.exceptions.CDBException;

/**
 * Created by lukas on 04.03.15.
 */
public interface IQueryRewriter {

    public IRewriteResult rewriteQuery(ICryptoManager cryMan, DBSchemeManager dmMan, String toRewrite) throws CDBException;

    public IRewriteResult rewriteInsert(ICryptoManager cryMan, DBSchemeManager dmMan, String toRewrite) throws CDBException;

    public IRewriteResult rewriteUpdate(ICryptoManager cryMan, DBSchemeManager dmMan, String toRewrite) throws CDBException;

}
