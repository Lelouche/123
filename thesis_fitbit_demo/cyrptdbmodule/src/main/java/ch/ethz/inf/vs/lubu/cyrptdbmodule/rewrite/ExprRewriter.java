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
import net.sf.jsqlparser.expression.operators.relational.SupportsOldOracleJoinSyntax;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.select.SelectVisitor;
import net.sf.jsqlparser.statement.select.SubSelect;

import java.util.Iterator;

import ch.ethz.inf.vs.lubu.cyrptdbmodule.crypto.EncLayer;
import ch.ethz.inf.vs.lubu.cyrptdbmodule.crypto.HOMLayer;
import ch.ethz.inf.vs.lubu.cyrptdbmodule.dbscheme.CDBColumn;
import ch.ethz.inf.vs.lubu.cyrptdbmodule.dbscheme.CDBField;
import ch.ethz.inf.vs.lubu.cyrptdbmodule.exceptions.CDBException;
import ch.ethz.inf.vs.lubu.cyrptdbmodule.util.CDBUtil;
import ch.ethz.inf.vs.lubu.cyrptdbmodule.util.Setting;

/**
 * Created by lukas on 24.03.15.
 * Visitor template is taken from JSQLLibrary (LGPL V2.1.)
 * Rewrites Expressions in queries
 */
public class ExprRewriter implements ExpressionVisitor, ItemsListVisitor {

    /**
     * Metadata during rewriting to get Crypto and Col Hashes
     */
    private QueryMetadata meta;

    /**
     * String Buffer which the new query is written to
     */
    private StringBuilder buffer;

    private SelectVisitor selectVisitor;

    private boolean useBracketsInExprList = false;

    /**
     * Keeps track of the current EncType during visiting
     */
    private EncLayer.EncLayerType curEncType = null;

    public ExprRewriter() {
    }


    public ExprRewriter(SelectVisitor selectVisitor, QueryMetadata meta, StringBuilder buffer) {
        this.selectVisitor = selectVisitor;
        this.meta = meta;
        this.buffer = buffer;
    }

    public ExprRewriter(QueryMetadata meta, StringBuilder buffer) {
        this.meta = meta;
        this.buffer = buffer;
    }

    public StringBuilder getBuffer() {
        return buffer;
    }

    public void setBuffer(StringBuilder buffer) {
        this.buffer = buffer;
    }

    public void setCurEncType(EncLayer.EncLayerType curEncType) {
        this.curEncType = curEncType;
    }

    @Override
    public void visit(Addition addition) {
        EncLayer.EncLayerType before = curEncType;
        curEncType = EncLayer.EncLayerType.HOM;
        visitBinaryExpression(addition, " + ");
        curEncType = before;
    }

    @Override
    public void visit(AndExpression andExpression) {
        visitBinaryExpression(andExpression, " AND ");
    }

    @Override
    public void visit(Between between) {
        between.getLeftExpression().accept(this);
        if (between.isNot()) {
            buffer.append(" NOT");
        }

        buffer.append(" BETWEEN ");
        between.getBetweenExpressionStart().accept(this);
        buffer.append(" AND ");
        between.getBetweenExpressionEnd().accept(this);

    }

    @Override
    public void visit(EqualsTo equalsTo) {
        EncLayer.EncLayerType before = curEncType;
        curEncType = EncLayer.EncLayerType.DET;
        visitOldOracleJoinBinaryExpression(equalsTo, " = ");
        curEncType = before;
    }

    @Override
    public void visit(Division division) {
        visitBinaryExpression(division, " / ");

    }

    @Override
    public void visit(DoubleValue doubleValue) {
        buffer.append(doubleValue.toString());

    }

    public void visitOldOracleJoinBinaryExpression(OldOracleJoinBinaryExpression expression, String operator) {
        if (expression.isNot()) {
            buffer.append(" NOT ");
        }
        expression.getLeftExpression().accept(this);
        if (expression.getOldOracleJoinSyntax() == EqualsTo.ORACLE_JOIN_RIGHT) {
            buffer.append("(+)");
        }
        buffer.append(operator);
        expression.getRightExpression().accept(this);
        if (expression.getOldOracleJoinSyntax() == EqualsTo.ORACLE_JOIN_LEFT) {
            buffer.append("(+)");
        }
    }

