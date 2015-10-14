package ch.ethz.inf.vs.lubu.cyrptdbmodule.crypto;

import ch.ethz.inf.vs.lubu.cyrptdbmodule.dbscheme.CDBField;
import ch.ethz.inf.vs.lubu.cyrptdbmodule.exceptions.CDBException;
import ch.ethz.inf.vs.lubu.cyrptdbmodule.util.DBType;

/**
 * Created by lukas on 23.03.15.
 */
public abstract class EncLayer {

    protected EncLayerType type;

    public EncLayerType getEncLayerType() {
        return type;
    }

    public abstract CDBField encrypt(CDBField field) throws CDBException;

    public abstract CDBField decrypt(CDBField field) throws CDBException;

    public abstract CDBField.DBStrType getStrType(DBType type);

    public enum EncLayerType {
        PLAIN,
        HOM,
        DET,
        DET_JOIN,
        RND,
        OPE;

        public static EncLayerType getEncType(String type) {
            EncLayerType res = null;
            String upper = type.toUpperCase();
            switch (upper) {
                case "PLAIN":
                    res = PLAIN;
                    break;
                case "HOM":
                    res = HOM;
                    break;
                case "DET":
                    res = DET;
                    break;
                case "RND":
                    res = RND;
                    break;
                case "OPE":
                    res = OPE;
                    break;
                case "DET_JOIN":
                    res = DET_JOIN;
                    break;
                default:
                    break;
            }
            return res;
        }

        public boolean isMain() {
            return this.isDeterministic() || this.equals(RND) || this.equals(PLAIN);
        }

        public boolean isDeterministic() {
            return this.equals(DET_JOIN) || this.equals(DET) || this.equals(PLAIN);
        }

        public boolean hasMultipleLayers() {
            return this.isRND() || this.isDeterministic();
        }

        public boolean isOPE() {
            return this.equals(OPE) || this.equals(PLAIN);
        }

        public boolean isHOM() {
            return this.equals(HOM) || this.equals(PLAIN);
        }

        public boolean isJOIN() {
            return this.equals(DET_JOIN) || this.equals(PLAIN);
        }

        public boolean isRND() {
            return this.equals(RND);
        }

        public boolean isPLAIN() {
            return this.equals(PLAIN);
        }

    }
}
