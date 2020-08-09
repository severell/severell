package com.mitchdennett.framework.http;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.util.HashMap;

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

    protected void addParam(String key, String value) {
        if(urlParams == null) {
            urlParams = new HashMap<String, String>();
        }

        urlParams.put(key, value);
    }

    public String getParam(String name) {
        if(urlParams == null) {
            return null;
        }

        return urlParams.get(name);
    }
}
