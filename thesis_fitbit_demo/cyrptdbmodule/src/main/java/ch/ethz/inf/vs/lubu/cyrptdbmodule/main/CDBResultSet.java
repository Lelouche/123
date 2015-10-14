package ch.ethz.inf.vs.lubu.cyrptdbmodule.main;

import android.util.Base64;
import android.util.SparseArray;

import java.util.ListIterator;
import java.util.Map;

import ch.ethz.inf.vs.lubu.cyrptdbmodule.crypto.EncLayer;
import ch.ethz.inf.vs.lubu.cyrptdbmodule.crypto.ICryptoManager;
import ch.ethz.inf.vs.lubu.cyrptdbmodule.dbscheme.CDBColumn;
import ch.ethz.inf.vs.lubu.cyrptdbmodule.dbscheme.CDBField;
import ch.ethz.inf.vs.lubu.cyrptdbmodule.dbscheme.DBSchemeManager;
import ch.ethz.inf.vs.lubu.cyrptdbmodule.exceptions.CDBException;
import ch.ethz.inf.vs.lubu.cyrptdbmodule.util.CDBUtil;
import ch.ethz.inf.vs.lubu.cyrptdbmodule.util.DBType;
import ch.ethz.inf.vs.lubu.cyrptdbmodule.util.Setting;

/**
 * Created by lukas on 23.03.15.
 * The CDBResultSet represents a result of a performed query.
 * Data can be accessed via iterating the rows and use the corresponding
 * getter methods to access the data from the different columns
 */
public abstract class CDBResultSet {

    protected ICryptoManager cryptoMan;

    protected DBSchemeManager schemeMan;

    protected ListIterator<CDBResultSetRow> rowIter;

    protected CDBResultSetRow curRow = null;

    protected CDBColumn[] colInRow;

    protected SparseArray<String> funcPrefix;

    protected EncLayer.EncLayerType[] encTypeCol;

    protected Map<String, EncLayer> encLayerCache;

    /**
     * Get the number of columns in this set
     * @return number of columns
     */
    public int getNumCols() {
        return colInRow.length;
    }

    protected boolean checkBounds(int index) throws CDBException {
        if (index >= 0 && index < colInRow.length)
            return true;
        else
            throw new CDBException("Index not in Range " + index);
    }

    protected int getCDBColumnFromLabel(String label) throws CDBException {
        if (label.matches(CDBUtil.FUNC_TAB_COL_PATTERN)) {
            String[] split = CDBUtil.getFuncTableColRef(label);
            for (int ind = 0; ind < colInRow.length; ind++) {
                String func = funcPrefix.get(ind);
                if (func != null)
                    func = CDBUtil.transformToNormalDBFUNC(func);
                boolean isEqual = split[1].equals(colInRow[ind].getRealName())
                        && split[0].equals(colInRow[ind].getBelongsTo().getRealName())
                        && func != null
                        && (func.equals(split[2].toUpperCase()));
                if (isEqual)
                    return ind;
            }
        } else if (label.matches(CDBUtil.TAB_COL_PATTERN)) {
            String[] split = CDBUtil.getTableColRef(label);
            for (int ind = 0; ind < colInRow.length; ind++) {
                boolean isEqual = split[1].equals(colInRow[ind].getRealName())
                        && split[0].equals(colInRow[ind].getBelongsTo().getRealName());
                if (isEqual)
                    return ind;
            }
        } else {
            for (int ind = 0; ind < colInRow.length; ind++) {
                if (label.equals(colInRow[ind].getRealName()))
                    return ind;
            }
        }
        throw new CDBException("Column " + label + "not found");
    }

    /**
     * Get the name of the column with this id
     * @param col column index
     * @return name of the column
     * @throws CDBException
     */
    public String getColumnName(int col) throws CDBException {
        checkBounds(col);
        return colInRow[col].getRealName();
    }

    /**
     * Get the name of the table of this column
     * @param col column id
     * @return Table name
     * @throws CDBException
     */
    public String getTableName(int col) throws CDBException {
        checkBounds(col);
        return colInRow[col].getBelongsTo().getRealName();
    }

    /**
     * Move iterator to the next row. The iterator is first on null.
     * @return true if it has a next row, false if we reached the end of the set
     */
    public boolean next() {
        if (rowIter.hasNext()) {
            curRow = rowIter.next();
            return true;
        }
        return false;
    }

