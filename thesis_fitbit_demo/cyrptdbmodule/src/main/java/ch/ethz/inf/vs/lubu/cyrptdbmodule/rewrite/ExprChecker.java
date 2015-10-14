package ch.ethz.inf.vs.lubu.cyrptdbmodule.rewrite;

import net.sf.jsqlparser.expression.AllComparisonExpression;
import net.sf.jsqlparser.expression.AnalyticExpression;
import net.sf.jsqlparser.expression.AnyComparisonExpression;
import net.sf.jsqlparser.expression.BinaryExpression;
import net.sf.jsqlparser.expression.CaseExpression;
import net.sf.jsqlparser.expression.CastExpression;
import net.sf.jsqlparser.expression.DateValue;
import net.sf.jsqlparser.expression.DoubleValue;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.ExpressionVisitor;
import net.sf.jsqlparser.expression.ExtractExpression;
import net.sf.jsqlparser.expression.Function;
import net.sf.jsqlparser.expression.IntervalExpression;
import net.sf.jsqlparser.expression.JdbcNamedParameter;
import net.sf.jsqlparser.expression.JdbcParameter;
import net.sf.jsqlparser.expression.JsonExpression;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.NullValue;
import net.sf.jsqlparser.expression.OracleHierarchicalExpression;
import net.sf.jsqlparser.expression.Parenthesis;
import net.sf.jsqlparser.expression.SignedExpression;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.expression.TimeValue;
import net.sf.jsqlparser.expression.TimestampValue;
import net.sf.jsqlparser.expression.UserVariable;
import net.sf.jsqlparser.expression.WhenClause;
import net.sf.jsqlparser.expression.WithinGroupExpression;
import net.sf.jsqlparser.expression.operators.arithmetic.Addition;
import net.sf.jsqlparser.expression.operators.arithmetic.BitwiseAnd;
import net.sf.jsqlparser.expression.operators.arithmetic.BitwiseOr;
import net.sf.jsqlparser.expression.operators.arithmetic.BitwiseXor;
import net.sf.jsqlparser.expression.operators.arithmetic.Concat;
import net.sf.jsqlparser.expression.operators.arithmetic.Division;
import net.sf.jsqlparser.expression.operators.arithmetic.Modulo;
import net.sf.jsqlparser.expression.operators.arithmetic.Multiplication;
import net.sf.jsqlparser.expression.operators.arithmetic.Subtraction;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.conditional.OrExpression;
import net.sf.jsqlparser.expression.operators.relational.Between;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.expression.operators.relational.ExistsExpression;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.expression.operators.relational.GreaterThan;
import net.sf.jsqlparser.expression.operators.relational.GreaterThanEquals;
import net.sf.jsqlparser.expression.operators.relational.InExpression;
import net.sf.jsqlparser.expression.operators.relational.IsNullExpression;
import net.sf.jsqlparser.expression.operators.relational.ItemsListVisitor;
import net.sf.jsqlparser.expression.operators.relational.LikeExpression;
import net.sf.jsqlparser.expression.operators.relational.Matches;
import net.sf.jsqlparser.expression.operators.relational.MinorThan;
import net.sf.jsqlparser.expression.operators.relational.MinorThanEquals;
import net.sf.jsqlparser.expression.operators.relational.MultiExpressionList;
import net.sf.jsqlparser.expression.operators.relational.NotEqualsTo;
import net.sf.jsqlparser.expression.operators.relational.OldOracleJoinBinaryExpression;
import net.sf.jsqlparser.expression.operators.relational.RegExpMatchOperator;
import net.sf.jsqlparser.expression.operators.relational.RegExpMySQLOperator;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.select.SelectVisitor;
import net.sf.jsqlparser.statement.select.SubSelect;

import java.util.Iterator;

import ch.ethz.inf.vs.lubu.cyrptdbmodule.exceptions.CDBException;
import ch.ethz.inf.vs.lubu.cyrptdbmodule.exceptions.NotSupportedCDBException;
import ch.ethz.inf.vs.lubu.cyrptdbmodule.exceptions.RewriteCDBException;
import ch.ethz.inf.vs.lubu.cyrptdbmodule.util.CDBUtil;

/**
 * Created by lukas on 24.03.15.
 * Visitor template is taken from JSQLLibrary (LGPL V2.1.)
 * Checks Expression in queries before Rewriting
 */
public class ExprChecker implements ExpressionVisitor, ItemsListVisitor {

    private SelectVisitor selectVisitor;

    private QueryMetadata meta;

    private TypeScope curTypeScope = null;

    public ExprChecker(SelectVisitor selectVisitor, QueryMetadata meta) {
        this.selectVisitor = selectVisitor;
        this.meta = meta;
    }

    public ExprChecker(QueryMetadata meta) {
        this.meta = meta;
    }

    public void setCurTypeScope(TypeScope curTypeScope) {
        this.curTypeScope = curTypeScope;
    }

