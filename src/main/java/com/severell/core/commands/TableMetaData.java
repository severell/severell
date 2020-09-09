package com.severell.core.commands;

import java.util.ArrayList;

public class TableMetaData {

    private ArrayList<ColumnMetaData> columns;
    private String tableName;


    public TableMetaData(String tableName) {
        this.tableName = tableName;
    }

    public ArrayList<ColumnMetaData> getColumns() {
        return columns;
    }

    public void setColumns(ArrayList<ColumnMetaData> columns) {
        this.columns = columns;
    }

    public String getTableName() {
        return tableName;
    }
}
