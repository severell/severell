package com.mitchdennett.framework.database.migrations;


import com.mitchdennett.framework.database.grammar.Grammar;

public class Command {

    private Object[] params;
    private CommandType type;

    enum CommandType {
        CREATE{
            @Override
            public String compile(Blueprint table, Grammar grammar) {
                return grammar.create(table);
            }
        },
        DROP{
            @Override
            public String compile(Blueprint table,Grammar grammar) {
                return grammar.drop(table);
            }
        };


        public abstract String compile(Blueprint table, Grammar grammar);
    }


    public Command(CommandType type, Object[] params) {
        this.type = type;
        this.params = params;
    }

    public String compile(Blueprint table, Grammar grammar) {
        return this.type.compile(table, grammar);
    }
}
