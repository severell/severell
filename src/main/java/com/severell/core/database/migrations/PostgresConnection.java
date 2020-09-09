package com.severell.core.database.migrations;

import com.severell.core.database.grammar.Grammar;
import com.severell.core.database.grammar.PostgresGrammar;

public class PostgresConnection extends Connection{

    @Override
    public Grammar getDefaultGrammar() {
        return new PostgresGrammar();
    }

    @Override
    public QueryBuilder getDefaultQueryBuilder() {
        return new PostgresQueryBuilder();
    }

}
