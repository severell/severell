package com.mitchdennett.framework.database.grammar;

import com.mitchdennett.framework.database.migrations.Blueprint;
import com.mitchdennett.framework.database.migrations.ColumnDefinition;

import java.util.ArrayList;
import java.util.Arrays;

public abstract class Grammar {

    public abstract String create(Blueprint table);

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

    protected String[] prefixArray(String[] list, String prefix) {
        return Arrays.stream(list).map((val) -> prefix + " " + val).toArray(String[]::new);
    }


    public abstract String typeString(ColumnDefinition col);

    public abstract String typeBigInteger(ColumnDefinition col);

    public abstract String typeTimestamp(ColumnDefinition c);

    public abstract String generatableColumn(String type, ColumnDefinition col);

    public abstract  String modifyNullable(ColumnDefinition col);

    public Modifier[] getModifiers() {
        return new Modifier[]{};
    }

    public abstract String drop(Blueprint table);

    public abstract String compileTableExists();

    public abstract String typeBinary(ColumnDefinition c);

    public abstract String typeBool(ColumnDefinition c);

    public abstract String typeInteger(ColumnDefinition c);

    public abstract String typeText(ColumnDefinition c);

    public abstract String typeDecimal(ColumnDefinition c);

    public abstract String typeDouble(ColumnDefinition c);

    public abstract String typeDate(ColumnDefinition c);

    public abstract String add(Blueprint table);
}
