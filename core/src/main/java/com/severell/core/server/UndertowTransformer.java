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
        com.severell.core.session.Session sessionImpl = null;


        //TODO Refactor This into a manager.
        String driver = Config.get("SESSION_DRIVER", "Memory");
        switch (driver.toLowerCase()){
            case "memory":
                UndertowInMemorySession inMemorySession = container.make("ServerMemorySession", UndertowInMemorySession.class);
                inMemorySession.setUndertowSession(session);
                sessionImpl = inMemorySession;
                break;
            case "redis":
                UndertowRedisSession redisSession = container.make("ServerRedisSession", UndertowRedisSession.class);
                redisSession.setUndertowSession(session);
                sessionImpl = redisSession;
                break;
        }

        UndertowRequest request = new UndertowRequest(exchange, sessionImpl, container);
        UndertowResponse response = new UndertowResponse(exchange, container);

        dispatcher.dispatch(request, response);

        //Write Response to Client
    }
}
