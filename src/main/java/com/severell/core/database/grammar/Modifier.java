package com.severell.core.database.grammar;

import com.severell.core.database.migrations.ColumnDefinition;

public enum Modifier {
    NULLABLE{
        @Override
        public String compile(Grammar g, ColumnDefinition col) {
            return g.modifyNullable(col);
        }
    };

    public abstract String compile(Grammar g, ColumnDefinition col);
}
