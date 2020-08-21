package com.mitchdennett.framework.database.migrations;

import com.mitchdennett.framework.database.grammar.Grammar;

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
        };;

        public abstract String compile(Grammar grammar,  ColumnDefinition c);
    }

    private ColumnType type;
    private String name;
    private ArrayList<ColumnParams> params;

    public ColumnDefinition(ColumnType type, String name, ColumnParams[] params) {
        this.type = type;
        this.name = name;

        if(params != null) {
            this.params = new ArrayList<ColumnParams>(Arrays.asList(params));
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
        for(ColumnParams p : params) {
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

    public ArrayList<ColumnParams> getParams() {
        return params;
    }

    public void nullable() {
        params.add(new ColumnParams("nullable",true));
    }
}
