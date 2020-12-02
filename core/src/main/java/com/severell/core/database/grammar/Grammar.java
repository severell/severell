package com.severell.core.database.grammar;

import com.severell.core.database.migrations.Blueprint;
import com.severell.core.database.migrations.ColumnDefinition;
import com.severell.core.database.migrations.Command;

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
        String[] colArray = new String[getModifiers().length];
        int i = 0;
        for(Modifier mode : getModifiers()) {
            colArray[i] = mode.compile(this, col);
            i++;
        }

        sql += String.join(" ", colArray);
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

    public String modifyAutoIncrement(ColumnDefinition col) {
        return null;
    };

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

    public abstract String primary(Blueprint table, Command command);

    public String foreign(Blueprint table, Command command) {
        String sql = String.format("alter table %s add constraint %s ",
                table.getTableName(),
                command.param("index")
        );

        sql = sql + String.format("foreign key (%s) references %s (%s)",
                String.join(", ", (String[])command.param("columns")),
                command.param("on"),
                String.join(", ", (String[])command.param("references"))
        );

        return sql;
    }

    public abstract String typeSmallInteger(ColumnDefinition c);
}
