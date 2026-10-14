package com.severell.core.server;

import io.undertow.server.session.Session;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

public class InMemoryTakeTest {

    @Test
    public void takeRemovesInOneStep() {
        // Undertow's in-memory session removes from a ConcurrentHashMap, so only one caller
        // gets the value back; take() must use that single remove, not a get then a remove.
        Session undertowSession = mock(Session.class);
        given(undertowSession.removeAttribute("token")).willReturn("abc", (Object) null);
        UndertowInMemorySession first = new UndertowInMemorySession(undertowSession);
        UndertowInMemorySession second = new UndertowInMemorySession(undertowSession);

        assertEquals("abc", first.take("token"));
        assertNull(second.take("token"));
        verify(undertowSession, never()).getAttribute("token");
    }
}
