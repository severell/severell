package com.mitchdennett.framework.drivers;

public interface Session {

    public Object get(String key);
    public String getString(String key);
    public String getId();
    public void put(String key, Object value);

}
