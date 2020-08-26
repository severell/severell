package com.mitchdennett.framework.database.migrations;

import com.mitchdennett.framework.database.grammar.Grammar;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Blueprint {

    private String table;
    private ArrayList<ColumnDefinition> columns;
    private ArrayList<Command> commands;


    public Blueprint(String tableName) {
        this.table = tableName;
        columns = new ArrayList<ColumnDefinition>();
        this.commands = new ArrayList<Command>();
    }

    public ColumnDefinition id() {
        return this.bigInteger("id", true, true);
    }

    public ColumnDefinition bigInteger(String column) {
        return this.bigInteger(column, false, false);
    }

    private ColumnDefinition bigInteger(String column, boolean autoincrement, boolean unsigned) {
        return this.addColumn(ColumnDefinition.ColumnType.BIGINTEGER, column, new ColumnParams("autoIncrement", autoincrement), new ColumnParams("unsigned", unsigned));
    }

    public ColumnDefinition string(String column) {
        return this.addColumn(ColumnDefinition.ColumnType.STRING, column, new ColumnParams("length", 255));
    }

    public ColumnDefinition timestamp(String timestamp) {
        return this.addColumn(ColumnDefinition.ColumnType.TIMESTAMP, timestamp, new ColumnParams("precision", 0));
    }

    public void timestamps() {
        this.timestamp("created_at").nullable();
        this.timestamp("updated_at").nullable();
    }

    public ColumnDefinition binary(String column) {
        return this.addColumn(ColumnDefinition.ColumnType.BINARY, column);
    }

    public ColumnDefinition bool(String column) {
        return this.addColumn(ColumnDefinition.ColumnType.BOOL, column);
    }

    public ColumnDefinition integer(String column) {
        return this.addColumn(ColumnDefinition.ColumnType.INTEGER, column);
    }

    public ColumnDefinition text(String column) {
        return this.addColumn(ColumnDefinition.ColumnType.TEXT, column);
    }

    public ColumnDefinition decimal(String column, int precision, int scale) {
        return this.addColumn(ColumnDefinition.ColumnType.DECIMAL,column, new ColumnParams("precision", precision), new ColumnParams("scale", scale));
    }

    public ColumnDefinition dbl(String column) {
        return dbl(column, null,null);
    }

    public ColumnDefinition dbl(String column, Integer precision, Integer scale) {
        return this.addColumn(ColumnDefinition.ColumnType.DOUBLE,column, new ColumnParams("precision", precision), new ColumnParams("scale", scale));
    }

    public ColumnDefinition date(String column, Integer precision, Integer scale) {
        return this.addColumn(ColumnDefinition.ColumnType.DATE,column);
    }

    private ColumnDefinition addColumn(ColumnDefinition.ColumnType type, String name, ColumnParams... params) {
        ColumnDefinition column = new ColumnDefinition(type, name, params);
        columns.add(column);
        return column;
    }

    public void build(Connection connection, Grammar grammar) throws MigrationException {
        getImpliedCommands();
        ArrayList<String> statements = new ArrayList<String>();

        for(Command c : commands) {
            String sql = c.compile(this, grammar);
            if(sql != null) {
                statements.add(sql);
            }
        }

        for(String statement : statements) {
            connection.statement(statement);
        }
    }

    private boolean creating() {
        for(Command c : commands) {
            if(c.getType() == Command.CommandType.CREATE) {
                return true;
            }
        }

        return false;
    }

    private void getImpliedCommands() {
        if(!this.creating()) {
            if(getAddedColumns().size() > 0) {
                this.addCommand(Command.CommandType.ADD);
            }
        }
    }

    private List<ColumnDefinition> getAddedColumns() {
        return columns.stream().filter((col) -> !col.get("change", Boolean.class))
                .collect(Collectors.toList());
    }

    public void create() {
        this.addCommand(Command.CommandType.CREATE);
    }

    private Command addCommand(Command.CommandType type) {
        Command command = this.createCommand(type);
        this.commands.add(command);
        return command;
    }

    private Command createCommand(Command.CommandType type, Object... params) {
        return new Command(type, params);
    }

    public String getTableName() {
        return this.table;
    }

    public ArrayList<ColumnDefinition> getColumnDefinitions() {
        return this.columns;
    }

    public void drop() {
        this.addCommand(Command.CommandType.DROP);
    }
}