    @Override
    public void visit(GreaterThan greaterThan) {
        EncLayer.EncLayerType before = curEncType;
        curEncType = EncLayer.EncLayerType.OPE;
        visitOldOracleJoinBinaryExpression(greaterThan, " > ");
        curEncType = before;
    }

    @Override
    public void visit(GreaterThanEquals greaterThanEquals) {
        EncLayer.EncLayerType before = curEncType;
        curEncType = EncLayer.EncLayerType.OPE;
        visitOldOracleJoinBinaryExpression(greaterThanEquals, " >= ");
        curEncType = before;

    }

    @Override
    public void visit(InExpression inExpression) {
        if (inExpression.getLeftExpression() == null) {
            inExpression.getLeftItemsList().accept(this);
        } else {
            inExpression.getLeftExpression().accept(this);
            if (inExpression.getOldOracleJoinSyntax() == SupportsOldOracleJoinSyntax.ORACLE_JOIN_RIGHT) {
                buffer.append("(+)");
            }
        }
        if (inExpression.isNot()) {
            buffer.append(" NOT");
        }
        buffer.append(" IN ");

        inExpression.getRightItemsList().accept(this);
    }

    @Override
    public void visit(SignedExpression signedExpression) {
        meta.isSigned = true;
        signedExpression.getExpression().accept(this);
        meta.isSigned = false;
    }

    @Override
    public void visit(IsNullExpression isNullExpression) {
        isNullExpression.getLeftExpression().accept(this);
        if (isNullExpression.isNot()) {
            buffer.append(" IS NOT NULL");
        } else {
            buffer.append(" IS NULL");
        }
    }

    @Override
    public void visit(JdbcParameter jdbcParameter) {
        buffer.append("?");

    }

    @Override
    public void visit(LikeExpression likeExpression) {
        visitBinaryExpression(likeExpression, " LIKE ");
        String escape = likeExpression.getEscape();
        if (escape != null) {
            buffer.append(" ESCAPE '").append(escape).append('\'');
        }
    }

    @Override
    public void visit(ExistsExpression existsExpression) {
        if (existsExpression.isNot()) {
            buffer.append("NOT EXISTS ");
        } else {
            buffer.append("EXISTS ");
        }
        existsExpression.getRightExpression().accept(this);
    }

    @Override
    public void visit(LongValue longValue) {
        if (meta.inInsert) {
            EncLayer enc;
            byte[] iv = null;
            TypeScope ts = meta.getPlainType(longValue);
            CDBColumn type = ts.getColType();
            for (EncLayer.EncLayerType encType : type.getTypes()) {
                CDBField field = new CDBField(type);
                if (meta.isSigned)
                    field.setValue(-longValue.getValue());
                else
                    field.setValue(longValue.getValue());
                try {
                    enc = meta.getCrypto().getEncLayer(encType, field.getType(), field.getSize());
                    field = enc.encrypt(field);
                    if (field.getSaltSmall() != null)
                        iv = field.getSaltSmall();
                    if(encType.isOPE() && Setting.USE_mOPE && !field.getType().isPlain())
                        handlemOPE(field);
                    else
                        buffer.append(field.getDBRepresentation());
                    buffer.append(", ");
                } catch (CDBException e) {
                    meta.setException(e);
                    return;
                }
            }
            if (iv != null)
                buffer.append(CDBUtil.bytesToPositiveNumString(iv));
            else
                buffer.setLength(buffer.length() - 2);
        } else {
            EncLayer enc;
            TypeScope ts = meta.getPlainType(longValue);
            CDBField field = new CDBField(ts.getColType());
            if (meta.isSigned)
                field.setValue(-longValue.getValue());
            else
                field.setValue(longValue.getValue());
            try {
                if (field.getType().isPlain())
                    enc = meta.getCrypto().getEncLayer(EncLayer.EncLayerType.PLAIN, field.getType(), field.getSize());
                else
                    enc = meta.getCrypto().getEncLayer(curEncType, field.getType(), field.getSize());
                field = enc.encrypt(field);
            } catch (CDBException e) {
                meta.setException(e);
                return;
            }
            if(curEncType.isOPE() && Setting.USE_mOPE && !field.getType().isPlain())
                handlemOPE(field);
            else
                buffer.append(field.getDBRepresentation());
        }
    }

