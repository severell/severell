package com.severell.core.session;

/**
 * This uses the default JSESSION sessions
 */
public class SessionMemoryDriver extends BaseSessionDriver implements Session {


    @Override
    public Object get(String key) {
        return this.underlyingSession.get(key);
    }

    @Override
    public String getString(String key) {
        Object val = this.underlyingSession.get(key);
        return val == null ? null : (String) val;
    }

    @Override
    public <T> T get(String key, Class<T> c) {
        Object val = this.underlyingSession.get(key);
        return val == null ? null : (T) val;
    }

    @Override
    public String getId() {
        return this.underlyingSession.getId();
    }

    @Override
    public void put(String key, Object value) {
        this.underlyingSession.put(key, value);
    }
}
