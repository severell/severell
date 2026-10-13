package com.severell.core.middleware;

import com.severell.core.http.*;
import com.severell.core.session.Session;
import com.severell.core.session.SessionCookieSettings;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Objects;
import java.util.Set;

/**
 * Protects against cross-site request forgery.
 *
 * Requests that can change state (anything but GET, HEAD, OPTIONS and TRACE) must send the
 * CSRF token, as the {@code __token} form field or the {@code X-CSRF-TOKEN} or
 * {@code X-XSRF-TOKEN} header. Otherwise they're refused with 403. Templates get the token as
 * the shared {@code csrf} value, and JavaScript can read it from the {@code XSRF-TOKEN} cookie.
 * Tokens are masked per response (see {@link CsrfTokens}) so they can't leak through BREACH.
 *
 * The secret tokens are checked against lives in the session once the session holds anything
 * (e.g. a signed-in user). Until then it lives in an HttpOnly cookie, so a signed-out visitor
 * doesn't need a stored session. When served over https the cookie is named with the
 * {@code __Host-} prefix, so browsers won't let another subdomain set or overwrite it. Signing
 * in or out (a new session id) always starts a new secret.
 *
 * Routes or controllers marked {@link SkipCsrf} aren't checked.
 */
public class CsrfMiddleware implements Middleware {

    static final String SESSION_KEY = CsrfTokens.SESSION_KEY;
    static final String FORM_FIELD = "__token";
    static final String COOKIE_NAME = "XSRF-TOKEN";
    private static final Set<String> SAFE_METHODS = Set.of("GET", "HEAD", "OPTIONS", "TRACE");
    private static final Logger logger = LogManager.getLogger(CsrfMiddleware.class);

    /**
     * The Request session
     */
    private Session session;

    public CsrfMiddleware(Session session) {
        this.session = session;
    }

    @Override
    public void handle(Request request, Response response, MiddlewareChain chain) throws Exception {
        if (isSkipped(request)) {
            logger.debug("CSRF check skipped for {} {} (@SkipCsrf)", request.method(), request.path());
            chain.next();
            return;
        }

        String sessionSecret = validSecret(session.getString(SESSION_KEY));
        String cookieSecret = validSecret(cookieSecret(request));
        // The session's secret wins: a signed-in user's forms only work with their session's.
        String secret = sessionSecret != null ? sessionSecret : cookieSecret;
        boolean safe = request.method() != null && SAFE_METHODS.contains(request.method().toUpperCase());

        if (!safe && !CsrfTokens.matches(submittedToken(request), secret)) {
            HashMap<String, Object> body = new HashMap<>();
            body.put("error", "Invalid CSRF token. Reload the page and try again.");
            response.json(body, 403);
            return;
        }

        if (secret == null) {
            secret = CsrfTokens.newSecret();
        }
        String sessionId = session.getId();
        response.share("csrf", CsrfTokens.mask(secret));
        chain.next();

        if (!Objects.equals(sessionId, session.getId())) {
            // Signed in or out: the session (and any secret in it) was replaced. Start a new
            // secret so a token someone learned before can't be used after.
            secret = CsrfTokens.newSecret();
            if (session.isEmpty()) {
                response.header("Set-Cookie", secretCookie(secret));
            } else {
                session.put(SESSION_KEY, secret);
                response.header("Set-Cookie", expiredSecretCookie());
            }
        } else if (session.isEmpty()) {
            if (!secret.equals(cookieSecret)) {
                response.header("Set-Cookie", secretCookie(secret));
            }
        } else if (!secret.equals(validSecret(session.getString(SESSION_KEY)))) {
            // The session holds something now, so keep the secret with it. Pages already
            // showing this secret's tokens keep working.
            session.put(SESSION_KEY, secret);
        }
        response.header("Set-Cookie", xsrfCookie(CsrfTokens.mask(secret)));
    }

    private static String validSecret(String secret) {
        return CsrfTokens.isValidSecret(secret) ? secret : null;
    }

    private static String cookieSecret(Request request) {
        Cookie cookie = request.cookie(secretCookieName());
        return cookie == null ? null : cookie.getValue();
    }

    private static boolean isSkipped(Request request) {
        RequestContext context = request.context();
        RouteExecutor route = context == null ? null : context.get(RouteExecutor.CONTEXT_KEY, RouteExecutor.class);
        return route != null && route.isSkipCsrf();
    }

    private static String submittedToken(Request request) {
        // Tokens never belong in a URL, where logs and Referer headers would leak them, and
        // input() would fall back to the query string, so refuse a request that puts one there.
        if (request.query(FORM_FIELD) != null) {
            return null;
        }
        String token = request.input(FORM_FIELD);
        if (token == null || token.isEmpty()) {
            token = request.header("X-CSRF-TOKEN");
        }
        if (token == null || token.isEmpty()) {
            token = request.header("X-XSRF-TOKEN");
        }
        return token;
    }

    // __Host- cookies must be Secure, so the prefix is only used when cookies are.
    static String secretCookieName() {
        return SessionCookieSettings.secure() ? "__Host-csrf" : "csrf";
    }

    // The secret itself: never readable by JavaScript. Lax so it's there when someone follows
    // a link to the site, and the page they land on keeps the same secret as their other tabs.
    static String secretCookie(String secret) {
        return secretCookieName() + "=" + secret + "; Path=/; HttpOnly; SameSite=Lax"
                + (SessionCookieSettings.secure() ? "; Secure" : "");
    }

    static String expiredSecretCookie() {
        return secretCookieName() + "=; Path=/; Max-Age=0; HttpOnly; SameSite=Lax"
                + (SessionCookieSettings.secure() ? "; Secure" : "");
    }

    // Readable by the page's JavaScript (no HttpOnly) so it can send the header; Strict so other
    // sites never send it along.
    static String xsrfCookie(String maskedToken) {
        return COOKIE_NAME + "=" + maskedToken + "; Path=/; SameSite=Strict"
                + (SessionCookieSettings.secure() ? "; Secure" : "");
    }
}
