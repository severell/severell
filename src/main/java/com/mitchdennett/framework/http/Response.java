package com.mitchdennett.framework.http;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
import com.mitchdennett.framework.container.Container;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class Response extends HttpServletResponseWrapper {

//    @FunctionalInterface
//    interface Function<One, Two, Three> {
//        public Three apply(One one, Two two);
//    }

    private final Container c;

    /**
     * Constructs a response adaptor wrapping the given response.
     *
     * @param response the {@link HttpServletResponse} to be wrapped.
     * @throws IllegalArgumentException if the response is null
     */

    private HashMap<String, Object> shared;

    public Response(HttpServletResponse response) {
        super(response);
        c = null;
        shared = new HashMap<>();
    }

    public Response(HttpServletResponse response, Container c) {
        super(response);
        this.c = c;
        shared = new HashMap<>();
    }

    public void view(String template, HashMap<String, Object> data) throws IOException {
        this.setContentType("text/html");
        MustacheFactory mf;
        if(c == null) {
            mf = new DefaultMustacheFactory();
        } else {
            mf = c.make(DefaultMustacheFactory.class);
        }
        Mustache m = mf.compile("src/main/resources/templates/" + template);
        PrintWriter writer = this.getWriter();
        data.putAll(shared);
        m.execute(writer, data).flush();
    }

    public void share(String key, Object obj) {
        shared.put(key, obj);
    }

    public void redirect(String url) throws IOException {
        this.sendRedirect(url);
    }

    public void headers(Map<String, String> headers) {
        if(headers != null) {
            for(String key : headers.keySet()) {
                String value = headers.get(key);
                addHeader(key, value);
            }
        }
    }
}
