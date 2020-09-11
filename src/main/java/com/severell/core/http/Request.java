package com.severell.core.http;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * A wrapper around {@link HttpServletRequest} with simpler syntax.
 */
public class Request extends HttpServletRequestWrapper {

    /**
     * Constructs a request object wrapping the given request.
     *
     * @param request the {@link HttpServletRequest} to be wrapped.
     * @throws IllegalArgumentException if the request is null
     */
    public Request(HttpServletRequest request) {
        super(request);
    }

    private HashMap<String, String> urlParams;
    private Map<String, String> queryData;

    /**
     * Used internally to add the named route parameters.
     *
     * @param key Name of the route parameter
     * @param value Value of the router parameter
     */
    protected void addParam(String key, String value) {
        if(urlParams == null) {
            urlParams = new HashMap<String, String>();
        }

        urlParams.put(key, value);
    }

    /**
     * Get a named route parameter
     * @param name The name of the route parameter
     * @return Returns a string containing the route parameter for the given name
     */
    public String param(String name) {
        if(urlParams == null) {
            return null;
        }

        return urlParams.get(name);
    }

    /**
     * Get a specific input value. This will return both query string parameters and body data.
     *
     * @param name Key of the input data
     * @return The requested input data.
     */
    public String input(String name) {
        return getParameter(name);
    }

    /**
     * Get a specific query string value.
     *
     * @param key Key for the query string data.
     * @return Returns a string of the query string data.
     */
    public String query(String key) {
        if(queryData == null) {
            parseQueryString();
        }

        return queryData.get(key);
    }

    private void parseQueryString() {
        String queryString = getQueryString();
        if(queryString != null) {
            this.queryData = Arrays.stream(queryString.split("&")).map(entry -> {
                try {
                    return URLDecoder.decode(entry, "UTF-8");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                return entry;
            }).collect(Collectors.toMap(
                    entry -> {
                        String[] parts = entry.split("=");
                        if(parts != null && parts.length > 0) {
                            return parts[0];
                        }

                        return "";
                    },
                    entry -> {
                        String[] parts = entry.split("=");
                        if(parts != null && parts.length > 1) {
                            return parts[1];
                        }

                        return "";
                    },
                    (key1, key2) -> key1
            ));
        }

        if(queryData == null) {
            queryData = new HashMap<>();
        }
    }
}
