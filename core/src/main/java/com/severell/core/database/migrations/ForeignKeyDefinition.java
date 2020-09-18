package com.severell.core.database.migrations;

public class ForeignKeyDefinition extends Command{

    public ForeignKeyDefinition(Params[] params) {
        super(CommandType.FOREIGN, params);
    }

    public ForeignKeyDefinition references(String... columns) {
        params.add(new Params("references", columns));
        return this;
    }

    public ForeignKeyDefinition on(String table) {
        params.add(new Params("on", table));
        return this;
    }
}
