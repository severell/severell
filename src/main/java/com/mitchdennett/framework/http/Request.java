package com.mitchdennett.framework.http;

import jdk.internal.joptsimple.internal.Strings;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.*;
import java.util.stream.Collectors;

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

    protected void addParam(String key, String value) {
        if(urlParams == null) {
            urlParams = new HashMap<String, String>();
        }

        urlParams.put(key, value);
    }

    public String param(String name) {
        if(urlParams == null) {
            return null;
        }

        return urlParams.get(name);
    }

    public String input(String name) {
        return getParameter(name);
    }

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
