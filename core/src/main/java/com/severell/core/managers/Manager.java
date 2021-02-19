package com.severell.core.managers;

import com.severell.core.container.Container;

public class Manager {

    protected String config;
    protected String driver_prefix;
    protected Container container;

    public Manager(Container c) {
        this.container = c;
    }

    /**
     * Create a new concrete instance of a driver.
     * @param driver
     * @param c
     * @param <T>
     * @return
     */
    public <T> T create_driver(String driver, Class<T> c) {
        return (T) this.container.make(driver_prefix + driver + "Driver", c);
    }

}
