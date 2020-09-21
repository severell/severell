package com.severell.core.drivers;

/**
 * This uses the default JSESSION sessions
 */
public class SessionMemoryDriver extends BaseSessionDriver implements Session {

    @Override
    public Object get(String key) {
        return this.request.getSession().getAttribute(key);
    }

    @Override
    public String getString(String key) {
        Object val = this.request.getSession().getAttribute(key);
        return val == null ? null : (String) val;
    }

    @Override
    public String getId() {
        return this.request.getSession().getId();
    }

    @Override
    public void put(String key, Object value) {
        this.request.getSession().setAttribute(key, value);
    }
}
