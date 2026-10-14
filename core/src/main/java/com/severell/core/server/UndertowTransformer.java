package com.severell.core.server;

import com.severell.core.container.Container;
import com.severell.core.http.Dispatcher;
import com.severell.core.session.SessionSettings;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.session.Session;
import io.undertow.util.Sessions;

public class UndertowTransformer {

    private Container container;
    private Dispatcher dispatcher;

    public UndertowTransformer(Container c) {
        this.container = c;
        dispatcher = c.make(Dispatcher.class);
    }

    public void dispatch(HttpServerExchange exchange) {
        Session session = Sessions.getOrCreateSession(exchange);
        if (pastMaxLifetime(session)) {
            // However recently it was used: start over with an empty session (signed out).
            session.invalidate(exchange);
            session = Sessions.getOrCreateSession(exchange);
        }
        UndertowInMemorySession sessionImpl = SessionSettings.DATABASE.equals(SessionSettings.driver())
                ? container.make("ServerDatabaseSession", UndertowDatabaseSession.class)
                : container.make("ServerMemorySession", UndertowInMemorySession.class);
        sessionImpl.setUndertowSession(session);
        sessionImpl.setExchange(exchange);
        UndertowRequest request = new UndertowRequest(exchange, sessionImpl, container);
        UndertowResponse response = new UndertowResponse(exchange, container);

        dispatcher.dispatch(request, response);

        //Write Response to Client
    }

    static boolean pastMaxLifetime(Session session) {
        long ageMillis = System.currentTimeMillis() - session.getCreationTime();
        return ageMillis >= SessionSettings.maxLifetimeSeconds() * 1000L;
    }
}
