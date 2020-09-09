package com.severell.core.database.migrations;

public class ColumnParams {
    private String name;
    private Object value;

    public ColumnParams(String name, Object value) {
        this.name = name;
        this.value = value;
    }

    public String getKey() {
        return name;
    }

    public Object getValue() {
        return value;
    }
}
