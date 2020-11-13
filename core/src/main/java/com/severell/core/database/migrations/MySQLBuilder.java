package com.severell.core.database.migrations;

import com.severell.core.database.Connection;

import java.sql.SQLException;

public class MySQLBuilder extends Builder {
    public MySQLBuilder(Connection connection) {
        super(connection);
    }

    @Override
    public boolean hasTable(String table) throws SQLException {
         return !this.connection.select(grammar.compileTableExists(), "public", table).isEmpty();
    }
}
