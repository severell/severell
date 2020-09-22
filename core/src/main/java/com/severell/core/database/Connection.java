package com.severell.core.database;

import com.severell.core.commands.ColumnMetaData;
import org.apache.commons.dbcp2.BasicDataSource;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class Connection {

    private BasicDataSource dataSource;

    public Connection(BasicDataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * Get Table Meta Data
     * @param table table name
     * @return
     */
    public TableMetaData metaData(String table) {
        try(java.sql.Connection conn = dataSource.getConnection()){
            ArrayList<ColumnMetaData> columnMetaData = getColumnMetaData(table, conn);
            TableMetaData metaData = new TableMetaData(table);
            metaData.setColumns(columnMetaData);
            setPrimaryKeys(conn, metaData);
            return metaData;
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
       return null;
    }

    private void setPrimaryKeys(java.sql.Connection conn, TableMetaData metaData) throws SQLException {
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

    private ArrayList<ColumnMetaData> getColumnMetaData(String table, java.sql.Connection conn) throws SQLException {
        ResultSet rs = conn.getMetaData().getColumns(null, "public", table, null);
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