    private boolean handlemOPE(CDBField encField) {
        meta.containsmOPE = true;
        String tag = CDBUtil.getmOPETag();
        meta.postmOPEWork(tag, meta.getSchemeManager().getSchemeName(),encField.getType(),encField.getDBRepresentation());
        buffer.append(tag);
        return true;
    }

    @Override
    public void visit(MinorThan minorThan) {
        EncLayer.EncLayerType old = curEncType;
        curEncType = EncLayer.EncLayerType.OPE;
        visitOldOracleJoinBinaryExpression(minorThan, " < ");
        curEncType = old;

    }

    @Override
    public void visit(MinorThanEquals minorThanEquals) {
        EncLayer.EncLayerType old = curEncType;
        curEncType = EncLayer.EncLayerType.OPE;
        visitOldOracleJoinBinaryExpression(minorThanEquals, " <= ");
        curEncType = old;
    }

    @Override
    public void visit(Multiplication multiplication) {
        visitBinaryExpression(multiplication, " * ");

    }

    @Override
    public void visit(NotEqualsTo notEqualsTo) {
        visitOldOracleJoinBinaryExpression(notEqualsTo, " " + notEqualsTo.getStringExpression() + " ");

    }

    @Override
    public void visit(NullValue nullValue) {
        buffer.append("NULL");

    }

    @Override
    public void visit(OrExpression orExpression) {
        visitBinaryExpression(orExpression, " OR ");

    }

    @Override
    public void visit(Parenthesis parenthesis) {
        if (parenthesis.isNot()) {
            buffer.append(" NOT ");
        }

        buffer.append("(");
        parenthesis.getExpression().accept(this);
        buffer.append(")");

    }

    @Override
    public void visit(StringValue stringValue) {
        if (meta.inInsert) {
            EncLayer enc;
            byte[] iv = null;
            TypeScope ts = meta.getPlainType(stringValue);
            CDBColumn type = ts.getColType();
            for (EncLayer.EncLayerType encType : type.getTypes()) {
                CDBField field = new CDBField(type);
                field.setValue(stringValue.getValue());
                try {
                    enc = meta.getCrypto().getEncLayer(encType, field.getType(), field.getSize());
                    field = enc.encrypt(field);
                    if (field.getSaltSmall() != null)
                        iv = field.getSaltSmall();
                    buffer.append(field.getDBRepresentation()).append(", ");
                } catch (CDBException e) {
                    meta.setException(e);
                    return;
                }
            }
            if (iv != null)
                buffer.append(CDBUtil.bytesToPositiveNumString(iv));
            else
                buffer.setLength(buffer.length() - 2);
        } else {
            EncLayer enc;
            TypeScope ts = meta.getPlainType(stringValue);
            CDBField field = new CDBField(ts.getColType());
            field.setValue(stringValue.getValue());
            try {
                if (field.getType().isPlain())
                    enc = meta.getCrypto().getEncLayer(EncLayer.EncLayerType.PLAIN, field.getType(), field.getSize());
                else
                    enc = meta.getCrypto().getEncLayer(curEncType, field.getType(), field.getSize());
                field = enc.encrypt(field);
            } catch (CDBException e) {
                meta.setException(e);
                return;
            }
            buffer.append(field.getDBRepresentation());

        }
    }

    @Override
    public void visit(Subtraction subtraction) {
        visitBinaryExpression(subtraction, " - ");

    }

