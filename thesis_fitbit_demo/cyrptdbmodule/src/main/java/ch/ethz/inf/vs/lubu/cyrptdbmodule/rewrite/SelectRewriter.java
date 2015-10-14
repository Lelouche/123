package ch.ethz.inf.vs.lubu.cyrptdbmodule.rewrite;

import net.sf.jsqlparser.expression.Alias;
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
import net.sf.jsqlparser.statement.select.Top;
import net.sf.jsqlparser.statement.select.ValuesList;
import net.sf.jsqlparser.statement.select.WithItem;

import java.util.Iterator;
import java.util.List;

import ch.ethz.inf.vs.lubu.cyrptdbmodule.crypto.EncLayer;
import ch.ethz.inf.vs.lubu.cyrptdbmodule.exceptions.CDBException;
import ch.ethz.inf.vs.lubu.cyrptdbmodule.exceptions.RewriteCDBException;

/**
 * Created by lukas on 24.03.15.
 * Visitor template is taken from JSQLLibrary (LGPL V2.1.)
 * Rewrites a Select statement
 */
public class SelectRewriter implements SelectVisitor, OrderByVisitor, SelectItemVisitor, FromItemVisitor, PivotVisitor {

    private ExprRewriter expressionVisitor;

    private QueryMetadata meta;

    private StringBuilder buffer;

    public SelectRewriter(ExprRewriter expressionVisitor, QueryMetadata meta, StringBuilder buffer) {
        this.expressionVisitor = expressionVisitor;
        this.meta = meta;
        this.buffer = buffer;
    }

    public SelectRewriter(QueryMetadata meta, StringBuilder buffer) {
        this.meta = meta;
        this.buffer = buffer;
    }

    public SelectRewriter(QueryMetadata meta) {
        this.meta = meta;
        this.buffer = new StringBuilder();
    }

