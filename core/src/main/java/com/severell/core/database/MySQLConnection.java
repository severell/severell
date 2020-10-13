package com.severell.core.database;

import com.severell.core.database.grammar.Grammar;
import com.severell.core.database.grammar.MySQLGrammer;
import com.severell.core.database.migrations.MySQLQueryBuilder;
import com.severell.core.database.migrations.QueryBuilder;

public class MySQLConnection extends Connection {
    @Override
    public Grammar getDefaultGrammar() {
        return new MySQLGrammer();
    }

    @Override
    public QueryBuilder getDefaultQueryBuilder() {
        return new MySQLQueryBuilder();
    }
}
