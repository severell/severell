package com.severell.core.server;

import com.severell.core.container.Container;
import com.severell.core.http.Dispatcher;
import com.severell.core.http.Request;
import com.severell.core.http.Response;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.session.Session;
import io.undertow.server.session.SessionConfig;
import io.undertow.server.session.SessionManager;
import io.undertow.util.Methods;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

public class UndertowTransformerTest {

    @Test
    public void dispatchesWithTheRequestsInMemorySession() {
        Container container = mock(Container.class);
        Dispatcher dispatcher = mock(Dispatcher.class);
        given(container.make(Dispatcher.class)).willReturn(dispatcher);
        given(container.make("ServerMemorySession", UndertowInMemorySession.class)).willReturn(new UndertowInMemorySession(container));

        Session undertowSession = mock(Session.class);
        given(undertowSession.getId()).willReturn("session-id");
        given(undertowSession.getCreationTime()).willReturn(System.currentTimeMillis());
        SessionManager manager = mock(SessionManager.class);
        SessionConfig config = mock(SessionConfig.class);
        HttpServerExchange exchange = new HttpServerExchange(null);
        exchange.setRequestMethod(Methods.GET);
        exchange.setRequestURI("/whoami");
        exchange.putAttachment(SessionManager.ATTACHMENT_KEY, manager);
        exchange.putAttachment(SessionConfig.ATTACHMENT_KEY, config);
        given(manager.getSession(exchange, config)).willReturn(undertowSession);

        new UndertowTransformer(container).dispatch(exchange);

        ArgumentCaptor<Request> request = ArgumentCaptor.forClass(Request.class);
        verify(dispatcher).dispatch(request.capture(), any(Response.class));
        UndertowInMemorySession session = (UndertowInMemorySession) request.getValue().session();
        assertEquals("session-id", session.getId());
        // The session knows its request, so regenerate()/reset() can replace it.
        assertSame(exchange, session.exchange);
    }

    @Test
    public void aSessionPastItsMaximumLifetimeIsReplacedWithAnEmptyOne() {
        Container container = mock(Container.class);
        Dispatcher dispatcher = mock(Dispatcher.class);
        given(container.make(Dispatcher.class)).willReturn(dispatcher);
        given(container.make("ServerMemorySession", UndertowInMemorySession.class)).willReturn(new UndertowInMemorySession(container));

        Session old = mock(Session.class);
        given(old.getCreationTime()).willReturn(System.currentTimeMillis() - 15L * 24 * 3600 * 1000);
        Session fresh = mock(Session.class);
        given(fresh.getId()).willReturn("fresh");
        given(fresh.getCreationTime()).willReturn(System.currentTimeMillis());
        SessionManager manager = mock(SessionManager.class);
        SessionConfig config = mock(SessionConfig.class);
        HttpServerExchange exchange = new HttpServerExchange(null);
        exchange.setRequestMethod(Methods.GET);
        exchange.setRequestURI("/whoami");
        exchange.putAttachment(SessionManager.ATTACHMENT_KEY, manager);
        exchange.putAttachment(SessionConfig.ATTACHMENT_KEY, config);
        given(manager.getSession(exchange, config)).willReturn(old, (Session) null);
        given(manager.createSession(exchange, config)).willReturn(fresh);

        new UndertowTransformer(container).dispatch(exchange);

        verify(old).invalidate(exchange);
        ArgumentCaptor<Request> request = ArgumentCaptor.forClass(Request.class);
        verify(dispatcher).dispatch(request.capture(), any(Response.class));
        assertEquals("fresh", request.getValue().session().getId());
    }
}
