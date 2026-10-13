package com.severell.core.server;

import io.undertow.server.HttpServerExchange;
import io.undertow.server.session.SessionConfig;
import io.undertow.server.session.SessionManager;
import com.severell.core.middleware.CsrfTokens;

import java.util.HashMap;
import java.util.Map;

import com.severell.core.container.Container;
import com.severell.core.http.Request;
import com.severell.core.session.BaseSessionDriver;
import com.severell.core.session.Session;

public class UndertowInMemorySession extends BaseSessionDriver implements Session {

    io.undertow.server.session.Session undertowSession;
    HttpServerExchange exchange;

    public UndertowInMemorySession(io.undertow.server.session.Session session) {
        this.undertowSession = session;
    }

    public UndertowInMemorySession(Container container) {
        this.container = container;
    }

    public void setExchange(HttpServerExchange exchange) {
        this.exchange = exchange;
    }

    public void setUndertowSession(io.undertow.server.session.Session undertowSession) {
        this.undertowSession = undertowSession;
    }

    @Override
    public Object get(String key) {
        return undertowSession.getAttribute(key);
    }

    @Override
    public String getString(String key) {
        Object val = this.undertowSession.getAttribute(key);
        return val == null ? null : (String) val;
    }

    @Override
    public <T> T get(String key, Class<T> c) {
        Object val = this.undertowSession.getAttribute(key);
        return val == null ? null : (T) val;
    }

    @Override
    public String getId() {
        return undertowSession.getId();
    }

    @Override
    public void put(String key, Object value) {
        undertowSession.setAttribute(key, value);
    }

    @Override
    public void setRequest(Request r) {
        //Don't need to implement this. Should I throw an error?
    }

    @Override
    public void regenerate() {
        replace(true);
    }

    @Override
    public void reset() {
        replace(false);
    }

    /**
     * Moves to a brand-new session with a new id and invalidates the old one, so a request still
     * holding the old id can't see or save what happens next. The CSRF secret is never carried
     * over: whoever knew the old session's token must not be able to use it in the new one.
     */
    private void replace(boolean keepData) {
        if (exchange == null) {
            throw new IllegalStateException("No request to replace the session for");
        }
        SessionConfig config = exchange.getAttachment(SessionConfig.ATTACHMENT_KEY);
        SessionManager manager = undertowSession.getSessionManager();

        Map<String, Object> data = new HashMap<>();
        if (keepData) {
            for (String name : undertowSession.getAttributeNames()) {
                data.put(name, undertowSession.getAttribute(name));
            }
        }
        data.remove(CsrfTokens.SESSION_KEY);

        String oldId = undertowSession.getId();
        undertowSession.invalidate(exchange);
        forgetSession(oldId);

        io.undertow.server.session.Session fresh = manager.createSession(exchange, config);
        data.forEach(fresh::setAttribute);
        undertowSession = fresh;
    }

    /**
     * Called after the old session is invalidated, before the new one is created.
     */
    protected void forgetSession(String oldId) {
    }
}
