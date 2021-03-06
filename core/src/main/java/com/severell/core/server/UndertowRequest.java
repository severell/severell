package com.severell.core.server;

import com.severell.core.http.Cookie;
import com.severell.core.http.Request;
import com.severell.core.session.Session;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.handlers.CookieImpl;
import io.undertow.server.handlers.form.FormData;
import io.undertow.server.handlers.form.FormDataParser;
import io.undertow.util.Sessions;

import java.util.Deque;
import java.util.HashMap;
import java.util.Map;

public class UndertowRequest implements Request {

    private Map<String, String> queryData;
    private Map<String, String> inputData;
    private HashMap<String, String> params;
    private String path;
    private String method;
    private HttpServerExchange exchange;
    private FormData formData;
    private Session session;

    public UndertowRequest(HttpServerExchange exchange, Session session) {
        queryData = new HashMap<>();
        params = new HashMap<>();
        for(Map.Entry<String, Deque<String>> entry : exchange.getQueryParameters().entrySet()) {
            queryData.put(entry.getKey(), entry.getValue().element());
        }

        path = exchange.getRequestURI();
        method = exchange.getRequestMethod().toString();
        this.exchange = exchange;

        formData = exchange.getAttachment(FormDataParser.FORM_DATA);
        this.session = session;
    }

    @Override
    public Session session() {
        session.setRequest(this);
        return session;
    }

    @Override
    public void addParam(String key, String value) {
        params.put(key,value);
    }

    @Override
    public HashMap<String, String> params() {
        return params;
    }

    @Override
    public String param(String name) {
        return params.get(name);
    }

    @Override
    public String input(String name) {
        return formData == null || formData.getFirst(name) == null ? query(name) : formData.getFirst(name).getValue();
    }

    @Override
    public String query(String key) {
        return queryData.get(key);
    }

    @Override
    public Cookie cookie(String s) {
        CookieImpl cookieImpl = (CookieImpl) exchange.getRequestCookies().get(s);
        return cookieImpl != null ? new UndertowCookie(cookieImpl) : null;
    }

    @Override
    public String method() {
        return method;
    }

    @Override
    public String path() {
        return path;
    }

    @Override
    public String header(String s) {
        return exchange.getRequestHeaders().get(s).getFirst();
    }
}
