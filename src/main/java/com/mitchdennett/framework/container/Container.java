package com.mitchdennett.framework.container;

import java.util.HashMap;

public class Container {

    private HashMap<String, Object> ioc;

    public Container() {
        ioc = new HashMap<String, Object>();
    }

    public <T> T make(Class<T> c) {
        T obj =(T) ioc.get(c.getName());
        return obj;
    }

    public void bind(Object c) {
        ioc.put(c.getClass().getName(), c);
    }
}
