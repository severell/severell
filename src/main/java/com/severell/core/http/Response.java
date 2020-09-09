package com.severell.core.http;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
import com.severell.core.config.Config;
import com.severell.core.container.Container;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

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

    public void render(String template, HashMap<String, Object> data) throws IOException {
        this.render(template, data, "templates/");
    }

    protected void render(String template, HashMap<String, Object> data, String baseDir) throws IOException {
        this.setContentType("text/html");
        MustacheFactory mf;
        mf = c.make(DefaultMustacheFactory.class);

        if(mf == null || Config.equals("ENV", "TEST")) {
            mf = new DefaultMustacheFactory();
        }

        Mustache m = mf.compile(baseDir + template);
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

    public void renderTemplateLiteral(String templateFile, HashMap<String, Object> data) throws IOException {
        this.setContentType("text/html");
        MustacheFactory mf;
        mf = c.make(DefaultMustacheFactory.class);

        if(mf == null || Config.equals("ENV", "TEST")) {
            mf = new DefaultMustacheFactory();
        }

        Mustache m = mf.compile(new StringReader(templateFile), "error.mustache");
        PrintWriter writer = this.getWriter();
        data.putAll(shared);
        m.execute(writer, data).flush();
    }
}
