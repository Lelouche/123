package ch.ethz.inf.vs.lubu.cyrptdbmodule.rewrite;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.select.Select;

import ch.ethz.inf.vs.lubu.cyrptdbmodule.crypto.ICryptoManager;
import ch.ethz.inf.vs.lubu.cyrptdbmodule.dbscheme.DBSchemeManager;
import ch.ethz.inf.vs.lubu.cyrptdbmodule.exceptions.CDBException;
import ch.ethz.inf.vs.lubu.cyrptdbmodule.exceptions.RewriteCDBException;
import ch.ethz.inf.vs.lubu.cyrptdbmodule.util.Logger;

/**
 * Created by lukas on 24.03.15.
 * Visitor template is taken from JSQLLibrary (LGPL V2.1.)
 * API for Rewriting SQL queries
 */
public class QueryRewriter implements IQueryRewriter {

    @Override
    public IRewriteResult rewriteQuery(ICryptoManager cryMan, DBSchemeManager dmMan, String toRewrite) throws CDBException {
        Statement stmt = null;
        try {
            stmt = CCJSqlParserUtil.parse(toRewrite);
        } catch (JSQLParserException e) {
            throw new RewriteCDBException("Error in Parsing Query: " + e.getMessage());
        }

        Select select = null;
        if (!(stmt instanceof Select))
            throw new RewriteCDBException("No SELECT statement");
        else
            select = (Select) stmt;

        Logger.log("Checking..");
        QueryMetadata metadata = new QueryMetadata(cryMan, dmMan);
        SelectChecker sc = new SelectChecker(metadata);
        ExprChecker ec = new ExprChecker(sc, metadata);

        sc.setExpressionVisitor(ec);
        select.getSelectBody().accept(sc);

        if (metadata.hasException())
            metadata.throwException();
        Logger.log("Checking succeeded");

        StringBuilder sb = new StringBuilder();
        SelectRewriter rewriter = new SelectRewriter(metadata, sb);
        ExprRewriter exprRewriter = new ExprRewriter(rewriter, metadata, sb);
        rewriter.setExpressionVisitor(exprRewriter);
        select.getSelectBody().accept(rewriter);

        if (metadata.hasException())
            metadata.throwException();
        Logger.log("Rewriting succeeded");

        return new RewriteResult(sb.append(";").toString(), metadata, true);
    }

    @Override
    public IRewriteResult rewriteInsert(ICryptoManager cryMan, DBSchemeManager dmMan, String toRewrite) throws CDBException {
        Statement stmt = null;
        try {
            stmt = CCJSqlParserUtil.parse(toRewrite);
        } catch (JSQLParserException e) {
            throw new RewriteCDBException("Error in Parsing Query: " + e.getMessage());
        }

        Insert insert = null;
        if (!(stmt instanceof Insert))
            throw new RewriteCDBException("No INSERT statement");
        else
            insert = (Insert) stmt;

        Logger.log("Checking..");
        QueryMetadata metadata = new QueryMetadata(cryMan, dmMan);
        ExprChecker ec = new ExprChecker(metadata);
        InsertChecker sc = new InsertChecker(ec, null, metadata);

        sc.check(insert);
        Logger.log("Checking succeeded");

        StringBuilder sb = new StringBuilder();
        ExprRewriter exprRewriter = new ExprRewriter(metadata, sb);
        InsertRewriter insertRewriter = new InsertRewriter(exprRewriter, sb, metadata, sc.getOrderCols());
        insertRewriter.rewrite(insert);

        if (metadata.hasException())
            metadata.throwException();
        Logger.log("Rewriting succeeded");

        return new RewriteResult(sb.append(";").toString(), metadata, false);
    }

    @Override
    public IRewriteResult rewriteUpdate(ICryptoManager cryMan, DBSchemeManager dmMan, String toRewrite) throws CDBException {
        return null;
    }
}
