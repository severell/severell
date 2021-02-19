package com.severell.core.session;

public interface Session {

    /**
     * Get the object from the session
     * @param key Key of the object.
     * @return
     */
    Object get(String key);

    /**
     * Get the object as a String from the session
     * @param key Key of the object.
     * @return
     */
    String getString(String key);

    /**
     * Get the object as the passed in type
     * @param key
     * @param c
     * @param <T>
     * @return
     */
    <T> T get(String key, Class<T> c);

    /**
     * Get the session id
     * @return
     */
    String getId();

    /**
     * Put an object into the session
     * @param key Key to store in the session
     * @param value Value to be stored in the session
     */
    void put(String key, Object value);

}
