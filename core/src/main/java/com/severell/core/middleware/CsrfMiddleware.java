package com.severell.core.middleware;

import com.severell.core.http.*;
import com.severell.core.session.Session;
import com.severell.core.session.SessionCookieSettings;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
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

        String secret = session.getString(SESSION_KEY);
        boolean safe = request.method() != null && SAFE_METHODS.contains(request.method().toUpperCase());

        if (!safe && !CsrfTokens.matches(submittedToken(request), secret)) {
            HashMap<String, Object> body = new HashMap<>();
            body.put("error", "Invalid CSRF token. Reload the page and try again.");
            response.json(body, 403);
            return;
        }

        response.share("csrf", CsrfTokens.mask(currentSecret()));
        chain.next();
        // Read it again: signing in or out replaces the session and its secret mid-request.
        response.header("Set-Cookie", xsrfCookie(CsrfTokens.mask(currentSecret())));
    }

    private String currentSecret() {
        String secret = session.getString(SESSION_KEY);
        if (!CsrfTokens.isValidSecret(secret)) {
            secret = CsrfTokens.newSecret();
            session.put(SESSION_KEY, secret);
        }
        return secret;
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

    // Readable by the page's JavaScript (no HttpOnly) so it can send the header; Strict so other
    // sites never send it along.
    static String xsrfCookie(String maskedToken) {
        return COOKIE_NAME + "=" + maskedToken + "; Path=/; SameSite=Strict"
                + (SessionCookieSettings.secure() ? "; Secure" : "");
    }
}
