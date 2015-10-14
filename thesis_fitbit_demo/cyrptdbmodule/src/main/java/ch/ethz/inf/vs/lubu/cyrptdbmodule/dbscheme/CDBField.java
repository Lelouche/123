package ch.ethz.inf.vs.lubu.cyrptdbmodule.dbscheme;

import android.util.Base64;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;

import ch.ethz.inf.vs.lubu.cyrptdbmodule.util.CDBUtil;

/**
 * Created by lukas on 23.03.15.
 * Represents a data field that stores a value in bytes, which can
 * be represented in multiple formats
 */
public class CDBField {

    private CDBColumn type;

    private byte[] value;

    private byte[] salt = null;

    private DBStrType outputType = DBStrType.NUM;

    public CDBField(CDBColumn type) {
        this.type = type;
    }

    public void setValue(String value) {
        try {
            this.value = value.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    public void setValue(BigInteger value) {
        this.value = value.toByteArray();
    }

    public void setValue(byte[] value) {
        this.value = value;
    }

    public void setValue(long value) {
        this.value = CDBUtil.transformSignedIntegerToBytes(value, type.getType());
    }

    public void setValueFromDB(String value) {
        String cur;
        switch (outputType) {
            case NUM:
                this.value = CDBUtil.NumStringToBytes(value);
                break;
            case SIGNEDNUM:
                this.value = CDBUtil.NumStringToBytes(value);
                break;
            case BASE64STR:
                this.value = Base64.decode(value, Base64.NO_WRAP);
                break;
            case ECPOINTSTR:
                try {
                    this.value = value.getBytes("UTF-8");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                break;
            case HEXSTR:
                this.value = CDBUtil.HexToBytes(value);
                break;
            case PLAINSTR:
                try {
                    this.value = value.getBytes("UTF-8");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                break;
        }
    }

    public int getSize() {
        if (type.getType().isInteger())
            return type.getType().getSizeInteger();
        if (value != null)
            return value.length + 1;
        return 0;
    }

    public CDBColumn getType() {
        return type;
    }

    public byte[] getCloneValue() {
        return value.clone();
    }

    public byte[] getSaltSmall() {
        return salt;
    }

    public byte[] getSaltBig() {
        if (salt == null)
            return null;
        return CDBUtil.toAESIV(salt);
    }

    public void setSalt(byte[] iv) {
        this.salt = iv;
    }

    public void setSalt(String iv) {
        byte[] ivByte = CDBUtil.NumStringToBytes(iv);
        this.setSalt(ivByte);
    }

    public String getBase64StringRep() {
        return Base64.encodeToString(value, Base64.NO_WRAP);
    }

    public String getNumRep() {
        return CDBUtil.bytesToSignedNumString(value);
    }

    public String getStrPlainRep() {
        try {
            return new String(value, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return null;
        }
    }

    public String getStrRep() {
        try {
            switch (outputType) {
                case NUM:
                    return getNumRep();
            }
            return new String(value, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return null;
        }
    }

    public String getDBRepresentation() {
        switch (outputType) {
            case NUM:
                return CDBUtil.bytesToPositiveNumString(value);
            case SIGNEDNUM:
                return CDBUtil.bytesToSignedNumString(value);
            case BASE64STR:
                return CDBUtil.stringify(getBase64StringRep());
            case ECPOINTSTR:
                return CDBUtil.stringify(getStrRep());
            case HEXSTR:
                return CDBUtil.stringify(CDBUtil.bytesToHex(value));
            case PLAINSTR:
                return CDBUtil.stringify(getStrPlainRep());
        }

        return null;
    }

    public BigInteger getUnsignedBigInteger() {
        if (value == null)
            return null;
        BigInteger res = new BigInteger(1, value);
        return res;
    }

    public BigInteger getSignedBigInteger() {
        if (value == null)
            return null;
        return new BigInteger(value);
    }


    public DBStrType getOutputType() {
        return outputType;
    }

    public void setOutputType(DBStrType outputType) {
        this.outputType = outputType;
    }


    public enum DBStrType {
        NUM,
        SIGNEDNUM,
        PLAINSTR,
        HEXSTR,
        ECPOINTSTR,
        BASE64STR;
    }
}
