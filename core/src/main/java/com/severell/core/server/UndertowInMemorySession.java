package com.severell.core.server;

import com.severell.core.container.Container;
import com.severell.core.http.Request;
import com.severell.core.session.BaseSessionDriver;
import com.severell.core.session.Session;

public class UndertowInMemorySession extends BaseSessionDriver implements Session {

    io.undertow.server.session.Session undertowSession;

    public UndertowInMemorySession(io.undertow.server.session.Session session) {
        this.undertowSession = session;
    }

    public UndertowInMemorySession(Container container) {
        this.container = container;
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
}
