package com.severell.core.server;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
import com.severell.core.container.Container;
import com.severell.core.exceptions.ViewException;
import com.severell.core.http.Responsable;
import com.severell.core.http.Response;
import com.severell.core.view.View;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;
import io.undertow.util.HttpString;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

public class UndertowResponse implements Response {

    private HttpServerExchange exchange;
    private PrintWriter writer;
    private ByteArrayOutputStream stream;

    /**
     * The {@link Container} to be used to resolve any dependencies.
     */
    private final Container c;

    /**
     * This holds any data that is to be passed to every mustache template.
     */
    private HashMap<String, Object> shared;
    private Responsable responsable;

    public UndertowResponse(HttpServerExchange exchange, Container c) {
        this.c = c;
        shared = new HashMap<>();
        this.exchange = exchange;
    }

    /**
     * Renders a mustache template string.
     *
     * @param templateString A string containing a mustache template
     * @param templateName A string containing a mustache template name
     * @param data The data to be used in the template string
     * @throws IOException
     * @return
     */
    public Responsable renderTemplateLiteral(String templateString, String templateName, HashMap<String, Object> data) throws IOException {
        MustacheFactory mf;
        mf = c.make(DefaultMustacheFactory.class);

        if(mf == null) {
            mf = new DefaultMustacheFactory();
        }

        Mustache m = mf.compile(new StringReader(templateString), templateName);
        Responsable responsable = setResponsable(200);
        responsable.setContentType("text/html");
        PrintWriter writer = responsable.getWriter();
        data.putAll(shared);
        m.execute(writer, data).flush();
        return responsable;
    }

    /**
     * Renders a mustache template.
     *
     * @param template The path to the template. This is relative to your templates folder. Eg. auth/login.mustache
     * @param data This contains the data to be used in the template.
     * @throws IOException
     * @return
     */
    public Responsable render(String template, HashMap<String, Object> data) throws IOException, ViewException {
        return this.render(template, data, "templates/");
    }

    /**
     * Renders a mustache template. Use this method if template is not located in default "template" directory
     * @param template The path to the template. Relative to the baseDir param.
     * @param data This contains the data to be used in the template.
     * @param baseDir Base directory of the template
     * @throws IOException
     * @return
     */
    public Responsable render(String template, HashMap<String, Object> data, String baseDir) throws ViewException, IOException {
        data.putAll(shared);
        View view = c.make(View.class);
        Responsable responsable = setResponsable(200);
        responsable.setContentType("text/html");
        view.render(template, data, baseDir, responsable.getWriter());
        return responsable;
    }

    @Override
    public void close() throws IOException {
        if(this.responsable != null) {
            this.responsable.getWriter().close();
            if(this.responsable.getStream() != null) {

                if(responsable.getContentType() != null && !responsable.getContentType().isEmpty()) {
                    exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, responsable.getContentType());
                }

                if(this.responsable.getStatusCode() > 0) {
                    exchange.setStatusCode(this.responsable.getStatusCode());
                }

                exchange.getResponseSender().send(ByteBuffer.wrap(this.responsable.getStream().toByteArray()));
            }
        }
    }

    @Override
    public void redirect(String s) {
        exchange.setStatusCode(303);
        exchange.getResponseHeaders().put(Headers.LOCATION, s);
    }

    @Override
    public void headers(HashMap<String, String> headers) {
        for(Map.Entry<String, String> entry : headers.entrySet()) {
            exchange.getResponseHeaders().add(HttpString.tryFromString(entry.getKey()), entry.getValue());
        }
    }

    @Override
    public void header(String s, String format) {
        exchange.getResponseHeaders().add(HttpString.tryFromString(s), format);
    }

    @Override
    public void share(String key, Object obj) {
        shared.put(key, obj);
    }

    private Responsable setResponsable(int statusCode) {
        Responsable resp = new Responsable(statusCode);
        this.responsable = resp;
        return resp;
    }

    /**
     * Writes the object to a JSON String
     * @param object Object to be converted to JSON
     * @throws IOException
     * @return
     */
    public Responsable json(Object object) throws IOException {
        return json(object, 200);
    }

    /**
     * Writes the object to a JSON String
     * @param object Object to be converted to JSON
     * @param statusCode HTTP Status Code
     * @throws IOException
     * @return
     */
    public Responsable json(Object object, int statusCode) throws IOException {
        ObjectMapper mapper = c.make(ObjectMapper.class);
        Responsable resp = setResponsable(statusCode);
        resp.setContentType("application/json");
        resp.getWriter().write(mapper.writeValueAsString(object));
        return resp;
    }

    /**
     * Writes a file to the client.
     * @param file
     * @param mimeType
     * @throws IOException
     */
    public Responsable download(File file, String mimeType) throws IOException {
        return download(file, mimeType, null);
    }

    /**
     * Writes a file to the client.
     * @param file
     * @param mimeType
     * @param name
     * @throws IOException
     */
    public Responsable download(File file, String mimeType, String name) throws IOException {
        byte[] bytes = Files.readAllBytes(file.toPath());
        header("Content-Type", mimeType);
        header("Content-Length", String.valueOf(bytes.length));
        header("Content-Disposition", "attachment; filename=\"" + name == null ? "generated" : name + "\"");
        Responsable responsable = setResponsable(200);
        responsable.getWriter().write(bytes.toString());
        return responsable;
    }
}