    private void visitBinaryExpression(BinaryExpression binaryExpression, String operator) {
        if (binaryExpression.isNot()) {
            buffer.append(" NOT ");
        }
        binaryExpression.getLeftExpression().accept(this);
        buffer.append(operator);
        binaryExpression.getRightExpression().accept(this);

    }

    @Override
    public void visit(SubSelect subSelect) {
        buffer.append("(");
        subSelect.getSelectBody().accept(selectVisitor);
        buffer.append(")");
    }

    @Override
    public void visit(Column tableColumn) {
        final Table table = tableColumn.getTable();
        String ref;

        String tableName = null;
        if (table != null) {
            if (table.getAlias() != null) {
                tableName = table.getAlias().getName();
            } else {
                tableName = table.getFullyQualifiedName();
            }
        }

        if (tableName != null && !tableName.isEmpty()) {
            ref = tableName + "." + tableColumn.getColumnName();
        } else {
            ref = tableColumn.getColumnName();
        }

        try {

            if (meta.inSelect) {
                if (curEncType == null) {
                    String ivTab = meta.getFullHashColIVName(ref);
                    buffer.append(meta.getHashColNameMainType(ref));
                    if (ivTab != null) {
                        buffer.append(", ").append(ivTab);
                    }
                } else {
                    if (meta.inAGR) {
                        HOMLayer enc = (HOMLayer) meta.getCrypto().getEncLayer(EncLayer.EncLayerType.HOM, meta.getColumn(ref), 4);
                        buffer.append(enc.getAgrFunc(meta.getHashColNameByType(EncLayer.EncLayerType.HOM, ref)));
                    } else if(meta.inMAX || meta.inMIN) {
                        String mainCOL = meta.getHashColNameMainType(ref);
                        String func = "";
                        if(meta.inMAX)
                            func = "mOPE_MAX";
                        else
                            func = "mOPE_MIN";
                        String col = meta.getHashColNameByType(EncLayer.EncLayerType.OPE, ref);
                        buffer.append(func+"("+mainCOL+", "+col+")");
                    } else {
                        buffer.append(meta.getHashColNameByType(curEncType, ref));
                    }
                }

            } else {
                if (curEncType.isDeterministic())
                    buffer.append(meta.getHashColNameMainType(ref));
                else
                    buffer.append(meta.getHashColNameByType(curEncType, ref));
            }
        } catch (CDBException e) {
            meta.setException(e);
        }

    }

    @Override
    public void visit(Function function) {

        curEncType = CDBUtil.getEncTypeForFunc(function.getName());

        if (CDBUtil.isSUM(function.getName())) {
            meta.inAGR = true;
            visit(function.getParameters());
            meta.inAGR = false;
        } else if(Setting.USE_mOPE && CDBUtil.isMAX(function.getName())) {
            meta.inMAX = true;
            visit(function.getParameters());
            meta.inMAX = false;
        } else if(Setting.USE_mOPE && CDBUtil.isMIN(function.getName())) {
            meta.inMIN = true;
            visit(function.getParameters());
            meta.inMIN = false;
        } else {
            curEncType = EncLayer.EncLayerType.DET;
            meta.inSelect = false;
            buffer.append(function.getName()).append("(");
            visit(function.getParameters());
            buffer.append(")");
            meta.inSelect = true;
        }


        meta.inAGR = false;
        curEncType = null;
    }