    @Override
    public void visit(PlainSelect plainSelect) {
        buffer.append("SELECT ");
        if (plainSelect.getDistinct() != null) {
            buffer.append("DISTINCT ");
            if (plainSelect.getDistinct().getOnSelectItems() != null) {
                buffer.append("ON (");
                for (Iterator<SelectItem> iter = plainSelect.getDistinct().getOnSelectItems().iterator(); iter.hasNext(); ) {
                    SelectItem selectItem = iter.next();
                    selectItem.accept(this);
                    if (iter.hasNext()) {
                        buffer.append(", ");
                    }
                }
                buffer.append(") ");
            }

        }
        Top top = plainSelect.getTop();
        if (top != null) {
            buffer.append(top).append(" ");
        }

        for (Iterator<SelectItem> iter = plainSelect.getSelectItems().iterator(); iter.hasNext(); ) {
            meta.inSelect = true;
            SelectItem selectItem = iter.next();
            selectItem.accept(this);
            if (iter.hasNext()) {
                buffer.append(", ");
            }
            meta.inSelect = false;

        }

        if (plainSelect.getIntoTables() != null) {
            buffer.append(" INTO ");
            for (Iterator<Table> iter = plainSelect.getIntoTables().iterator(); iter.hasNext(); ) {
                visit(iter.next());
                if (iter.hasNext()) {
                    buffer.append(", ");
                }
            }
        }

        if (plainSelect.getFromItem() != null) {
            buffer.append(" FROM ");
            plainSelect.getFromItem().accept(this);
        }

        if (plainSelect.getJoins() != null) {
            for (Join join : plainSelect.getJoins()) {
                deparseJoin(join);
            }
        }

        if (plainSelect.getWhere() != null) {
            buffer.append(" WHERE ");
            plainSelect.getWhere().accept(expressionVisitor);
        }

        if (plainSelect.getOracleHierarchical() != null) {
            plainSelect.getOracleHierarchical().accept(expressionVisitor);
        }

        if (plainSelect.getGroupByColumnReferences() != null) {
            buffer.append(" GROUP BY ");
            for (Iterator<Expression> iter = plainSelect.getGroupByColumnReferences().iterator(); iter.hasNext(); ) {
                Expression columnReference = iter.next();
                expressionVisitor.setCurEncType(EncLayer.EncLayerType.DET);
                columnReference.accept(expressionVisitor);
                if (iter.hasNext()) {
                    buffer.append(", ");
                }
            }
        }

        if (plainSelect.getHaving() != null) {
            buffer.append(" HAVING ");
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
        if (plainSelect.isForUpdate()) {
            buffer.append(" FOR UPDATE");
        }

    }

    @Override
    public void visit(OrderByElement orderBy) {
        expressionVisitor.setCurEncType(EncLayer.EncLayerType.OPE);
        orderBy.getExpression().accept(expressionVisitor);
        expressionVisitor.setCurEncType(null);
        if (!orderBy.isAsc()) {
            buffer.append(" DESC");
        } else if (orderBy.isAscDescPresent()) {
            buffer.append(" ASC");
        }
        if (orderBy.getNullOrdering() != null) {
            buffer.append(' ');
            buffer.append(orderBy.getNullOrdering() == OrderByElement.NullOrdering.NULLS_FIRST ? "NULLS FIRST" : "NULLS LAST");
        }
    }

    public void visit(Column column) {
        buffer.append(column.getFullyQualifiedName());
    }

    @Override
    public void visit(AllTableColumns allTableColumns) {
        buffer.append(allTableColumns.getTable().getFullyQualifiedName()).append(".*");
    }

    @Override
    public void visit(SelectExpressionItem selectExpressionItem) {
        selectExpressionItem.getExpression().accept(expressionVisitor);
        if (selectExpressionItem.getAlias() != null) {
            buffer.append(selectExpressionItem.getAlias().toString());
        }

    }

    @Override
    public void visit(SubSelect subSelect) {
        buffer.append("(");
        if (subSelect.getWithItemsList() != null && !subSelect.getWithItemsList().isEmpty()) {
            buffer.append("WITH ");
            for (Iterator<WithItem> iter = subSelect.getWithItemsList().iterator(); iter.hasNext(); ) {
                WithItem withItem = iter.next();
                withItem.accept(this);
                if (iter.hasNext()) {
                    buffer.append(",");
                }
                buffer.append(" ");
            }
        }
        subSelect.getSelectBody().accept(this);
        buffer.append(")");
        Pivot pivot = subSelect.getPivot();
        if (pivot != null) {
            pivot.accept(this);
        }
        Alias alias = subSelect.getAlias();
        if (alias != null) {
            buffer.append(alias.toString());
        }
    }

    @Override
    public void visit(Table tableName) {
        try {
            buffer.append(meta.getFullHashTabName(tableName.getName()));
        } catch (CDBException e) {
            meta.setException(new RewriteCDBException("Table not found"));
        }
    }

    @Override
    public void visit(Pivot pivot) {
        List<Column> forColumns = pivot.getForColumns();
        buffer.append(" PIVOT (")
                .append(PlainSelect.getStringList(pivot.getFunctionItems()))
                .append(" FOR ")
                .append(PlainSelect.getStringList(forColumns, true, forColumns != null && forColumns.size() > 1))
                .append(" IN ")
                .append(PlainSelect.getStringList(pivot.getInItems(), true, true))
                .append(")");
    }

    @Override
    public void visit(PivotXml pivot) {
        List<Column> forColumns = pivot.getForColumns();
        buffer.append(" PIVOT XML (")
                .append(PlainSelect.getStringList(pivot.getFunctionItems()))
                .append(" FOR ")
                .append(PlainSelect.getStringList(forColumns, true, forColumns != null && forColumns.size() > 1))
                .append(" IN (");
        if (pivot.isInAny()) {
            buffer.append("ANY");
        } else if (pivot.getInSelect() != null) {
            buffer.append(pivot.getInSelect());
        } else {
            buffer.append(PlainSelect.getStringList(pivot.getInItems()));
        }
        buffer.append("))");
    }

    public void deparseOrderBy(List<OrderByElement> orderByElements) {
        deparseOrderBy(false, orderByElements);
    }

    public void deparseOrderBy(boolean oracleSiblings, List<OrderByElement> orderByElements) {

        buffer.append(" ORDER BY ");

        for (Iterator<OrderByElement> iter = orderByElements.iterator(); iter.hasNext(); ) {
            OrderByElement orderByElement = iter.next();
            orderByElement.accept(this);
            if (iter.hasNext()) {
                buffer.append(", ");
            }
        }
    }

    public void deparseLimit(Limit limit) {
        // LIMIT n OFFSET skip
        if (limit.isRowCountJdbcParameter()) {
            buffer.append(" LIMIT ");
            buffer.append("?");
        } else if (limit.getRowCount() >= 0) {
            buffer.append(" LIMIT ");
            buffer.append(limit.getRowCount());
        } else if (limit.isLimitNull()) {
            buffer.append(" LIMIT NULL");
        }

        if (limit.isOffsetJdbcParameter()) {
            buffer.append(" OFFSET ?");
        } else if (limit.getOffset() != 0) {
            buffer.append(" OFFSET ").append(limit.getOffset());
        }

    }

    public void deparseOffset(Offset offset) {
        // OFFSET offset
        // or OFFSET offset (ROW | ROWS)
        if (offset.isOffsetJdbcParameter()) {
            buffer.append(" OFFSET ?");
        } else if (offset.getOffset() != 0) {
            buffer.append(" OFFSET ");
            buffer.append(offset.getOffset());
        }
        if (offset.getOffsetParam() != null) {
            buffer.append(" ").append(offset.getOffsetParam());
        }

    }

    public void deparseFetch(Fetch fetch) {
        // FETCH (FIRST | NEXT) row_count (ROW | ROWS) ONLY
        buffer.append(" FETCH ");
        if (fetch.isFetchParamFirst()) {
            buffer.append("FIRST ");
        } else {
            buffer.append("NEXT ");
        }
        if (fetch.isFetchJdbcParameter()) {
            buffer.append("?");
        } else {
            buffer.append(fetch.getRowCount());
        }
        buffer.append(" ").append(fetch.getFetchParam()).append(" ONLY");

    }

    public StringBuilder getBuffer() {
        return buffer;
    }

    public void setBuffer(StringBuilder buffer) {
        this.buffer = buffer;
    }

    public ExpressionVisitor getExpressionVisitor() {
        return expressionVisitor;
    }

    public void setExpressionVisitor(ExprRewriter visitor) {
        expressionVisitor = visitor;
    }

    @Override
    public void visit(SubJoin subjoin) {
        buffer.append("(");
        subjoin.getLeft().accept(this);
        deparseJoin(subjoin.getJoin());
        buffer.append(")");

        if (subjoin.getPivot() != null) {
            subjoin.getPivot().accept(this);
        }
    }

    public void deparseJoin(Join join) {
        if (join.isSimple()) {
            buffer.append(", ");
        } else {

            if (join.isRight()) {
                buffer.append(" RIGHT");
            } else if (join.isNatural()) {
                buffer.append(" NATURAL");
            } else if (join.isFull()) {
                buffer.append(" FULL");
            } else if (join.isLeft()) {
                buffer.append(" LEFT");
            } else if (join.isCross()) {
                buffer.append(" CROSS");
            }

            if (join.isOuter()) {
                buffer.append(" OUTER");
            } else if (join.isInner()) {
                buffer.append(" INNER");
            }

            buffer.append(" JOIN ");

        }

        FromItem fromItem = join.getRightItem();
        fromItem.accept(this);
        if (join.getOnExpression() != null) {
            buffer.append(" ON ");
            join.getOnExpression().accept(expressionVisitor);
        }

    }

    @Override
    public void visit(SetOperationList list) {
        for (int i = 0; i < list.getPlainSelects().size(); i++) {
            if (i != 0) {
                buffer.append(' ').append(list.getOperations().get(i - 1)).append(' ');
            }
            buffer.append("(");
            PlainSelect plainSelect = list.getPlainSelects().get(i);
            plainSelect.accept(this);
            buffer.append(")");
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
        buffer.append(withItem.getName());
        if (withItem.getWithItemList() != null) {
            buffer.append(" ").append(PlainSelect.getStringList(withItem.getWithItemList(), true, true));
        }
        buffer.append(" AS (");
        withItem.getSelectBody().accept(this);
        buffer.append(")");
    }

    @Override
    public void visit(LateralSubSelect lateralSubSelect) {
        buffer.append(lateralSubSelect.toString());
    }

    @Override
    public void visit(ValuesList valuesList) {
        buffer.append(valuesList.toString());
    }

    @Override
    public void visit(AllColumns allColumns) {
        buffer.append(meta.getAllColNamesForStar());
    }
}
