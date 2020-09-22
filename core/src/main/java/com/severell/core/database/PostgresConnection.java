package com.severell.core.database;

import com.severell.core.database.grammar.Grammar;
import com.severell.core.database.grammar.PostgresGrammar;
import com.severell.core.database.migrations.PostgresQueryBuilder;
import com.severell.core.database.migrations.QueryBuilder;

public class PostgresConnection extends Connection {

    @Override
    public Grammar getDefaultGrammar() {
        return new PostgresGrammar();
    }

    @Override
    public QueryBuilder getDefaultQueryBuilder() {
        return new PostgresQueryBuilder();
    }

}
