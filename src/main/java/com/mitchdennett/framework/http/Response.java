package com.mitchdennett.framework.http;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public class Response extends HttpServletResponseWrapper {

    /**
     * Constructs a response adaptor wrapping the given response.
     *
     * @param response the {@link HttpServletResponse} to be wrapped.
     * @throws IllegalArgumentException if the response is null
     */
    public Response(HttpServletResponse response) {
        super(response);
    }

    public void view(String template, List<Object> data) throws IOException {
        this.setContentType("text/html");
        MustacheFactory mf = new DefaultMustacheFactory();
        Mustache m = mf.compile("src/main/resources/templates/" + template);
        PrintWriter writer = this.getWriter();
        m.execute(writer, data).flush();
    }
}
