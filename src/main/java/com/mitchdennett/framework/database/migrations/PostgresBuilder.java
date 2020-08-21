package com.mitchdennett.framework.database.migrations;

import java.sql.SQLException;
import java.util.List;

public class PostgresBuilder extends Builder{

    public PostgresBuilder(Connection connection) {
        super(connection);
    }

    @Override
    public boolean hasTable(String table) throws SQLException {
        List set = this.connection.select(grammar.compileTableExists(), "public", table);
        return set.size() > 0;
    }
}
