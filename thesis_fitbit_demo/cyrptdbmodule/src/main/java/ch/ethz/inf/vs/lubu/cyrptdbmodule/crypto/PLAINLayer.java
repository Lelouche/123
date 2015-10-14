package ch.ethz.inf.vs.lubu.cyrptdbmodule.crypto;

import ch.ethz.inf.vs.lubu.cyrptdbmodule.dbscheme.CDBField;
import ch.ethz.inf.vs.lubu.cyrptdbmodule.exceptions.CDBException;
import ch.ethz.inf.vs.lubu.cyrptdbmodule.util.DBType;

/**
 * Created by lukas on 04.05.15.
 */
public class PLAINLayer extends EncLayer {

    @Override
    public CDBField encrypt(CDBField field) throws CDBException {
        DBType colType = field.getType().getType();
        if (colType.isInteger())
            field.setOutputType(CDBField.DBStrType.SIGNEDNUM);
        else
            field.setOutputType(CDBField.DBStrType.PLAINSTR);
        return field;
    }

    @Override
    public CDBField decrypt(CDBField field) throws CDBException {
        DBType colType = field.getType().getType();
        if (colType.isInteger())
            field.setOutputType(CDBField.DBStrType.NUM);
        else
            field.setOutputType(CDBField.DBStrType.PLAINSTR);
        return field;
    }

    @Override
    public CDBField.DBStrType getStrType(DBType type) {
        if (type.isInteger())
            return CDBField.DBStrType.NUM;
        else
            return CDBField.DBStrType.PLAINSTR;
    }

}
