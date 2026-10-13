package com.severell.core.server;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.session.Session;
import io.undertow.server.session.SessionConfig;
import io.undertow.server.session.SessionManager;

import java.io.IOException;
import java.sql.SQLException;
import java.time.Instant;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * One request's view of a session stored by {@link DatabaseSessionManager}. Values are kept as
 * JSON: strings, numbers, booleans, lists, maps, and simple objects (stored as their fields,
 * read back with {@link #getAttribute(String, Class)}). Changes are saved by {@link #save()}.
 */
public class DatabaseSession implements Session {

    private final DatabaseSessionManager manager;
    private final String id;
    private final ObjectNode data;
    private final ObjectNode changes = JsonNodeFactory.instance.objectNode();
    private final Set<String> removed = new HashSet<>();
    private final HttpServerExchange exchange;
    private final SessionConfig config;
    private final Instant expiresAt;
    // Not in the database yet: created in this request.
    private boolean isNew;
    private boolean invalid;

    private DatabaseSession(DatabaseSessionManager manager, String id, ObjectNode data, Instant expiresAt,
                            boolean isNew, HttpServerExchange exchange, SessionConfig config) {
        this.manager = manager;
        this.id = id;
        this.data = data;
        this.expiresAt = expiresAt;
        this.isNew = isNew;
        this.exchange = exchange;
        this.config = config;
    }

    static DatabaseSession created(DatabaseSessionManager manager, String id, HttpServerExchange exchange, SessionConfig config) {
        return new DatabaseSession(manager, id, JsonNodeFactory.instance.objectNode(), null, true, exchange, config);
    }

    static DatabaseSession loaded(DatabaseSessionManager manager, String id, ObjectNode data, Instant expiresAt,
                                  HttpServerExchange exchange, SessionConfig config) {
        return new DatabaseSession(manager, id, data, expiresAt, false, exchange, config);
    }

    /**
     * Writes this request's changes, and sets the session cookie for a new session. Called at the
     * end of the request, before the response is sent.
     */
    public void save() {
        if (invalid) {
            return;
        }
        try {
            if (isNew) {
                // Lazy: a visitor whose session never held anything leaves no row and no cookie.
                if (data.size() == 0) {
                    return;
                }
                manager.insert(this, data);
                isNew = false;
                if (config != null && exchange != null) {
                    config.setSessionId(exchange, id);
                }
            } else if (changes.size() > 0 || !removed.isEmpty()) {
                manager.merge(this, changes, removed);
                changes.removeAll();
                removed.clear();
            } else if (expiresAt != null && manager.touchDue(expiresAt)) {
                manager.touch(this);
            }
        } catch (SQLException | IOException e) {
            throw new IllegalStateException("Couldn't save the session", e);
        }
    }

    boolean isInvalid() {
        return invalid;
    }

    /**
     * Reads a value as the given type, e.g. an object stored with {@link #setAttribute}.
     */
    public <T> T getAttribute(String name, Class<T> type) {
        checkValid();
        JsonNode node = data.get(name);
        if (node == null || node.isNull()) {
            return null;
        }
        try {
            return manager.mapper().treeToValue(node, type);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Session value " + name + " isn't a " + type.getSimpleName(), e);
        }
    }

    @Override
    public Object getAttribute(String name) {
        return getAttribute(name, Object.class);
    }

    @Override
    public Set<String> getAttributeNames() {
        checkValid();
        Set<String> names = new LinkedHashSet<>();
        data.fieldNames().forEachRemaining(names::add);
        return names;
    }

    @Override
    public Object setAttribute(String name, Object value) {
        if (value == null) {
            return removeAttribute(name);
        }
        checkValid();
        Object old = getAttribute(name);
        JsonNode node = manager.mapper().valueToTree(value);
        data.set(name, node);
        changes.set(name, node);
        removed.remove(name);
        return old;
    }

    @Override
    public Object removeAttribute(String name) {
        checkValid();
        if (!data.has(name)) {
            // Nothing to remove: don't write the session just for this.
            return null;
        }
        Object old = getAttribute(name);
        data.remove(name);
        changes.remove(name);
        removed.add(name);
        return old;
    }

    /**
     * Deletes the session now, so the id stops working everywhere straight away.
     */
    @Override
    public void invalidate(HttpServerExchange exchange) {
        checkValid();
        invalid = true;
        if (!isNew) {
            manager.delete(id);
        }
        if (config != null && exchange != null) {
            config.clearSession(exchange, id);
        }
    }

    private void checkValid() {
        if (invalid) {
            throw new IllegalStateException("Session is invalid");
        }
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void requestDone(HttpServerExchange exchange) {
    }

    @Override
    public long getCreationTime() {
        return 0;
    }

    @Override
    public long getLastAccessedTime() {
        return 0;
    }

    @Override
    public void setMaxInactiveInterval(int interval) {
    }

    @Override
    public int getMaxInactiveInterval() {
        return manager.getTimeoutSeconds();
    }

    @Override
    public SessionManager getSessionManager() {
        return manager;
    }

    @Override
    public String changeSessionId(HttpServerExchange exchange, SessionConfig config) {
        // Severell replaces sessions with Session.regenerate() instead.
        throw new UnsupportedOperationException("Use Session.regenerate()");
    }
}
