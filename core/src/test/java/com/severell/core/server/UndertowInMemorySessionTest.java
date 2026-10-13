package com.severell.core.server;

import io.undertow.server.HttpServerExchange;
import io.undertow.server.session.SessionConfig;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

public class UndertowInMemorySessionTest {

    @Test
    public void regenerateChangesTheSessionIdForThisRequest() {
        io.undertow.server.session.Session undertowSession = mock(io.undertow.server.session.Session.class);
        HttpServerExchange exchange = new HttpServerExchange(null);
        SessionConfig config = mock(SessionConfig.class);
        exchange.putAttachment(SessionConfig.ATTACHMENT_KEY, config);

        UndertowInMemorySession session = new UndertowInMemorySession(undertowSession);
        session.setExchange(exchange);
        session.regenerate();

        verify(undertowSession).changeSessionId(exchange, config);
    }

    @Test
    public void regenerateNeedsARequest() {
        UndertowInMemorySession session = new UndertowInMemorySession(mock(io.undertow.server.session.Session.class));
        assertThrows(IllegalStateException.class, session::regenerate);
    }
}
