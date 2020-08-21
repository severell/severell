package com.mitchdennett.framework.database.grammar;

import com.mitchdennett.framework.database.migrations.Blueprint;
import com.mitchdennett.framework.database.migrations.ColumnDefinition;

import java.util.ArrayList;

public class Grammar {

    public String create(Blueprint table) {
        return null;
    }

    public String[] getColumns(Blueprint table) {
        ArrayList<String> cols = new ArrayList<String>();

        for(ColumnDefinition col : table.getColumnDefinitions()) {
            String sql = col.compile(this);
            if(sql != null) {
                sql = col.getName() + " " + sql;
                sql = this.addModifiers(sql, table, col);
                cols.add(sql);
            }
        }
        String[] colArray = new String[cols.size()];
        return cols.toArray(colArray);
    }

    public String addModifiers(String sql, Blueprint table, ColumnDefinition col) {
        for(Modifier mode : getModifiers()) {
            sql += mode.compile(this, col);
        }

        return sql;
    }


    public String typeString(ColumnDefinition col) {
        return null;
    }

    public String typeBigInteger(ColumnDefinition col) {
        return null;
    }

    public String typeTimestamp(ColumnDefinition c) {
        return null;
    }

    public String generatableColumn(String type, ColumnDefinition col) {
        return null;
    }

    public String modifyNullable(ColumnDefinition col) {
        return null;
    }

    public Modifier[] getModifiers() {
        return new Modifier[]{};
    }

    public String drop(Blueprint table) {
        return null;
    }

    public String compileTableExists() {
        return null;
    }

}
