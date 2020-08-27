package com.mitchdennett.framework.commands;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class TableMetaDataBuilder {

    public static TableMetaData create(String tableName, Connection conn) throws SQLException {
        TableMetaData metaData = new TableMetaData(tableName);
        metaData.setColumns(buildColumnMetaData(conn, tableName));
        setPrimaryKeys(conn, metaData);
        return metaData;
    }

    private static void setPrimaryKeys(Connection conn, TableMetaData metaData) throws SQLException {
        ResultSet pk = conn.getMetaData().getPrimaryKeys(null, null, metaData.getTableName());
        while(pk.next()) {
            for(ColumnMetaData col : metaData.getColumns()){
                if(col.getColumnName().equals(pk.getString("COLUMN_NAME"))){
                    col.setIsPrimaryKey(true);
                    break;
                }
            }
        }
    }

    private static ArrayList<ColumnMetaData> buildColumnMetaData(Connection conn, String tableName) throws SQLException {
        ResultSet rs = conn.getMetaData().getColumns(null, "public", tableName, null);
        ArrayList<ColumnMetaData> metaData = new ArrayList<>();
        while(rs.next()) {
            ColumnMetaData meta = new ColumnMetaData();
            meta.setColumnName(rs.getString("COLUMN_NAME"));
            meta.setDataType(rs.getInt("DATA_TYPE"));
            metaData.add(meta);
        }

        return metaData;
    }

}
