package ch.ethz.inf.vs.lubu.cyrptdbmodule.rewrite;

import java.util.ArrayList;
import java.util.List;

import ch.ethz.inf.vs.lubu.cyrptdbmodule.crypto.EncLayer;
import ch.ethz.inf.vs.lubu.cyrptdbmodule.dbscheme.CDBColumn;
import ch.ethz.inf.vs.lubu.cyrptdbmodule.exceptions.CDBException;
import ch.ethz.inf.vs.lubu.cyrptdbmodule.exceptions.RewriteCDBException;

/**
 * Created by lukas on 29.03.15.
 * A TypeScope keeps track of the expression type
 * during query checking.
 */
public class TypeScope {

    List<CDBColumn> types = new ArrayList<CDBColumn>();

    ExprType curType;

    public TypeScope(ExprType expr) {
        this.curType = expr;
    }

    public void addColType(CDBColumn col) throws CDBException {
        boolean noConflict = true;
        //EncLayer.EncLayerType type = col.getEncLayerType();

        if (curType.equals(ExprType.NO_COLL))
            throw new RewriteCDBException("Operation not supported on Columns");

        if (!types.isEmpty()) {
            if (curType.equals(ExprType.EXPR_DETALL))
                curType = ExprType.EXPR_DETJOIN;
            else if (!curType.equals(ExprType.EXPR_DETJOIN))
                throw new RewriteCDBException("Type conflict, diffrent Enclayer");
        }

        switch (curType) {
            case EXPR_DETALL:
                noConflict = col.supportsType(EncLayer.EncLayerType.DET_JOIN)
                        || col.supportsType(EncLayer.EncLayerType.DET)
                        || col.supportsType(EncLayer.EncLayerType.PLAIN);
                break;
            case EXPR_DET:
                noConflict = col.supportsType(EncLayer.EncLayerType.DET)
                        || col.supportsType(EncLayer.EncLayerType.PLAIN);
                break;
            case EXPR_OPE:
                noConflict = col.supportsType(EncLayer.EncLayerType.OPE)
                        || col.supportsType(EncLayer.EncLayerType.PLAIN);
                break;
            case EXPR_HOM:
                noConflict = col.supportsType(EncLayer.EncLayerType.HOM)
                        || col.supportsType(EncLayer.EncLayerType.PLAIN);
                break;
            case EXPR_DETJOIN:
                noConflict = col.supportsType(EncLayer.EncLayerType.DET_JOIN)
                        || col.supportsType(EncLayer.EncLayerType.PLAIN);
                break;
            case EXPR_NONE:
                break;
        }

        if (noConflict)
            types.add(col);
        else
            throw new CDBException("Type mismatch");
    }

    public void finalCheck() throws CDBException {
        if (types.isEmpty())
            this.curType = ExprType.EXPR_NONE;
        else if (types.size() > 1) {
            //TODO check joinable
        }
    }

    public CDBColumn getColType() {
        if (types.isEmpty())
            return null;
        else
            return types.get(0);
    }

    public boolean isJoin() {
        return this.types.size() > 1;
    }

    public List<CDBColumn> getColTypes() {
        return types;
    }

    public boolean compareTo(TypeScope other) {
        return other.curType.compare(this.curType);
    }


    public enum ExprType {
        EXPR_ALL,
        EXPR_DETALL,
        EXPR_DET,
        EXPR_OPE,
        EXPR_HOM,
        EXPR_DETJOIN,
        NO_COLL,
        EXPR_NONE;

        public boolean compare(ExprType other) {
            switch (this) {
                case EXPR_ALL:
                    return true;
                case EXPR_DETALL:
                    return other == EXPR_DET
                            || other == EXPR_ALL
                            || other == EXPR_DETJOIN;
                case EXPR_DET:
                    return other == EXPR_DET
                            || other == EXPR_ALL
                            || other == EXPR_DETJOIN;
                case EXPR_OPE:
                    return other == EXPR_OPE
                            || other == EXPR_ALL;
                case EXPR_HOM:
                    return other == EXPR_HOM
                            || other == EXPR_ALL;
                case EXPR_DETJOIN:
                    return other == EXPR_ALL
                            || other == EXPR_DETJOIN;
                case NO_COLL:
                    return other == NO_COLL;
                case EXPR_NONE:
                    return other == EXPR_NONE;
            }
            return false;
        }
    }

}
