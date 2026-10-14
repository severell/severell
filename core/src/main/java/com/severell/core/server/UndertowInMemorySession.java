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

    /**
     * When a regenerated in-memory session really started (epoch millis): regenerate() keeps the
     * data, so it mustn't restart the maximum-lifetime clock. Database sessions keep this in
     * their created_at column instead.
     */
    static final String STARTED_KEY = "severell.sessionStarted";

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
        if (this.undertowSession instanceof DatabaseSession) {
            // Stored as JSON: read it back as the type asked for.
            return ((DatabaseSession) this.undertowSession).getAttribute(key, c);
        }
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
    public Object take(String key) {
        if (this.undertowSession instanceof DatabaseSession) {
            return ((DatabaseSession) this.undertowSession).take(key);
        }
        // Undertow's in-memory sessions remove from a concurrent map: only one caller gets it.
        return undertowSession.removeAttribute(key);
    }

    @Override
    public boolean isEmpty() {
        return undertowSession.getAttributeNames().isEmpty();
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

        long started = startedAt(undertowSession);
        Map<String, Object> data = new HashMap<>();
        if (keepData) {
            for (String name : undertowSession.getAttributeNames()) {
                data.put(name, undertowSession.getAttribute(name));
            }
        }
        data.remove(CsrfTokens.SESSION_KEY);

        undertowSession.invalidate(exchange);

        // Undertow reuses the id from the request's cookie when creating a session if that id is
        // still accepted. Hide the cookie so the new session always gets a new id.
        io.undertow.server.session.Session fresh = manager.createSession(exchange, new NewIdSessionConfig(config));
        data.forEach(fresh::setAttribute);
        if (keepData) {
            // Same data, same clock: regenerating can't extend the maximum lifetime.
            if (fresh instanceof DatabaseSession) {
                ((DatabaseSession) fresh).startedAt(java.time.Instant.ofEpochMilli(started));
            } else {
                fresh.setAttribute(STARTED_KEY, started);
            }
        }
        undertowSession = fresh;
    }

    /**
     * @return when the session started, carried across regenerate()
     */
    static long startedAt(io.undertow.server.session.Session session) {
        long created = session.getCreationTime();
        if (session instanceof DatabaseSession) {
            // Kept in created_at.
            return created;
        }
        Object started = session.getAttribute(STARTED_KEY);
        // Only ever earlier than the session itself, so a bad value can't extend its life.
        return started instanceof Number ? Math.min(((Number) started).longValue(), created) : created;
    }

    /**
     * Passes everything through to the real config, except that it never reports the session
     * id the request came with.
     */
    static class NewIdSessionConfig implements SessionConfig {
        private final SessionConfig delegate;

        NewIdSessionConfig(SessionConfig delegate) {
            this.delegate = delegate;
        }

        @Override
        public void setSessionId(HttpServerExchange exchange, String sessionId) {
            delegate.setSessionId(exchange, sessionId);
        }

        @Override
        public void clearSession(HttpServerExchange exchange, String sessionId) {
            delegate.clearSession(exchange, sessionId);
        }

        @Override
        public String findSessionId(HttpServerExchange exchange) {
            return null;
        }

        @Override
        public SessionCookieSource sessionCookieSource(HttpServerExchange exchange) {
            return delegate.sessionCookieSource(exchange);
        }

        @Override
        public String rewriteUrl(String originalUrl, String sessionId) {
            return delegate.rewriteUrl(originalUrl, sessionId);
        }
    }
}
