package ch.ethz.inf.vs.lubu.cyrptdbmodule.dbscheme;

import com.google.common.collect.ImmutableMap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import ch.ethz.inf.vs.lubu.cyrptdbmodule.crypto.EncLayer;
import ch.ethz.inf.vs.lubu.cyrptdbmodule.util.DBType;

/**
 * Created by lukas on 04.03.15.
 * Represents a column in the DBScheme
 */
public class CDBColumn {

    private String realName;

    private String colIVName;

    private CDBTable belongsTo;

    private DBType type;

    private List<CDBColumn> joinable = new ArrayList<CDBColumn>();

    private ImmutableMap<EncLayer.EncLayerType, String> typesToHashName;

    public CDBColumn() {
    }

    public CDBColumn(String realName,
                     String colIVName,
                     DBType type,
                     CDBTable belongsTo,
                     Map<EncLayer.EncLayerType, String> subCols) {

        this.realName = realName;
        this.colIVName = colIVName;
        this.type = type;
        this.belongsTo = belongsTo;
        ImmutableMap.Builder<EncLayer.EncLayerType, String> builder = ImmutableMap.builder();
        typesToHashName = builder.putAll(subCols).build();
    }

    public String getRealName() {
        return realName;
    }

    public void setRealName(String realName) {
        this.realName = realName;
    }

    public String getHashName(EncLayer.EncLayerType type) {
        String res = typesToHashName.get(type);
        if (res == null && type.isDeterministic())
            res = typesToHashName.get(getMainType());
        return res;
    }

    public boolean supportsType(EncLayer.EncLayerType type) {
        return typesToHashName.containsKey(type);
    }

    public boolean isPlain() {
        return supportsType(EncLayer.EncLayerType.PLAIN);
    }

    public String getColIVName() {
        return colIVName;
    }

    public boolean hasIV() {
        return colIVName != null;
    }

    public void setColIVName(String colIVName) {
        this.colIVName = colIVName;
    }

    public List<EncLayer.EncLayerType> getTypes() {
        List<EncLayer.EncLayerType> res = new ArrayList<>();
        EncLayer.EncLayerType[] orderTypes = new EncLayer.EncLayerType[3];
        for (EncLayer.EncLayerType type : typesToHashName.keySet())
            if (type.isMain())
                orderTypes[0] = type;
            else if (type.isHOM())
                orderTypes[1] = type;
            else if (type.isOPE())
                orderTypes[2] = type;
        for (EncLayer.EncLayerType type : orderTypes)
            if (type != null)
                res.add(type);
        return res;
    }

    public Collection<String> getHashNames() {
        return typesToHashName.values();
    }

    public DBType getType() {
        return type;
    }

    public void setType(DBType type) {
        this.type = type;
    }

    public CDBTable getBelongsTo() {
        return belongsTo;
    }

    public void setBelongsTo(CDBTable belongsTo) {
        this.belongsTo = belongsTo;
    }

    public EncLayer.EncLayerType getEncType(String colHash) {
        for (Map.Entry<EncLayer.EncLayerType, String> entry : typesToHashName.entrySet()) {
            if (entry.getValue().equals(colHash))
                return entry.getKey();
        }
        return null;
    }

    public boolean isIV(String hashName) {
        return hashName.equals(colIVName);
    }

    public void addJoinableCol(CDBColumn col) {
        this.joinable.add(col);
    }

    public List<CDBColumn> getJoinableCols() {
        return joinable;
    }

    public boolean isJoinable() {
        return !joinable.isEmpty();
    }

    public EncLayer.EncLayerType getMainType() {
        for (EncLayer.EncLayerType encType : getTypes()) {
            if (encType.isMain()) {
                return encType;
            }
        }
        return null;
    }


}
