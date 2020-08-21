package com.mitchdennett.framework.database.migrations;

import com.mitchdennett.framework.database.grammar.Grammar;
import com.mitchdennett.framework.database.grammar.PostgresGrammar;

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
