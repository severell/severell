package com.mitchdennett.framework.database.migrations;

import com.mitchdennett.framework.database.grammar.Grammar;

import java.sql.SQLException;

public class Builder {

    protected Connection connection;
    protected Grammar grammar;

    public Builder(Connection connection) {
        this.connection = connection;
        this.grammar = connection.getDefaultGrammar();
    }

    public void build(Blueprint table) {
        table.build(connection, grammar);
    }

    public boolean hasTable(String table) throws SQLException {
        return true;
    }

}
