package com.severell.core.drivers;

public interface Session {

    /**
     * Get the object from the session
     * @param key Key of the object.
     * @return
     */
    public Object get(String key);

    /**
     * Get the object as a String from the session
     * @param key Key of the object.
     * @return
     */
    public String getString(String key);

    /**
     * Get the session id
     * @return
     */
    public String getId();

    /**
     * Put an object into the session
     * @param key Key to store in the session
     * @param value Value to be stored in the session
     */
    public void put(String key, Object value);

}
