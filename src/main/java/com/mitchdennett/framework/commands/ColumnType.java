package com.mitchdennett.framework.commands;

import java.sql.Timestamp;

public enum ColumnType {
    BIT(-7),
    TINYINT(-6),
    SMALLINT(5),
    INTEGER(4){
        @Override
        public Class getJavaType() {
            return int.class;
        }
    },
    BIGINT(-5),
    FLOAT(6),
    REAL(7),
    DOUBLE(8),
    NUMERIC(2),
    DECIMAL(3),
    CHAR(1),
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
    DATE(91),
    TIME(92),
    TIMESTAMP(93){
        @Override
        public Class getJavaType() {
            return Timestamp.class;
        }
    },
    BINARY(-2),
    VARBINARY(-3),
    LONGVARBINARY(-4),
    NULL(0),
    OTHER(1111),
    JAVA_OBJECT(2000),
    DISTINCT(2001),
    STRUCT(2002),
    ARRAY(2003),
    BLOB(2004),
    CLOB(2005),
    REF(2006),
    DATALINK(70),
    BOOLEAN(16),
    ROWID(-8),
    NCHAR(-15),
    NVARCHAR(-9),
    LONGNVARCHAR(-16),
    NCLOB(2011),
    SQLXML(2009),
    REF_CURSOR(2012),
    TIME_WITH_TIMEZONE(2013),
    TIMESTAMP_WITH_TIMEZONE(2014);

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
