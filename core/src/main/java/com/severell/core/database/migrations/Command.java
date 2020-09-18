package com.severell.core.database.migrations;


import com.severell.core.database.grammar.Grammar;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Command {

    protected List<Params> params;
    private CommandType type;

    enum CommandType {
        CREATE{
            @Override
            public String compile(Blueprint table, Grammar grammar, Command command) {
                return grammar.create(table);
            }
        },
        DROP{
            @Override
            public String compile(Blueprint table,Grammar grammar, Command command) {
                return grammar.drop(table);
            }
        },
        ADD{
            @Override
            public String compile(Blueprint table,Grammar grammar, Command command) {
                return grammar.add(table);
            }
        },
        PRIMARY{
            @Override
            public String compile(Blueprint table,Grammar grammar, Command command) {
                return grammar.primary(table, command);
            }
        },
        FOREIGN{
            @Override
            public String compile(Blueprint table,Grammar grammar, Command command) {
                return grammar.foreign(table, command);
            }
        };


        public abstract String compile(Blueprint table, Grammar grammar, Command command);
    }


    public Command(CommandType type, Params[] params) {
        this.type = type;
        this.params = new ArrayList<>(Arrays.asList(params));
    }

    protected CommandType getType() {
        return this.type;
    }

    protected Params[] getParams() {
        return params.toArray(Params[]::new);
    }

    public Object param(String name) {
        for(Params p : params) {
            if(p.getKey().equals(name)) {
                return p.getValue();
            }
        }

        return null;
    }

    protected String compile(Blueprint table, Grammar grammar) {
        return this.type.compile(table, grammar, this);
    }
}
