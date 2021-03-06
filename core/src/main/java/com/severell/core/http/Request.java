package com.severell.core.http;

import com.severell.core.session.Session;
import java.util.HashMap;

public interface Request {


    Session session();


    /**
     * Used internally to add the named route parameters.
     *
     * @param key Name of the route parameter
     * @param value Value of the router parameter
     */
    default void addParam(String key, String value) {
        HashMap<String, String> urlParams = params();
        if(urlParams == null) {
            urlParams = new HashMap<String, String>();
        }

        urlParams.put(key, value);
    }

    /**
     * Get all route parameters
     * @return Returns a HashMap containing all the route parameters
     */
    HashMap<String, String> params();

    /**
     * Get a named route parameter
     * @param name The name of the route parameter
     * @return Returns a string containing the route parameter for the given name
     */
    String param(String name);

    /**
     * Get a specific input value. This will return both query string parameters and body data.
     *
     * @param name Key of the input data
     * @return The requested input data.
     */
     String input(String name);

    /**
     * Get a specific query string value.
     *
     * @param key Key for the query string data.
     * @return Returns a string of the query string data.
     */
    String query(String key);

    Cookie cookie(String s);

    String method();

    String path();

    String header(String s);
}
