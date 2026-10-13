package com.severell.core.server;

import io.undertow.server.handlers.Cookie;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class SeverellSessionCookieConfigTest {

    @Test
    public void sessionCookieCarriesTheConfiguredFlags() {
        Cookie cookie = new SeverellSessionCookieConfig(true, true, "Lax").cookie("abc");
        assertEquals("JSESSIONID", cookie.getName());
        assertEquals("abc", cookie.getValue());
        assertEquals("/", cookie.getPath());
        assertTrue(cookie.isSecure());
        assertTrue(cookie.isHttpOnly());
        assertTrue(cookie.isSameSite());
        assertEquals("Lax", cookie.getSameSiteMode());
    }

    @Test
    public void localCookieCanSkipSecure() {
        Cookie cookie = new SeverellSessionCookieConfig(false, true, "Strict").cookie("abc");
        assertFalse(cookie.isSecure());
        assertEquals("Strict", cookie.getSameSiteMode());
    }
}
