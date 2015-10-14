package ch.ethz.inf.vs.lubu.cyrptdbmodule.rewrite;

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.ExpressionVisitor;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.select.AllColumns;
import net.sf.jsqlparser.statement.select.AllTableColumns;
import net.sf.jsqlparser.statement.select.Fetch;
import net.sf.jsqlparser.statement.select.FromItem;
import net.sf.jsqlparser.statement.select.FromItemVisitor;
import net.sf.jsqlparser.statement.select.Join;
import net.sf.jsqlparser.statement.select.LateralSubSelect;
import net.sf.jsqlparser.statement.select.Limit;
import net.sf.jsqlparser.statement.select.Offset;
import net.sf.jsqlparser.statement.select.OrderByElement;
import net.sf.jsqlparser.statement.select.OrderByVisitor;
import net.sf.jsqlparser.statement.select.Pivot;
import net.sf.jsqlparser.statement.select.PivotVisitor;
import net.sf.jsqlparser.statement.select.PivotXml;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.SelectExpressionItem;
import net.sf.jsqlparser.statement.select.SelectItem;
import net.sf.jsqlparser.statement.select.SelectItemVisitor;
import net.sf.jsqlparser.statement.select.SelectVisitor;
import net.sf.jsqlparser.statement.select.SetOperationList;
import net.sf.jsqlparser.statement.select.SubJoin;
import net.sf.jsqlparser.statement.select.SubSelect;
import net.sf.jsqlparser.statement.select.ValuesList;
import net.sf.jsqlparser.statement.select.WithItem;

import java.util.Iterator;
import java.util.List;

import ch.ethz.inf.vs.lubu.cyrptdbmodule.exceptions.NotSupportedCDBException;
import ch.ethz.inf.vs.lubu.cyrptdbmodule.exceptions.RewriteCDBException;

/**
 * Created by lukas on 24.03.15.
 * Visitor template is taken from JSQLLibrary (LGPL V2.1.)
 * Checks if a select statement is valid
 */
public class SelectChecker implements SelectVisitor, OrderByVisitor, SelectItemVisitor, FromItemVisitor, PivotVisitor {

    private ExprChecker expressionVisitor;

    private QueryMetadata meta;

    public SelectChecker(ExprChecker expressionVisitor, QueryMetadata meta) {
        this.expressionVisitor = expressionVisitor;
        this.meta = meta;
    }

    public SelectChecker(QueryMetadata meta) {
        this.meta = meta;
    }

    @Override
    public void visit(PlainSelect plainSelect) {

        //Visit FROM items
        if (plainSelect.getFromItem() != null) {
            plainSelect.getFromItem().accept(this);
            if (plainSelect.getJoins() != null) {
                for (Join join : plainSelect.getJoins()) {
                    addJoinTables(join);
                }
            }
        }

        if (plainSelect.getDistinct() != null) {
            if (plainSelect.getDistinct().getOnSelectItems() != null) {
                for (Iterator<SelectItem> iter = plainSelect.getDistinct().getOnSelectItems().iterator(); iter.hasNext(); ) {
                    SelectItem selectItem = iter.next();
                    selectItem.accept(this);
                }
            }

        }

        for (Iterator<SelectItem> iter = plainSelect.getSelectItems().iterator(); iter.hasNext(); ) {
            SelectItem selectItem = iter.next();
            selectItem.accept(this);
        }

        if (plainSelect.getIntoTables() != null) {
            for (Iterator<Table> iter = plainSelect.getIntoTables().iterator(); iter.hasNext(); ) {
                visit(iter.next());
            }
        }


        if (plainSelect.getJoins() != null) {
            for (Join join : plainSelect.getJoins()) {
                checkJoin(join);
            }
        }

        if (plainSelect.getWhere() != null) {
            plainSelect.getWhere().accept(expressionVisitor);
        }

        if (plainSelect.getOracleHierarchical() != null) {
            plainSelect.getOracleHierarchical().accept(expressionVisitor);
        }

        if (plainSelect.getGroupByColumnReferences() != null) {
            for (Iterator<Expression> iter = plainSelect.getGroupByColumnReferences().iterator(); iter.hasNext(); ) {
                Expression columnReference = iter.next();
                expressionVisitor.setCurTypeScope(new TypeScope(TypeScope.ExprType.EXPR_DETALL));
                columnReference.accept(expressionVisitor);
            }
        }

        if (plainSelect.getHaving() != null) {
            plainSelect.getHaving().accept(expressionVisitor);
        }

        if (plainSelect.getOrderByElements() != null) {
            deparseOrderBy(plainSelect.isOracleSiblings(), plainSelect.getOrderByElements());
        }

        if (plainSelect.getLimit() != null) {
            deparseLimit(plainSelect.getLimit());
        }
        if (plainSelect.getOffset() != null) {
            deparseOffset(plainSelect.getOffset());
        }
        if (plainSelect.getFetch() != null) {
            deparseFetch(plainSelect.getFetch());
        }

    }

