package com.severell.core.database.migrations;

import com.severell.core.database.grammar.Grammar;

import java.util.ArrayList;
import java.util.Arrays;

public class ColumnDefinition {


    enum ColumnType{
        STRING {
            @Override
            public String compile(Grammar grammar, ColumnDefinition c) {
                return grammar.typeString(c);
            }
        },
        BIGINTEGER {
            @Override
            public String compile(Grammar grammar, ColumnDefinition c) {
                return grammar.typeBigInteger(c);
            }
        },
        TIMESTAMP {
            @Override
            public String compile(Grammar grammar, ColumnDefinition c) {
                return grammar.typeTimestamp(c);
            }
        },
        BINARY {
            @Override
            public String compile(Grammar grammar, ColumnDefinition c) {
                return grammar.typeBinary(c);
            }
        },
        BOOL {
            @Override
            public String compile(Grammar grammar, ColumnDefinition c) {
                return grammar.typeBool(c);
            }
        },
        INTEGER {
            @Override
            public String compile(Grammar grammar, ColumnDefinition c) {
                return grammar.typeInteger(c);
            }
        },
        TEXT {
            @Override
            public String compile(Grammar grammar, ColumnDefinition c) {
                return grammar.typeText(c);
            }
        },
        DECIMAL {
            @Override
            public String compile(Grammar grammar, ColumnDefinition c) {
                return grammar.typeDecimal(c);
            }
        }, DOUBLE{
            @Override
            public String compile(Grammar grammar, ColumnDefinition c) {
                return grammar.typeDouble(c);
            }
        }, DATE {
            @Override
            public String compile(Grammar grammar, ColumnDefinition c) {
                return grammar.typeDate(c);
            }
        };


        public abstract String compile(Grammar grammar, ColumnDefinition c);
    }

    private ColumnType type;
    private String name;
    private ArrayList<Params> params;

    public ColumnDefinition(ColumnType type, String name, Params[] params) {
        this.type = type;
        this.name = name;

        if(params != null) {
            this.params = new ArrayList<Params>(Arrays.asList(params));
        } else {
            this.params = new ArrayList<>();
        }
    }

    public ColumnType getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public Object get(String key) {
        for(Params p : params) {
            if(key.equals(p.getKey())) {
                return p.getValue();
            }
        }

        return null;
    }

    public <T> T get(String key, Class<T> clazz) {
        Object temp = get(key);
        if(temp != null) {
            return (T) get(key);
        }else{
            if (Boolean.class.equals(clazz)) {
                return (T) Boolean.valueOf(false);
            } else if (Integer.class.equals(clazz)) {
                return (T) Integer.valueOf(-1);
            }
        }
        return null;
    }

    public String compile(Grammar grammar) {
        return type.compile(grammar, this);
    }

    public ArrayList<Params> getParams() {
        return params;
    }

    public void nullable() {
        params.add(new Params("nullable",true));
    }
}