    @Override
    public void visit(Addition addition) {
        TypeScope addScope = new TypeScope(TypeScope.ExprType.EXPR_HOM);
        visitBinaryExpression(addition, addScope);
    }

    @Override
    public void visit(AndExpression andExpression) {
        visitBinaryExpression(andExpression);
    }

    @Override
    public void visit(Between between) {
        setNotSupported("Between");
    }

    @Override
    public void visit(EqualsTo equalsTo) {
        TypeScope scope = new TypeScope(TypeScope.ExprType.EXPR_DETALL);
        visitBinaryExpression(equalsTo, scope);
    }

    @Override
    public void visit(Division division) {
        TypeScope scope = new TypeScope(TypeScope.ExprType.NO_COLL);
        visitBinaryExpression(division, scope);

    }

    @Override
    public void visit(DoubleValue doubleValue) {

    }

    public void visitOldOracleJoinBinaryExpression(OldOracleJoinBinaryExpression expression) {
        expression.getLeftExpression().accept(this);
        expression.getRightExpression().accept(this);
    }

    @Override
    public void visit(GreaterThan greaterThan) {
        TypeScope scope = new TypeScope(TypeScope.ExprType.EXPR_OPE);
        visitBinaryExpression(greaterThan, scope);
    }

    @Override
    public void visit(GreaterThanEquals greaterThanEquals) {
        TypeScope scope = new TypeScope(TypeScope.ExprType.EXPR_OPE);
        visitBinaryExpression(greaterThanEquals, scope);
    }

    @Override
    public void visit(InExpression inExpression) {
        if (inExpression.getLeftExpression() == null) {
            inExpression.getLeftItemsList().accept(this);
        } else {
            inExpression.getLeftExpression().accept(this);
        }
        inExpression.getRightItemsList().accept(this);
    }

    @Override
    public void visit(SignedExpression signedExpression) {
        signedExpression.getExpression().accept(this);
    }

    @Override
    public void visit(IsNullExpression isNullExpression) {
        isNullExpression.getLeftExpression().accept(this);
    }

    @Override
    public void visit(JdbcParameter jdbcParameter) {

    }

    @Override
    public void visit(LikeExpression likeExpression) {
        setNotSupported("Like");
        ;
    }

    @Override
    public void visit(ExistsExpression existsExpression) {
        existsExpression.getRightExpression().accept(this);
    }

    @Override
    public void visit(LongValue longValue) {
        if (curTypeScope != null) {
            meta.addPlainTypePair(longValue, curTypeScope);
        } else {
            meta.setException(new RewriteCDBException("No scope on value " + longValue.getStringValue()));
        }
    }

    @Override
    public void visit(MinorThan minorThan) {
        TypeScope scope = new TypeScope(TypeScope.ExprType.EXPR_OPE);
        visitBinaryExpression(minorThan, scope);

    }

    @Override
    public void visit(MinorThanEquals minorThanEquals) {
        TypeScope scope = new TypeScope(TypeScope.ExprType.EXPR_OPE);
        visitBinaryExpression(minorThanEquals, scope);

    }

    @Override
    public void visit(Multiplication multiplication) {
        TypeScope scope = new TypeScope(TypeScope.ExprType.NO_COLL);
        visitBinaryExpression(multiplication, scope);
    }

    @Override
    public void visit(NotEqualsTo notEqualsTo) {
        visitOldOracleJoinBinaryExpression(notEqualsTo);

    }

    @Override
    public void visit(NullValue nullValue) {

    }

    @Override
    public void visit(Function function) {
        String func = function.getName();
        if (!CDBUtil.isValidAgrFunction(func))
            meta.setException(new CDBException("Invalid function"));
        visit(function.getParameters());
    }

    @Override
    public void visit(OrExpression orExpression) {
        visitBinaryExpression(orExpression);
    }

    @Override
    public void visit(Parenthesis parenthesis) {
        parenthesis.getExpression().accept(this);
    }

    @Override
    public void visit(StringValue stringValue) {
        if (curTypeScope != null) {
            meta.addPlainTypePair(stringValue, curTypeScope);
        } else {
            meta.setException(new RewriteCDBException("No scope on value " + stringValue.getValue()));
        }
    }

    @Override
    public void visit(Subtraction subtraction) {
        TypeScope scope = new TypeScope(TypeScope.ExprType.EXPR_HOM);
        visitBinaryExpression(subtraction, scope);
    }

    private void visitBinaryExpression(BinaryExpression binaryExpression, TypeScope scope) {
        TypeScope temp = curTypeScope;
        curTypeScope = scope;
        binaryExpression.getLeftExpression().accept(this);
        binaryExpression.getRightExpression().accept(this);
        curTypeScope = temp;
        if (temp != null) {
            if (!temp.compareTo(scope))
                meta.setException(new RewriteCDBException("Type mismatch"));
        }
    }

