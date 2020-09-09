package com.mitchdennett.framework.managers;

import com.mitchdennett.framework.container.Container;

public class Manager {

    protected String config;
    protected String driver_prefix;
    private Container container;

    public Manager(Container c) {
        this.container = c;
    }

    public <T> T create_driver(String driver, Class<T> c) {
        return (T) this.container.make(driver_prefix + driver + "Driver", c);
    }

}
