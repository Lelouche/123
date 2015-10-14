package ch.ethz.inf.vs.lubu.cyrptdbmodule.util;

/**
 * Created by lukas on 23.03.15.
 * Represents all possible types in the MySql database
 */
public enum DBType {
    INT,
    TINYINT,
    SMALLINT,
    MEDIUMINT,
    BIGINT,
    FLOAT,
    DOUBLE,
    DECIMAL,
    DATE,
    DATETIME,
    TIMESTAMP,
    TIME,
    YEAR,
    CHAR,
    VARCHAR,
    BLOB,
    TEXT;

    /**
     * Returns the corresponding Enum type for that String,
     * null if not found
     * @param type String representation of the DBType
     * @return Enum type of the DB type
     */
    public static DBType getDBType(String type) {
        DBType res = null;
        String upper = type.toUpperCase();

        switch (upper) {
            case "INT":
                res = INT;
                break;
            case "TINYINT":
                res = TINYINT;
                break;
            case "SMALLINT":
                res = SMALLINT;
                break;
            case "MEDIUMINT":
                res = MEDIUMINT;
                break;
            case "BIGINT":
                res = BIGINT;
                break;
            case "FLOAT":
                res = FLOAT;
                break;
            case "DOUBLE":
                res = DOUBLE;
                break;
            case "DECIMAL":
                res = DECIMAL;
                break;
            case "DATETIME":
                res = DATETIME;
                break;
            case "TIMESTAMP":
                res = TIMESTAMP;
                break;
            case "TIME":
                res = TIME;
                break;
            case "YEAR":
                res = YEAR;
                break;
            case "CHAR":
                res = CHAR;
                break;
            case "VARCHAR":
                res = VARCHAR;
                break;
            case "BLOB":
                res = BLOB;
                break;
            case "TEXT":
                res = TEXT;
                break;
            default:
                break;
        }
        return res;
    }

    /**
     * Checks if the current type is an Integer
     * @return
     */
    public boolean isInteger() {
        boolean isInt =
                this == INT ||
                        this == TINYINT ||
                        this == MEDIUMINT ||
                        this == BIGINT;
        return isInt;
    }

    /**
     * Returns the size of the integer
     * @return
     */
    public int getSizeInteger() {
        switch (this) {
            case INT:
                return 4;
            case TINYINT:
                return 1;
            case SMALLINT:
                return 2;
            case MEDIUMINT:
                return 3;
            case BIGINT:
                return 8;
        }
        return -1;
    }


}