    private void visitBinaryExpression(BinaryExpression binaryExpression) {
        binaryExpression.getLeftExpression().accept(this);
        binaryExpression.getRightExpression().accept(this);
    }

    @Override
    public void visit(SubSelect subSelect) {
        subSelect.getSelectBody().accept(selectVisitor);
    }

    @Override
    public void visit(ExpressionList expressionList) {
        for (Iterator<Expression> iter = expressionList.getExpressions().iterator(); iter.hasNext(); ) {
            Expression expression = iter.next();
            expression.accept(this);
        }
    }

    @Override
    public void visit(Column tableColumn) {
        final Table table = tableColumn.getTable();
        String tableName = null;
        if (table != null) {
            if (table.getAlias() != null)
                tableName = table.getAlias().getName();
            else
                tableName = table.getFullyQualifiedName();
        }

        if (!meta.addCDBColumn(tableName, tableColumn.getColumnName()))
            meta.setException(new RewriteCDBException(tableColumn.getColumnName() + "Column not found"));

        String ref;
        if (tableName != null && !tableName.isEmpty())
            ref = tableName + "." + tableColumn.getColumnName();
        else
            ref = tableColumn.getColumnName();

        if (this.curTypeScope != null) {
            try {
                curTypeScope.addColType(meta.getColumn(ref));
            } catch (CDBException e) {
                meta.setException(new RewriteCDBException(tableColumn.getColumnName() + " " + e.getMessage()));
            }
        }
    }

    public SelectVisitor getSelectVisitor() {
        return selectVisitor;
    }

    public void setSelectVisitor(SelectVisitor visitor) {
        selectVisitor = visitor;
    }

    @Override
    public void visit(DateValue dateValue) {
        setNotSupported("DateValue");
    }

    @Override
    public void visit(TimestampValue timestampValue) {
        setNotSupported("TimestampValue");
    }

    @Override
    public void visit(TimeValue timeValue) {
        setNotSupported("TimeValue");
    }

    @Override
    public void visit(CaseExpression caseExpression) {
        setNotSupported("CaseExpression");
    }

    @Override
    public void visit(WhenClause whenClause) {
        setNotSupported("WhenClause");
    }

    @Override
    public void visit(AllComparisonExpression allComparisonExpression) {
        allComparisonExpression.getSubSelect().accept((ExpressionVisitor) this);
    }

    @Override
    public void visit(AnyComparisonExpression anyComparisonExpression) {
        anyComparisonExpression.getSubSelect().accept((ExpressionVisitor) this);
    }

    @Override
    public void visit(Concat concat) {
        setNotSupported("Concat");
    }

    @Override
    public void visit(Matches matches) {
        setNotSupported("Matches");
    }

    @Override
    public void visit(BitwiseAnd bitwiseAnd) {
        setNotSupported("BitwiseAnd");
    }

    @Override
    public void visit(BitwiseOr bitwiseOr) {
        setNotSupported("BitwiseOr");
    }

    @Override
    public void visit(BitwiseXor bitwiseXor) {
        setNotSupported("BitwiseXor");
    }

    @Override
    public void visit(CastExpression cast) {
        if (cast.isUseCastKeyword()) {
        } else {
        }
    }

    @Override
    public void visit(Modulo modulo) {
        setNotSupported("Modulo");
    }

    @Override
    public void visit(AnalyticExpression aexpr) {
        setNotSupported("AnalyticExpression");
    }

    @Override
    public void visit(ExtractExpression eexpr) {
        setNotSupported("ExtractExpression");
    }

    @Override
    public void visit(MultiExpressionList multiExprList) {
        for (Iterator<ExpressionList> it = multiExprList.getExprList().iterator(); it.hasNext(); ) {
            it.next().accept(this);
        }
    }

    @Override
    public void visit(IntervalExpression iexpr) {
        setNotSupported("IntervalExpression");
    }

    @Override
    public void visit(JdbcNamedParameter jdbcNamedParameter) {
        setNotSupported("JdbcNamedParameter");
    }

    @Override
    public void visit(OracleHierarchicalExpression oexpr) {
        setNotSupported("OracleHierarchicalExpression");
    }

    @Override
    public void visit(RegExpMatchOperator rexpr) {
        setNotSupported("RegExpMatchOperator");
    }

    @Override
    public void visit(RegExpMySQLOperator rexpr) {
        setNotSupported("RegExpMySQLOperator");
    }

    @Override
    public void visit(JsonExpression jsonExpr) {
        setNotSupported("JsonExpression");
    }

    @Override
    public void visit(WithinGroupExpression wgexpr) {
        setNotSupported("WithinGroupExpression");
    }

    @Override
    public void visit(UserVariable var) {
        setNotSupported("WithinGroupExpression");
    }

    private void setNotSupported(String name) {
        meta.setException(new NotSupportedCDBException(name));
    }


}
