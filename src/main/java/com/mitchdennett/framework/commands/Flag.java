package com.mitchdennett.framework.commands;

public class Flag {
    public Flag(String flag, String description) {
        this.flag = flag;
        this.description = description;
    }
    protected String flag;
    protected String description;
    protected String value;

    public String getFlag() {
        return flag;
    }

    public void setFlag(String flag) {
        this.flag = flag;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}