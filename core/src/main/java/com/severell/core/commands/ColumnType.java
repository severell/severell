package com.severell.core.commands;

import java.math.BigDecimal;
import java.sql.*;

public enum ColumnType {
    BIT(-7){
        @Override
        public Class getJavaType() {
            return boolean.class;
        }
    },
    TINYINT(-6){
        @Override
        public Class getJavaType() {
            return short.class;
        }
    },
    SMALLINT(5){
        @Override
        public Class getJavaType() {
            return short.class;
        }
    },
    INTEGER(4){
        @Override
        public Class getJavaType() {
            return int.class;
        }
    },
    BIGINT(-5){
        @Override
        public Class getJavaType() {
            return long.class;
        }
    },
    FLOAT(6){
        @Override
        public Class getJavaType() {
            return double.class;
        }
    },
    REAL(7){
        @Override
        public Class getJavaType() {
            return float.class;
        }
    },
    DOUBLE(8){
        @Override
        public Class getJavaType() {
            return double.class;
        }
    },
    NUMERIC(2){
        @Override
        public Class getJavaType() {
            return BigDecimal.class;
        }
    },
    DECIMAL(3){
        @Override
        public Class getJavaType() {
            return BigDecimal.class;
        }
    },
    CHAR(1) {
        @Override
        public Class getJavaType() {
            return String.class;
        }
    },
    VARCHAR(12){
        @Override
        public Class getJavaType() {
            return String.class;
        }
    },
    LONGVARCHAR(-1){
        @Override
        public Class getJavaType() {
            return String.class;
        }
    },
    DATE(91){
        @Override
        public Class getJavaType() {
            return Date.class;
        }
    },
    TIME(92){
        @Override
        public Class getJavaType() {
            return Time.class;
        }
    },
    TIMESTAMP(93){
        @Override
        public Class getJavaType() {
            return Timestamp.class;
        }
    },
    BINARY(-2){
        @Override
        public Class getJavaType() {
            return Byte[].class;
        }
    },
    VARBINARY(-3){
        @Override
        public Class getJavaType() {
            return Byte[].class;
        }
    },
    LONGVARBINARY(-4){
        @Override
        public Class getJavaType() {
            return Byte[].class;
        }
    },
    NULL(0),
    OTHER(1111),
    JAVA_OBJECT(2000),
    DISTINCT(2001),
    STRUCT(2002){
        @Override
        public Class getJavaType() {
            return Struct.class;
        }
    },
    ARRAY(2003){
        @Override
        public Class getJavaType() {
            return Object[].class;
        }
    },
    BLOB(2004){
        @Override
        public Class getJavaType() {
            return byte[].class;
        }
    },
    CLOB(2005){
        @Override
        public Class getJavaType() {
            return String.class;
        }
    },
    REF(2006){
        @Override
        public Class getJavaType() {
            return Ref.class;
        }
    },
    DATALINK(70),
    BOOLEAN(16){
        @Override
        public Class getJavaType() {
            return boolean.class;
        }
    },
    ROWID(-8),
    NCHAR(-15),
    NVARCHAR(-9),
    LONGNVARCHAR(-16){
        @Override
        public Class getJavaType() {
            return String.class;
        }
    },
    NCLOB(2011),
    SQLXML(2009),
    REF_CURSOR(2012),
    TIME_WITH_TIMEZONE(2013){
        @Override
        public Class getJavaType() {
            return Time.class;
        }
    },
    TIMESTAMP_WITH_TIMEZONE(2014){
        @Override
        public Class getJavaType() {
            return Timestamp.class;
        }
    };

    private int type;

    ColumnType(int type){
        this.type = type;
    }

    public Class getJavaType() {
        return Object.class;
    }

    public static ColumnType valueOf(int type) {
        for (ColumnType c : values()) {
            if (c.type == type) {
                return c;
            }
        }
        // either throw the IAE or return null, your choice.
        throw new IllegalArgumentException(String.valueOf(type));
    }


}
