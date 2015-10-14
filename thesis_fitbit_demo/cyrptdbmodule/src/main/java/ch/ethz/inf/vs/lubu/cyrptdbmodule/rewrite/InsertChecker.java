package ch.ethz.inf.vs.lubu.cyrptdbmodule.rewrite;

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.ExpressionVisitor;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.expression.operators.relational.ItemsListVisitor;
import net.sf.jsqlparser.expression.operators.relational.MultiExpressionList;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.select.SelectVisitor;
import net.sf.jsqlparser.statement.select.SubSelect;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import ch.ethz.inf.vs.lubu.cyrptdbmodule.dbscheme.CDBColumn;
import ch.ethz.inf.vs.lubu.cyrptdbmodule.exceptions.CDBException;
import ch.ethz.inf.vs.lubu.cyrptdbmodule.exceptions.NotSupportedCDBException;
import ch.ethz.inf.vs.lubu.cyrptdbmodule.exceptions.RewriteCDBException;

/**
 * Created by lukas on 18.04.15.
 * Visitor template is taken from JSQLLibrary (LGPL V2.1.)
 * Checks rewrite queries
 */
public class InsertChecker implements ItemsListVisitor {

    private ExprChecker expressionVisitor;
    private SelectVisitor selectVisitor;

    private QueryMetadata meta;

    private List<CDBColumn> orderCols = new ArrayList<CDBColumn>();

    public InsertChecker(ExprChecker expressionVisitor, SelectVisitor selectVisitor, QueryMetadata meta) {
        this.expressionVisitor = expressionVisitor;
        this.selectVisitor = selectVisitor;
        this.meta = meta;
    }

    public void check(Insert insert) throws CDBException {
        String tableName = insert.getTable().getFullyQualifiedName();
        if (!meta.addCDBTable(tableName)) {
            throw new RewriteCDBException("Wrong Table " + tableName + " in Insert ");
        }

        if (insert.getColumns() != null) {
            for (Iterator<Column> iter = insert.getColumns().iterator(); iter.hasNext(); ) {
                Column column = iter.next();
                String colName = column.getFullyQualifiedName();
                if (!meta.addCDBColumn(tableName, colName)) {
                    throw new RewriteCDBException("Wrong Column " + colName + " in Insert ");
                }
                orderCols.add(meta.getColumn(tableName + "." + colName));
            }
        } else {
            orderCols = meta.addAllColumnsFromTable(tableName);
        }

        if (insert.getItemsList() != null) {
            insert.getItemsList().accept(this);
        }

        if (insert.getSelect() != null) {
            throw new NotSupportedCDBException("InsertSelect");
        }

        if (meta.hasException())
            meta.throwException();
    }

    public QueryMetadata getMeta() {
        return meta;
    }

    public void setMeta(QueryMetadata meta) {
        this.meta = meta;
    }

    public List<CDBColumn> getOrderCols() {
        return orderCols;
    }

    @Override
    public void visit(ExpressionList expressionList) {
        Iterator<CDBColumn> iterCol = orderCols.iterator();
        for (Iterator<Expression> iter = expressionList.getExpressions().iterator(); iter.hasNext(); ) {
            Expression expression = iter.next();
            if (!iterCol.hasNext()) {
                meta.setException(new RewriteCDBException("Num Cols not matching num values"));
                return;
            }
            CDBColumn col = iterCol.next();
            TypeScope type = new TypeScope(TypeScope.ExprType.EXPR_ALL);
            try {
                type.addColType(col);
            } catch (CDBException e) {
                meta.setException(e);
                return;
            }
            expressionVisitor.setCurTypeScope(type);
            expression.accept(expressionVisitor);
        }
    }

    @Override
    public void visit(MultiExpressionList multiExprList) {
        meta.setException(new NotSupportedCDBException("multiExprList"));
    }

    @Override
    public void visit(SubSelect subSelect) {
        meta.setException(new NotSupportedCDBException("subSelect"));
    }

    public ExpressionVisitor getExpressionVisitor() {
        return expressionVisitor;
    }

    public SelectVisitor getSelectVisitor() {
        return selectVisitor;
    }

    public void setExpressionVisitor(ExprChecker visitor) {
        expressionVisitor = visitor;
    }

    public void setSelectVisitor(SelectVisitor visitor) {
        selectVisitor = visitor;
    }
}
