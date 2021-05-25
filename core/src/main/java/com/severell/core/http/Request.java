package com.severell.core.http;

import com.fasterxml.jackson.databind.JsonNode;
import com.severell.core.session.Session;

import java.io.IOException;
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
     * Get a specific input value. This will get only the first value.
     *
     * @param name Key of the input data
     * @return The requested input data.
     */
     String input(String name);

    /**
     * Get a specific input value. This will get all values for that key
     *
     * @param name Key of the input data
     * @return The requested input data.
     */
    String[] inputAsArray(String name);

    /**
     * This will deserialize json input into an object on the given clazz.
     *
     * @param clazz The class to deserialize the json body into
     * @return
     */
    <T> T json(Class<T> clazz) throws IOException;

    /**
     * This will deserialize json input into a JsonNode
     *
     * @return JsonNode
     */
    JsonNode json() throws IOException;

    /**
     * Returns the request body as raw byte array
     *
     * @return byte array of request body
     */
    byte[] raw() throws IOException;

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
