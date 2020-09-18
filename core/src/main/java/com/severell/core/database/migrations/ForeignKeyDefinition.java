package com.severell.core.database.migrations;

public class ForeignKeyDefinition extends Command{

    /**
     * Create a new ForeignKeyDefinition. Used to create Foreign key constraints
     * @param params
     */
    public ForeignKeyDefinition(Params[] params) {
        super(CommandType.FOREIGN, params);
    }

    /**
     * Set the columns on the reference table to be used for the constraint
     * @param columns Column Names
     * @return
     */
    public ForeignKeyDefinition references(String... columns) {
        params.add(new Params("references", columns));
        return this;
    }

    /**
     * Set the referenced table for the foreign key
     * @param table Name of foreign table
     * @return
     */
    public ForeignKeyDefinition on(String table) {
        params.add(new Params("on", table));
        return this;
    }
}
