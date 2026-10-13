package com.severell.core.server;

import com.severell.core.config.Config;
import com.severell.core.container.Container;
import com.severell.core.http.Dispatcher;
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
        // SessionProvider only allows the memory driver.
        UndertowInMemorySession sessionImpl = container.make("ServerMemorySession", UndertowInMemorySession.class);
        sessionImpl.setUndertowSession(session);
        sessionImpl.setExchange(exchange);
        UndertowRequest request = new UndertowRequest(exchange, sessionImpl, container);
        UndertowResponse response = new UndertowResponse(exchange, container);

        dispatcher.dispatch(request, response);

        //Write Response to Client
    }
}