    @Override
    public void visit(ExpressionList expressionList) {
        if (useBracketsInExprList) {
            buffer.append("(");
        }
        for (Iterator<Expression> iter = expressionList.getExpressions().iterator(); iter.hasNext(); ) {
            Expression expression = iter.next();
            expression.accept(this);
            if (iter.hasNext()) {
                buffer.append(", ");
            }
        }
        if (useBracketsInExprList) {
            buffer.append(")");
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
        buffer.append("{d '").append(dateValue.getValue().toString()).append("'}");
    }

    @Override
    public void visit(TimestampValue timestampValue) {
        buffer.append("{ts '").append(timestampValue.getValue().toString()).append("'}");
    }

    @Override
    public void visit(TimeValue timeValue) {
        buffer.append("{t '").append(timeValue.getValue().toString()).append("'}");
    }

    @Override
    public void visit(CaseExpression caseExpression) {
        buffer.append("CASE ");
        Expression switchExp = caseExpression.getSwitchExpression();
        if (switchExp != null) {
            switchExp.accept(this);
            buffer.append(" ");
        }

        for (Expression exp : caseExpression.getWhenClauses()) {
            exp.accept(this);
        }

        Expression elseExp = caseExpression.getElseExpression();
        if (elseExp != null) {
            buffer.append("ELSE ");
            elseExp.accept(this);
            buffer.append(" ");
        }

        buffer.append("END");
    }

    @Override
    public void visit(WhenClause whenClause) {
        buffer.append("WHEN ");
        whenClause.getWhenExpression().accept(this);
        buffer.append(" THEN ");
        whenClause.getThenExpression().accept(this);
        buffer.append(" ");
    }

    @Override
    public void visit(AllComparisonExpression allComparisonExpression) {
        buffer.append(" ALL ");
        allComparisonExpression.getSubSelect().accept((ExpressionVisitor) this);
    }

    @Override
    public void visit(AnyComparisonExpression anyComparisonExpression) {
        buffer.append(" ANY ");
        anyComparisonExpression.getSubSelect().accept((ExpressionVisitor) this);
    }

    @Override
    public void visit(Concat concat) {
        visitBinaryExpression(concat, " || ");
    }

    @Override
    public void visit(Matches matches) {
        visitOldOracleJoinBinaryExpression(matches, " @@ ");
    }

    @Override
    public void visit(BitwiseAnd bitwiseAnd) {
        visitBinaryExpression(bitwiseAnd, " & ");
    }

    @Override
    public void visit(BitwiseOr bitwiseOr) {
        visitBinaryExpression(bitwiseOr, " | ");
    }

    @Override
    public void visit(BitwiseXor bitwiseXor) {
        visitBinaryExpression(bitwiseXor, " ^ ");
    }

    @Override
    public void visit(CastExpression cast) {
        if (cast.isUseCastKeyword()) {
            buffer.append("CAST(");
            buffer.append(cast.getLeftExpression());
            buffer.append(" AS ");
            buffer.append(cast.getType());
            buffer.append(")");
        } else {
            buffer.append(cast.getLeftExpression());
            buffer.append("::");
            buffer.append(cast.getType());
        }
    }

    @Override
    public void visit(Modulo modulo) {
        visitBinaryExpression(modulo, " % ");
    }

    @Override
    public void visit(AnalyticExpression aexpr) {
        buffer.append(aexpr.toString());
    }

    @Override
    public void visit(ExtractExpression eexpr) {
        buffer.append(eexpr.toString());
    }

    @Override
    public void visit(MultiExpressionList multiExprList) {
        for (Iterator<ExpressionList> it = multiExprList.getExprList().iterator(); it.hasNext(); ) {
            it.next().accept(this);
            if (it.hasNext()) {
                buffer.append(", ");
            }
        }
    }

    @Override
    public void visit(IntervalExpression iexpr) {
        buffer.append(iexpr.toString());
    }

    @Override
    public void visit(JdbcNamedParameter jdbcNamedParameter) {
        buffer.append(jdbcNamedParameter.toString());
    }

    @Override
    public void visit(OracleHierarchicalExpression oexpr) {
        buffer.append(oexpr.toString());
    }

    @Override
    public void visit(RegExpMatchOperator rexpr) {
        visitBinaryExpression(rexpr, " " + rexpr.getStringExpression() + " ");
    }

    @Override
    public void visit(RegExpMySQLOperator rexpr) {
        visitBinaryExpression(rexpr, " " + rexpr.getStringExpression() + " ");
    }

    @Override
    public void visit(JsonExpression jsonExpr) {
        buffer.append(jsonExpr.toString());
    }

    @Override
    public void visit(WithinGroupExpression wgexpr) {
        buffer.append(wgexpr.toString());
    }

    @Override
    public void visit(UserVariable var) {
        buffer.append(var.toString());
    }
}
