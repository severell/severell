package com.severell.core.server;

import io.undertow.server.handlers.Cookie;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class SeverellSessionCookieConfigTest {

    @Test
    public void sessionCookieCarriesTheConfiguredFlags() {
        Cookie cookie = new SeverellSessionCookieConfig(true, true, "Lax", id -> true).cookie("abc");
        // __Host- so no other subdomain can plant a session.
        assertEquals("__Host-session", cookie.getName());
        assertEquals("abc", cookie.getValue());
        assertEquals("/", cookie.getPath());
        assertNull(cookie.getDomain());
        assertTrue(cookie.isSecure());
        assertTrue(cookie.isHttpOnly());
        assertTrue(cookie.isSameSite());
        assertEquals("Lax", cookie.getSameSiteMode());
    }

    @Test
    public void localCookieCanSkipSecure() {
        Cookie cookie = new SeverellSessionCookieConfig(false, true, "Strict", id -> true).cookie("abc");
        assertFalse(cookie.isSecure());
        assertEquals("JSESSIONID", cookie.getName(), "__Host- cookies must be Secure");
        assertEquals("Strict", cookie.getSameSiteMode());
    }

    // Rejecting ids the server never issued is tested against a real server in SessionFixationTest.
}
