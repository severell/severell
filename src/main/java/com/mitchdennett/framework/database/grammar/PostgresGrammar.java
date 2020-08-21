package com.mitchdennett.framework.database.grammar;

import com.mitchdennett.framework.database.migrations.Blueprint;
import com.mitchdennett.framework.database.migrations.ColumnDefinition;
import org.codehaus.plexus.util.StringUtils;

import static com.mitchdennett.framework.database.grammar.Modifier.NULLABLE;


public class PostgresGrammar extends Grammar {

    protected Modifier[] modifiers = new Modifier[]{
            NULLABLE,
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
}
