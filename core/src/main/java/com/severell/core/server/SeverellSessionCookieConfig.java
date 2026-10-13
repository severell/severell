package com.severell.core.server;

import com.severell.core.session.SessionCookieSettings;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.handlers.Cookie;
import io.undertow.server.handlers.CookieImpl;
import io.undertow.server.session.SessionCookieConfig;

import java.util.Date;
import java.util.function.Predicate;

/**
 * The session cookie, with Secure, HttpOnly and SameSite set from {@link SessionCookieSettings}.
 * Undertow's own SessionCookieConfig can't set SameSite in this version.
 */
public class SeverellSessionCookieConfig extends SessionCookieConfig {

    private final boolean secure;
    private final boolean httpOnly;
    private final String sameSite;
    private final Predicate<String> sessionExists;

    /**
     * @param sessionExists whether a session id from a request names a session we created. Ids
     *                      that don't are ignored and a new id is issued, so nobody can choose
     *                      (and then plant) a session id.
     */
    public SeverellSessionCookieConfig(Predicate<String> sessionExists) {
        this(SessionCookieSettings.secure(), SessionCookieSettings.httpOnly(), SessionCookieSettings.sameSite(), sessionExists);
    }

    SeverellSessionCookieConfig(boolean secure, boolean httpOnly, String sameSite, Predicate<String> sessionExists) {
        this.sessionExists = sessionExists;
        this.secure = secure;
        this.httpOnly = httpOnly;
        this.sameSite = sameSite;
        setSecure(secure);
        setHttpOnly(httpOnly);
    }

    @Override
    public String findSessionId(HttpServerExchange exchange) {
        String sessionId = super.findSessionId(exchange);
        return sessionId != null && sessionExists.test(sessionId) ? sessionId : null;
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
