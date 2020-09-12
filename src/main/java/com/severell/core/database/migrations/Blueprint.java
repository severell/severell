package com.severell.core.database.migrations;

import com.severell.core.database.grammar.Grammar;
import com.severell.core.database.grammar.PostgresGrammar;

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

    /**
     *  Creates a new unsigned BigInteger column that auto increments
     *
     * @return
     */
    public ColumnDefinition id() {
        return this.bigInteger("id", true, true);
    }

    /**
     * Create a new BigInteger column
     *
     * @param columnName Name of the column
     * @return
     */
    public ColumnDefinition bigInteger(String columnName) {
        return this.bigInteger(columnName, false, false);
    }


    /**
     * Create a new BigInteger column
     *
     * @param columnName Name of the column
     * @param autoincrement Set column to auto increment
     * @param unsigned Make it an unsigned integer
     * @return
     */
    private ColumnDefinition bigInteger(String columnName, boolean autoincrement, boolean unsigned) {
        return this.addColumn(ColumnDefinition.ColumnType.BIGINTEGER, columnName, new ColumnParams("autoIncrement", autoincrement), new ColumnParams("unsigned", unsigned));
    }

    /**
     * Create a new VARCHAR(255) or equivalent column
     *
     * @param columnName Name of the column
     * @return
     */
    public ColumnDefinition string(String columnName) {
        return this.addColumn(ColumnDefinition.ColumnType.STRING, columnName, new ColumnParams("length", 255));
    }

    /**
     * Create a new Timestamp column
     *
     * @param columnName Name of the column
     * @return
     */
    public ColumnDefinition timestamp(String columnName) {
        return this.addColumn(ColumnDefinition.ColumnType.TIMESTAMP, columnName, new ColumnParams("precision", 0));
    }

    /**
     * Create a 'created_at' and 'updated_at' timestamp columns
     * If using the Severell-CLI make:model command these will
     * automatically set themselves on create and update of record
     */
    public void timestamps() {
        this.timestamp("created_at").nullable();
        this.timestamp("updated_at").nullable();
    }

    /**
     * Add a new Binary column. ('bytea' in Postgres)
     *
     * @param columnName Name of the column
     * @return
     */
    public ColumnDefinition binary(String columnName) {
        return this.addColumn(ColumnDefinition.ColumnType.BINARY, columnName);
    }

    /**
     * Create a new Boolean column
     *
     * @param columnName Name of the column
     * @return
     */
    public ColumnDefinition bool(String columnName) {
        return this.addColumn(ColumnDefinition.ColumnType.BOOL, columnName);
    }

    /**
     * Create a new Integer column
     * @param columnName
     * @return
     */
    public ColumnDefinition integer(String columnName) {
        return this.addColumn(ColumnDefinition.ColumnType.INTEGER, columnName);
    }

    /**
     * Create a new Text column
     *
     * @param columnName Name of the column
     * @return
     */
    public ColumnDefinition text(String columnName) {
        return this.addColumn(ColumnDefinition.ColumnType.TEXT, columnName);
    }

    /**
     * Create a new Decimal column
     * @param columnName Name of the column
     * @param precision Number of total digits
     * @param scale Number of digits to the right of the decimal point
     * @return
     */
    public ColumnDefinition decimal(String columnName, int precision, int scale) {
        return this.addColumn(ColumnDefinition.ColumnType.DECIMAL,columnName, new ColumnParams("precision", precision), new ColumnParams("scale", scale));
    }

    /**
     * Create a new Double column
     *
     * @param columnName Name of the column
     * @return
     */
    public ColumnDefinition dbl(String columnName) {
        return dbl(columnName, null,null);
    }

    /**
     * Create a new Double column
     *
     * @param columnName Name of the column
     * @param precision
     * @param scale
     * @return
     */
    public ColumnDefinition dbl(String columnName, Integer precision, Integer scale) {
        return this.addColumn(ColumnDefinition.ColumnType.DOUBLE,columnName, new ColumnParams("precision", precision), new ColumnParams("scale", scale));
    }

    /**
     * Create a new Date Column
     *
     * @param columnName Name of the column
     * @return
     */
    public ColumnDefinition date(String columnName) {
        return this.addColumn(ColumnDefinition.ColumnType.DATE,columnName);
    }

    /**
     * Adds a new column definition to this blueprint.
     *
     * @param type Column data type (i.e VARCHAR, DECIMAL)
     * @param name Name of the column
     * @param params Additional column parameters (i.e unique, nullable)
     * @return
     */
    private ColumnDefinition addColumn(ColumnDefinition.ColumnType type, String name, ColumnParams... params) {
        ColumnDefinition column = new ColumnDefinition(type, name, params);
        columns.add(column);
        return column;
    }

    /**
     * This method builds the schema and runs the resulting SQL statements
     *
     * @param connection Database connection
     * @param grammar Grammar for chosen Database (i.e {@link PostgresGrammar}
     * @throws MigrationException
     */
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

    /**
     * Returns true if this blueprint is creating a new table.
     * @return
     */
    private boolean creating() {
        for(Command c : commands) {
            if(c.getType() == Command.CommandType.CREATE) {
                return true;
            }
        }

        return false;
    }

    /**
     * If this blueprint is not creating a new table and there are columns to add
     * this adds a new {@link Command.CommandType} ADD to indicate we are adding columns.
     */
    private void getImpliedCommands() {
        if(!this.creating()) {
            if(getAddedColumns().size() > 0) {
                this.addCommand(Command.CommandType.ADD);
            }
        }
    }

    /**
     * Returns a list of columns to be added to an existing table.
     * @return
     */
    private List<ColumnDefinition> getAddedColumns() {
        return columns.stream().filter((col) -> !col.get("change", Boolean.class))
                .collect(Collectors.toList());
    }

    /**
     * Indicates that this blueprint is to create a new table
     */
    public void create() {
        this.addCommand(Command.CommandType.CREATE);
    }

    /**
     * Add a new command to this blueprint
     * @param type Command Type (i.e Create or Update)
     * @return
     */
    private Command addCommand(Command.CommandType type) {
        Command command = this.createCommand(type);
        this.commands.add(command);
        return command;
    }

    /**
     * Create a new command
     * @param type Command Type (i.e Create)
     * @param params Additional Params
     * @return
     */
    private Command createCommand(Command.CommandType type, Object... params) {
        return new Command(type, params);
    }

    /**
     * Get the table name for this blueprint.
     * @return
     */
    public String getTableName() {
        return this.table;
    }

    /**
     * Returns all the column definitions on this blueprint. Each
     * column definition represents a database column.
     * @return
     */
    public ArrayList<ColumnDefinition> getColumnDefinitions() {
        return this.columns;
    }

    /**
     * Indicates that this blueprint is to drop a table.
     */
    public void drop() {
        this.addCommand(Command.CommandType.DROP);
    }
}
