package com.severell.core.server;

import io.undertow.server.HttpServerExchange;
import io.undertow.server.session.SessionConfig;
import io.undertow.server.session.SessionManager;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

public class UndertowInMemorySessionTest {

    private final io.undertow.server.session.Session old = mock(io.undertow.server.session.Session.class);
    private final io.undertow.server.session.Session fresh = mock(io.undertow.server.session.Session.class);
    private final SessionManager manager = mock(SessionManager.class);
    private final SessionConfig config = mock(SessionConfig.class);
    private final HttpServerExchange exchange = new HttpServerExchange(null);

    private UndertowInMemorySession session() {
        exchange.putAttachment(SessionConfig.ATTACHMENT_KEY, config);
        given(old.getSessionManager()).willReturn(manager);
        given(old.getId()).willReturn("old-id");
        given(old.getAttributeNames()).willReturn(Set.of("userid", "after_login", "csrfToken"));
        given(old.getAttribute("userid")).willReturn(7);
        given(old.getAttribute("after_login")).willReturn("/oauth/authorize?x");
        given(old.getAttribute("csrfToken")).willReturn("secret");
        given(manager.createSession(org.mockito.ArgumentMatchers.eq(exchange), org.mockito.ArgumentMatchers.any(SessionConfig.class))).willReturn(fresh);
        UndertowInMemorySession session = new UndertowInMemorySession(old);
        session.setExchange(exchange);
        return session;
    }

    @Test
    public void regenerateMovesDataToANewSessionAndInvalidatesTheOld() {
        UndertowInMemorySession session = session();
        session.regenerate();

        verify(old).invalidate(exchange);
        verify(fresh).setAttribute("userid", 7);
        verify(fresh).setAttribute("after_login", "/oauth/authorize?x");
        // The CSRF secret isn't carried over; a fresh one is made on next use.
        verify(fresh, never()).setAttribute(eq("csrfToken"), any());

        given(fresh.getId()).willReturn("new-id");
        assertEquals("new-id", session.getId());
    }

    @Test
    public void resetStartsAnEmptySession() {
        UndertowInMemorySession session = session();
        session.reset();

        verify(old).invalidate(exchange);
        verify(fresh, never()).setAttribute(any(), any());
    }

    @Test
    public void invalidatesBeforeCreatingSoTheOldIdIsntReused() {
        UndertowInMemorySession session = session();
        session.regenerate();

        var order = inOrder(old, manager);
        order.verify(old).invalidate(exchange);
        order.verify(manager).createSession(org.mockito.ArgumentMatchers.eq(exchange), org.mockito.ArgumentMatchers.any(SessionConfig.class));
    }

    @Test
    public void theNewSessionNeverTakesTheIdTheBrowserSent() {
        UndertowInMemorySession session = session();
        given(config.findSessionId(exchange)).willReturn("planted-id");
        org.mockito.ArgumentCaptor<SessionConfig> used = org.mockito.ArgumentCaptor.forClass(SessionConfig.class);

        session.regenerate();

        verify(manager).createSession(org.mockito.ArgumentMatchers.eq(exchange), used.capture());
        assertNull(used.getValue().findSessionId(exchange));
        // Everything else still goes to the real config, e.g. setting the new cookie.
        used.getValue().setSessionId(exchange, "new-id");
        verify(config).setSessionId(exchange, "new-id");
    }

    @Test
    public void needsARequest() {
        UndertowInMemorySession session = new UndertowInMemorySession(old);
        assertThrows(IllegalStateException.class, session::regenerate);
        assertThrows(IllegalStateException.class, session::reset);
    }

    private static <T> T eq(T value) {
        return org.mockito.ArgumentMatchers.eq(value);
    }

    private static <T> T any() {
        return org.mockito.ArgumentMatchers.any();
    }
}
