package com.severell.core.database.grammar;

import com.severell.core.database.migrations.Blueprint;
import com.severell.core.database.migrations.ColumnDefinition;
import com.severell.core.database.migrations.Command;
import org.codehaus.plexus.util.StringUtils;

public class MySQLGrammer extends Grammar {


    @Override
    public String create(Blueprint table) {
        return String.format("%s table %s (%s)",
                "create",
                table.getTableName(),
                StringUtils.join(getColumns(table), ", ")
        );
    }

    @Override
    public String typeString(ColumnDefinition col) {
        return String.format("varchar(%s) character set utf8mb4", col.get("length"));
    }

    @Override
    public String typeBigInteger(ColumnDefinition col) {
        return generatableColumn("bigint", col);
    }

    @Override
    public String typeTimestamp(ColumnDefinition col) {
        String sql = "timestamp";
        if(col.get("precision", Integer.class) > -1) {
            sql =  String.format( sql + "(%d)", col.get("precision", Integer.class));
        }
        if(col.get("useCurrent", Boolean.class)) {
            sql += " default CURRENT_TIMESTAMP";
        }
        return sql;
    }

    @Override
    public String generatableColumn(String type, ColumnDefinition col) {
        if(!col.get("autoIncrement", Boolean.class)) {
            return type;
        } else {
            switch (type) {
                case "integer":
                    return "int";
                case "bigint":
                    return "bigint";
                case "smallint":
                    return "smallint";
                case "tinyint":
                    return "tinyint";
                case "mediumint":
                    return "mediumint";
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
    public String drop(Blueprint table) {
        return String.format("drop table if exists %s", table.getTableName());
    }

    @Override
    public String compileTableExists() {
        return "select * from information_schema.tables where table_schema = ? and table_name = ? and table_type = 'BASE TABLE'";
    }

    @Override
    public String typeBinary(ColumnDefinition c) {
        return "blob";
    }

    @Override
    public String typeBool(ColumnDefinition c) {
        return "boolean";
    }

    @Override
    public String typeInteger(ColumnDefinition c) {
        return generatableColumn("integer", c);
    }

    @Override
    public String typeText(ColumnDefinition c) {
        return "text";
    }

    @Override
    public String typeDecimal(ColumnDefinition col) {
        String sql = "decimal";
        if(col.get("precision", Integer.class) > -1) {
            sql += "(";
            sql =  String.format( sql + "%d", col.get("precision", Integer.class));
            if(col.get("scale", Integer.class) > -1) {
                sql =  String.format( sql + ",%d", col.get("scale", Integer.class));
            }
            sql += ")";
        }
        return sql;
    }

    @Override
    public String typeDouble(ColumnDefinition c) {
        return "double";
    }

    @Override
    public String typeDate(ColumnDefinition c) {
        return "date";
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
    public String primary(Blueprint table, Command command) {
        return String.format("%s table %s add primary key %s (%s)",
                "alter",
                table.getTableName(),
                command.param("auto_increment"),
                String.join(", ", (String[])command.param("columns")));
    }

    @Override
    public String typeSmallInteger(ColumnDefinition c) {
        return generatableColumn("smallint", c);
    }

    @Override
    public Modifier[] getModifiers() {
        return new Modifier[]{
                Modifier.NULLABLE,
        };
    }
}
