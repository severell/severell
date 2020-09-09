package com.severell.core.database.grammar;

import com.severell.core.database.migrations.Blueprint;
import com.severell.core.database.migrations.ColumnDefinition;
import org.codehaus.plexus.util.StringUtils;


public class PostgresGrammar extends Grammar {

    protected Modifier[] modifiers = new Modifier[]{
            Modifier.NULLABLE,
    };

    @Override
    public String create(Blueprint table) {
        return String.format("%s table %s (%s)",
                "create",
                table.getTableName(),
                StringUtils.join(getColumns(table), ", ")
                );
    }

    @Override
    public String add(Blueprint table) {
        return String.format("%s table %s %s",
                "alter",
                table.getTableName(),
                StringUtils.join(prefixArray(getColumns(table), "add column"), ", ")
        );
    }

    @Override
    public String drop(Blueprint table) {
        return String.format("drop table %s", table.getTableName());
    }

    @Override
    public String typeTimestamp(ColumnDefinition col) {
        String sql = "timestamp";
        if(col.get("precision", Integer.class) > -1) {
            sql =  String.format( sql + "(%d)", col.get("precision", Integer.class));
        }

        sql += " without time zone";

        if(col.get("useCurrent", Boolean.class)) {
            sql += " default CURRENT_TIMESTAMP";
        }

        return sql;
    }

    @Override
    public String typeString(ColumnDefinition col) {
        return String.format("varchar(%s)", col.get("length"));
    }

    @Override
    public String typeBigInteger(ColumnDefinition col) {
        return generatableColumn("bigint", col);
    }

    public String generatableColumn(String type, ColumnDefinition col) {
        if(!col.get("autoIncrement", Boolean.class)) {
            return type;
        } else {
            switch (type) {
                case "integer":
                    return "serial";
                case "bigint":
                    return "bigserial";
                case "smallint":
                    return "smallserial";
                default:
                    return type;
            }
        }
    }

    @Override
    public String modifyNullable(ColumnDefinition col) {
        return col.get("nullable", Boolean.class) ? " null" : " not null";
    }

    @Override
    public Modifier[] getModifiers() {
        return modifiers;
    }

    @Override
    public String compileTableExists() {
        return "select * from information_schema.tables where table_schema = ? and table_name = ? and table_type = 'BASE TABLE'";
    }

    @Override
    public String typeBinary(ColumnDefinition c) {
        return "bytea";
    }

    @Override
    public String typeBool(ColumnDefinition c) {
        return "boolean";
    }

    @Override
    public String typeInteger(ColumnDefinition c) {
        return this.generatableColumn("integer", c);
    }

    @Override
    public String typeText(ColumnDefinition c) {
        return "text";
    }

    @Override
    public String typeDecimal(ColumnDefinition c) {
        int precision = c.get("precision", Integer.class);
        int scale = c.get("scale", Integer.class);
        return String.format("decimal(%d,%d)", precision, scale);
    }

    @Override
    public String typeDouble(ColumnDefinition c) {
        return "double precision";
    }

    @Override
    public String typeDate(ColumnDefinition c) {
        return "date";
    }

}