    /**
     * Move the iterator to the previous row.
     * @return true if it has a previous row, false if we reached the beginning of the set
     */
    public boolean previous() {
        if (rowIter.hasPrevious()) {
            curRow = rowIter.previous();
            return true;
        }
        return false;
    }

    /**
     * Move the iterator to the first row
     * @return
     */
    public abstract boolean first();

    /**
     * Move the iterator to the last row
     * @return
     */
    public abstract boolean last();

    protected String getDecryptedData(int realCol) throws CDBException {
        CDBColumn curCol = colInRow[realCol];
        try {
            if (Setting.CRYPTO_ON) {
                EncLayer.EncLayerType type = encTypeCol[realCol];
                String hashTabName = curCol.getHashName(type);
                String ivColName = curCol.getColIVName();

                String pref = this.funcPrefix.get(realCol);
                if (pref != null && !CDBUtil.isUDFSUMorSUM(pref) && !CDBUtil.ismOPEudfMinMax(pref)) {
                    return curRow.getValue(CDBUtil.functionize(pref, hashTabName));
                }

                String dataEnc = "";
                if (pref == null) {
                    dataEnc = curRow.getValue(hashTabName);
                } else {
                    if(CDBUtil.ismOPEudfMinMax(pref))
                        type = curCol.getMainType();
                    dataEnc = curRow.getValue(CDBUtil.functionize(pref, hashTabName));
                }

                String iv = null;
                CDBField field = new CDBField(curCol);
                if (type.isRND()) {
                    iv = curRow.getValue(ivColName);
                    field.setSalt(iv);
                }

                EncLayer layer;
                int predictedLength = predictDataLength(curCol.getType(), dataEnc);
                String encTag = hashTabName + cryptoMan.getEnclayerTag(type, predictedLength);
                if (encLayerCache.containsKey(encTag)) {
                    layer = encLayerCache.get(encTag);
                } else {
                    if (type.hasMultipleLayers()) {
                        layer = cryptoMan.getEncLayer(type, curCol, predictedLength);
                    } else {
                        layer = cryptoMan.getEncLayer(type, curCol);
                    }
                    encLayerCache.put(encTag, layer);
                }
                field.setOutputType(layer.getStrType(curCol.getType()));
                field.setValueFromDB(dataEnc);
                field = layer.decrypt(field);
                if (curCol.getType().isInteger())
                    field.setOutputType(CDBField.DBStrType.NUM);
                else
                    field.setOutputType(CDBField.DBStrType.PLAINSTR);

                return field.getStrRep();
            } else {
                return curRow.getValue(String.valueOf(realCol));
            }
        } catch (Exception e) {
            throw new CDBException("Error occurred during decryption: " + e.getMessage());
        }
    }

    private int predictDataLength(DBType type, String dbValue) {
        if (type.isInteger())
            return 8;
        else {
            byte[] bytes = Base64.decode(dbValue, Base64.NO_WRAP);
            return bytes.length;
        }
    }

    /**
     * Get the data in String format from the corresponding column
     * @param col Name of the column as string
     * @return Data in String format
     * @throws CDBException
     */
    public String getString(String col) throws CDBException {
        int ind = getCDBColumnFromLabel(col);
        return getString(ind);
    }

    /**
     * Get the data as Integer from the corresponding column
     * @param col Name of the column as string
     * @return Data as Integer
     * @throws CDBException
     */
    public int getInt(String col) throws CDBException {
        int ind = getCDBColumnFromLabel(col);
        return getInt(ind);
    }

    /**
     * Get the data in String format from the corresponding column
     * @param col index of the column
     * @return Data in String format
     * @throws CDBException
     */
    public String getString(int col) throws CDBException {
        checkBounds(col);
        return getDecryptedData(col);
    }

    /**
     * Get the data as Integer from the corresponding column
     * @param col index of the column
     * @return Data as Integer
     * @throws CDBException
     */
    public int getInt(int col) throws CDBException {
        int res;
        String strRes;
        checkBounds(col);
        strRes = getDecryptedData(col);
        try {
            res = Integer.parseInt(strRes);
        } catch (NumberFormatException e) {
            throw new CDBException("Could not transform to Java int " + strRes);
        }
        return res;
    }

}
