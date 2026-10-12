package com.severell.core.http;

import java.util.HashMap;
import java.util.Map;

/**
 * Per request storage for values that middleware wants to hand to controllers and services,
 * for example the id of the authenticated user.
 */
public class RequestContext {

    private final Map<String, Object> values = new HashMap<>();

    /**
     * Store a value for the rest of this request.
     *
     * @param key Key to store the value under
     * @param value Value to store
     */
    public void set(String key, Object value) {
        values.put(key, value);
    }

    /**
     * Get a value stored earlier in this request.
     *
     * @param key Key the value was stored under
     * @return The value, or null if nothing was stored
     */
    public Object get(String key) {
        return values.get(key);
    }

    /**
     * Get a value stored earlier in this request, cast to the given type.
     *
     * @param key Key the value was stored under
     * @param clazz Expected type of the value
     * @param <T> Expected type of the value
     * @return The value, or null if nothing was stored
     */
    public <T> T get(String key, Class<T> clazz) {
        return clazz.cast(values.get(key));
    }

    /**
     * @param key Key to check
     * @return True if a value was stored under the key
     */
    public boolean has(String key) {
        return values.containsKey(key);
    }

    /**
     * Remove a value from this request.
     *
     * @param key Key to remove
     */
    public void remove(String key) {
        values.remove(key);
    }
}
