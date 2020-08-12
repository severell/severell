package com.mitchdennett.framework.managers;

import com.mitchdennett.framework.container.Container;
import com.mitchdennett.framework.drivers.Session;

public class Manager {

    protected String config;
    protected String driver_prefix;
    private Container container;
    private Session manage_driver;

    public Manager(Container c) {
        this.container = c;
    }

    public <T> T create_driver(String driver, Class<T> c) {
        try {
            return (T) this.container.make(Class.forName("com.mitchdennett.framework.drivers." + driver_prefix + driver + "Driver"));
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

}