    @Override
    public void visit(OrderByElement orderBy) {
        expressionVisitor.setCurTypeScope(new TypeScope(TypeScope.ExprType.EXPR_OPE));
        orderBy.getExpression().accept(expressionVisitor);
        expressionVisitor.setCurTypeScope(null);
    }

    public void visit(Column column) {

    }

    @Override
    public void visit(AllTableColumns allTableColumns) {
    }

    @Override
    public void visit(SelectExpressionItem selectExpressionItem) {
        selectExpressionItem.getExpression().accept(expressionVisitor);

    }

    @Override
    public void visit(SubSelect subSelect) {
        if (subSelect.getWithItemsList() != null && !subSelect.getWithItemsList().isEmpty()) {
            for (Iterator<WithItem> iter = subSelect.getWithItemsList().iterator(); iter.hasNext(); ) {
                WithItem withItem = iter.next();
                withItem.accept(this);
            }
        }
        subSelect.getSelectBody().accept(this);
        Pivot pivot = subSelect.getPivot();
        if (pivot != null) {
            pivot.accept(this);
        }
    }

    @Override
    public void visit(Table table) {
        String name = table.getName();

        if (!meta.addCDBTable(name))
            meta.setException(new RewriteCDBException(name + "Table not found"));
    }

    @Override
    public void visit(Pivot pivot) {
    }

    @Override
    public void visit(PivotXml pivot) {
        List<Column> forColumns = pivot.getForColumns();

    }

    public void deparseOrderBy(List<OrderByElement> orderByElements) {
        deparseOrderBy(false, orderByElements);
    }

    public void deparseOrderBy(boolean oracleSiblings, List<OrderByElement> orderByElements) {
        for (Iterator<OrderByElement> iter = orderByElements.iterator(); iter.hasNext(); ) {
            OrderByElement orderByElement = iter.next();
            orderByElement.accept(this);
        }
    }

    public void deparseLimit(Limit limit) {

    }

    public void deparseOffset(Offset offset) {

    }

    public void deparseFetch(Fetch fetch) {

    }

    public ExpressionVisitor getExpressionVisitor() {
        return expressionVisitor;
    }

    public void setExpressionVisitor(ExprChecker visitor) {
        expressionVisitor = visitor;
    }

    @Override
    public void visit(SubJoin subjoin) {
        subjoin.getLeft().accept(this);
        checkJoin(subjoin.getJoin());

        if (subjoin.getPivot() != null) {
            subjoin.getPivot().accept(this);
        }
    }

    public void checkJoin(Join join) {
        FromItem fromItem = join.getRightItem();

        if (!join.isSimple())
            expressionVisitor.setCurTypeScope(new TypeScope(TypeScope.ExprType.EXPR_DETJOIN));

        fromItem.accept(this);

        if (join.getOnExpression() == null && !join.isSimple()) {
            meta.setException(new RewriteCDBException("JOIN needs ON"));
            return;
        }

        if (join.getOnExpression() != null) {
            join.getOnExpression().accept(expressionVisitor);
        }

        if (join.getUsingColumns() != null) {
            meta.setException(new NotSupportedCDBException("USING"));
        }
    }

    public void addJoinTables(Join join) {
        if (!join.isSimple()) {
            FromItem fromItem = join.getRightItem();
            fromItem.accept(this);
        }
    }

    @Override
    public void visit(SetOperationList list) {
        for (int i = 0; i < list.getPlainSelects().size(); i++) {
            PlainSelect plainSelect = list.getPlainSelects().get(i);
            plainSelect.accept(this);
        }
        if (list.getOrderByElements() != null) {
            deparseOrderBy(list.getOrderByElements());
        }

        if (list.getLimit() != null) {
            deparseLimit(list.getLimit());
        }
        if (list.getOffset() != null) {
            deparseOffset(list.getOffset());
        }
        if (list.getFetch() != null) {
            deparseFetch(list.getFetch());
        }
    }

    @Override
    public void visit(WithItem withItem) {
        withItem.getSelectBody().accept(this);
    }

    @Override
    public void visit(LateralSubSelect lateralSubSelect) {
    }

    @Override
    public void visit(ValuesList valuesList) {

    }

    @Override
    public void visit(AllColumns allColumns) {
        meta.addAllColumnsFromCurrentTables();
    }
}
