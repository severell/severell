package com.severell.core.session;

import com.severell.core.http.Request;

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

    /**
     * Removes a value and returns it, for one-time values such as a token checked once. With the
     * Memory and Database drivers it's atomic: if two requests take the same key at once, only
     * one of them gets the value.
     * @param key Key of the object
     * @return the value, or null if there wasn't one (or another request took it)
     */
    default Object take(String key) {
        Object value = get(key);
        put(key, null);
        return value;
    }

    /**
     * Set the Request Object for the Session
     * @param r Request obj.
     */
    void setRequest(Request r);


    /**
     * @return whether nothing is stored in the session. A session that has never held anything
     * needn't be saved, so state for signed-out visitors (like the CSRF secret) is kept in a
     * cookie instead.
     */
    default boolean isEmpty() {
        return false;
    }

    /**
     * Gives the session a new id, keeping its data, and sends the new id to the browser. Call it
     * when a user signs in so a session id an attacker planted or saw earlier stops working.
     * The CSRF secret is replaced too, so redirect afterwards rather than rendering a page with
     * a form in the same request.
     */
    default void regenerate() {
        throw new UnsupportedOperationException("This session driver can't regenerate its id");
    }

    /**
     * Throws the session away and starts an empty one with a new id, e.g. when a user signs out.
     */
    default void reset() {
        throw new UnsupportedOperationException("This session driver can't reset");
    }
}
