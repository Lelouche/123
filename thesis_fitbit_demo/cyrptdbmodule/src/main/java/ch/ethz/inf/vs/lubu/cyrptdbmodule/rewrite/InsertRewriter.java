package ch.ethz.inf.vs.lubu.cyrptdbmodule.rewrite;

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.ExpressionVisitor;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.expression.operators.relational.ItemsListVisitor;
import net.sf.jsqlparser.expression.operators.relational.MultiExpressionList;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.select.SubSelect;

import java.util.Iterator;
import java.util.List;

import ch.ethz.inf.vs.lubu.cyrptdbmodule.dbscheme.CDBColumn;
import ch.ethz.inf.vs.lubu.cyrptdbmodule.exceptions.CDBException;

/**
 * Created by lukas on 24.03.15.
 * Visitor template is taken from JSQLLibrary (LGPL V2.1.)
 * Rewrites Insert query's
 */
public class InsertRewriter implements ItemsListVisitor {

    private StringBuilder buffer;
    private ExpressionVisitor expressionVisitor;

    private QueryMetadata meta;
    private List<CDBColumn> orderedColumns;


    public InsertRewriter(ExpressionVisitor expressionVisitor, StringBuilder buffer, QueryMetadata meta, List<CDBColumn> orderedColumns) {
        this.buffer = buffer;
        this.expressionVisitor = expressionVisitor;
        this.meta = meta;
        this.orderedColumns = orderedColumns;
    }

    public StringBuilder getBuffer() {
        return buffer;
    }

    public void setBuffer(StringBuilder buffer) {
        this.buffer = buffer;
    }

    public void rewrite(Insert insert) throws CDBException {
        String tableName = insert.getTable().getFullyQualifiedName();
        meta.inInsert = true;
        buffer.append("INSERT INTO ");
        buffer.append(meta.getFullHashTabName(tableName));
        if (insert.getColumns() != null) {
            buffer.append(" (");
            for (Iterator<Column> iter = insert.getColumns().iterator(); iter.hasNext(); ) {
                Column column = iter.next();
                String colname = column.getFullyQualifiedName();
                buffer.append(meta.getAllHashCols(tableName + "." + colname));
                if (iter.hasNext()) {
                    buffer.append(", ");
                }
            }
            buffer.append(")");
        } else {
            buffer.append(" (");
            for (Iterator<CDBColumn> colIter = orderedColumns.iterator(); colIter.hasNext(); ) {
                String colname = colIter.next().getRealName();
                buffer.append(meta.getAllHashCols(tableName + "." + colname));
                if (colIter.hasNext()) {
                    buffer.append(", ");
                }
            }
            buffer.append(")");
        }

        if (insert.getItemsList() != null) {
            insert.getItemsList().accept(this);
        }
    }

    @Override
    public void visit(ExpressionList expressionList) {
        buffer.append(" VALUES (");
        for (Iterator<Expression> iter = expressionList.getExpressions().iterator(); iter.hasNext(); ) {
            Expression expression = iter.next();
            expression.accept(expressionVisitor);
            if (iter.hasNext()) {
                buffer.append(", ");
            }
        }
        buffer.append(")");
    }

    @Override
    public void visit(MultiExpressionList multiExprList) {

    }

    @Override
    public void visit(SubSelect subSelect) {

    }

    public ExpressionVisitor getExpressionVisitor() {
        return expressionVisitor;
    }

    public void setExpressionVisitor(ExpressionVisitor visitor) {
        expressionVisitor = visitor;
    }


}
