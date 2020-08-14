package com.mitchdennett.framework.http;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.IOException;
import java.net.URLDecoder;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
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
    private Map<String, String> bodyData;

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

    public String input(String name) {
        if(bodyData == null) {
            return null;
        }

        return bodyData.get(name);
    }

    protected void parseBody() {
        if ("POST".equalsIgnoreCase(this.getMethod()))
        {
            String body = null;
            try {
                body = this.getReader().lines().collect(Collectors.joining(System.lineSeparator()));

                body = URLDecoder.decode(body, "UTF-8");
                this.bodyData = Arrays.stream(body.split("&")).collect(Collectors.toMap(
                        entry -> {
                            return entry.split("=")[0];
                        },
                        entry -> {
                            String[] spl = entry.split("=");
                            if(spl != null && spl.length > 1) {
                                return spl[1];
                            } else {
                                return "";
                            }
                        }
                ));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    };
}
