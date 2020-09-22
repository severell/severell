package com.severell.core.commands;

public class Flag {
    public Flag(String flag, String description) {
        this.flag = flag;
        this.description = description;
    }

    protected String flag;
    protected String description;
    protected String value;

    public String getValue() {
        return value ;
    }

    public String getFlag() {
        return flag;
    }

    public String getDescription() {
        return description;
    }

}