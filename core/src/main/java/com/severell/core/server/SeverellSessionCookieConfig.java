package com.severell.core.server;

import com.severell.core.session.SessionCookieSettings;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.handlers.Cookie;
import io.undertow.server.handlers.CookieImpl;
import io.undertow.server.session.SessionCookieConfig;

import java.util.Date;

/**
 * The session cookie, with Secure, HttpOnly and SameSite set from {@link SessionCookieSettings}.
 * Undertow's own SessionCookieConfig can't set SameSite in this version.
 */
public class SeverellSessionCookieConfig extends SessionCookieConfig {

    private final boolean secure;
    private final boolean httpOnly;
    private final String sameSite;

    public SeverellSessionCookieConfig() {
        this(SessionCookieSettings.secure(), SessionCookieSettings.httpOnly(), SessionCookieSettings.sameSite());
    }

    SeverellSessionCookieConfig(boolean secure, boolean httpOnly, String sameSite) {
        this.secure = secure;
        this.httpOnly = httpOnly;
        this.sameSite = sameSite;
        setSecure(secure);
        setHttpOnly(httpOnly);
    }

    @Override
    public void setSessionId(HttpServerExchange exchange, String sessionId) {
        exchange.setResponseCookie(cookie(sessionId));
    }

    @Override
    public void clearSession(HttpServerExchange exchange, String sessionId) {
        Cookie cookie = cookie(sessionId).setMaxAge(0).setExpires(new Date(0));
        exchange.setResponseCookie(cookie);
    }

    Cookie cookie(String sessionId) {
        return new CookieImpl(getCookieName(), sessionId)
                .setPath("/")
                .setSecure(secure)
                .setHttpOnly(httpOnly)
                .setSameSiteMode(sameSite);
    }
}
