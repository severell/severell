package com.severell.core.middleware;

import com.severell.core.http.*;
import com.severell.core.session.Session;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

public class CsrfMiddlewareTest {

    private Request request(String method, RouteExecutor route) {
        Request request = mock(Request.class);
        given(request.method()).willReturn(method);
        given(request.path()).willReturn("/thing");
        RequestContext context = new RequestContext();
        if (route != null) {
            context.set(RouteExecutor.CONTEXT_KEY, route);
        }
        given(request.context()).willReturn(context);
        return request;
    }

    private Session sessionWith(String secret) {
        Session session = mock(Session.class);
        given(session.getString("csrfToken")).willReturn(secret);
        return session;
    }

    private String sharedToken(Response resp) {
        ArgumentCaptor<Object> val = ArgumentCaptor.forClass(Object.class);
        verify(resp).share(eq("csrf"), val.capture());
        return (String) val.getValue();
    }

    // A session that remembers what's put in it, unlike a mock.
    private static class MapSession implements Session {
        final java.util.Map<String, Object> data = new HashMap<>();
        String id = "id";
        public Object get(String key) { return data.get(key); }
        public String getString(String key) { return (String) data.get(key); }
        public <T> T get(String key, Class<T> c) { return c.cast(data.get(key)); }
        public String getId() { return id; }
        public void put(String key, Object value) { if (value == null) data.remove(key); else data.put(key, value); }
        public void setRequest(Request r) { }
        public boolean isEmpty() { return data.isEmpty(); }
    }

    private static MapSession signedIn() {
        MapSession session = new MapSession();
        session.put("userid", 7);
        return session;
    }

    private static MiddlewareChain chainThat(Runnable handler) throws Exception {
        MiddlewareChain chain = mock(MiddlewareChain.class);
        doAnswer(i -> { handler.run(); return null; }).when(chain).next();
        return chain;
    }

    private static Request withSecretCookie(Request request, String secret) {
        Cookie cookie = mock(Cookie.class);
        given(cookie.getValue()).willReturn(secret);
        given(request.cookie(CsrfMiddleware.secretCookieName())).willReturn(cookie);
        return request;
    }

    private static java.util.List<String> setCookies(Response resp) {
        ArgumentCaptor<String> header = ArgumentCaptor.forClass(String.class);
        verify(resp, atLeast(0)).header(eq("Set-Cookie"), header.capture());
        return header.getAllValues();
    }

    private static String secretFromCookies(Response resp) {
        String prefix = CsrfMiddleware.secretCookieName() + "=";
        return setCookies(resp).stream().filter(c -> c.startsWith(prefix))
                .map(c -> c.substring(prefix.length(), c.indexOf(';'))).findFirst().orElse(null);
    }

    @Test
    public void getStoresANewSecretAndSharesAMaskedToken() throws Exception {
        Response resp = mock(Response.class);
        MapSession session = signedIn();
        MiddlewareChain chain = mock(MiddlewareChain.class);

        new CsrfMiddleware(session).handle(request("GET", null), resp, chain);

        String secret = session.getString("csrfToken");
        assertTrue(CsrfTokens.isValidSecret(secret));
        assertTrue(CsrfTokens.matches(sharedToken(resp), secret));
        verify(chain).next();
    }

    @Test
    public void getReusesTheStoredSecret() throws Exception {
        String secret = CsrfTokens.newSecret();
        Response resp = mock(Response.class);
        Session session = sessionWith(secret);

        new CsrfMiddleware(session).handle(request("GET", null), resp, mock(MiddlewareChain.class));

        verify(session, never()).put(anyString(), any());
        String shared = sharedToken(resp);
        assertNotEquals(secret, shared);
        assertTrue(CsrfTokens.matches(shared, secret));
    }

    @Test
    public void setsAStrictXsrfCookieWithAMaskedToken() throws Exception {
        String secret = CsrfTokens.newSecret();
        Response resp = mock(Response.class);

        new CsrfMiddleware(sessionWith(secret)).handle(request("GET", null), resp, mock(MiddlewareChain.class));

        ArgumentCaptor<String> header = ArgumentCaptor.forClass(String.class);
        verify(resp).header(eq("Set-Cookie"), header.capture());
        String cookie = header.getValue();
        assertTrue(cookie.startsWith("XSRF-TOKEN="));
        assertTrue(cookie.contains("SameSite=Strict"));
        String token = cookie.substring("XSRF-TOKEN=".length(), cookie.indexOf(';'));
        assertTrue(CsrfTokens.matches(token, secret));
    }

    @Test
    public void postWithAValidFormTokenPasses() throws Exception {
        String secret = CsrfTokens.newSecret();
        Request request = request("POST", null);
        given(request.input("__token")).willReturn(CsrfTokens.mask(secret));
        MiddlewareChain chain = mock(MiddlewareChain.class);
        Response resp = mock(Response.class);

        new CsrfMiddleware(sessionWith(secret)).handle(request, resp, chain);

        verify(chain).next();
        verify(resp, never()).json(any(), eq(403));
    }

    @Test
    public void validHeaderTokensPass() throws Exception {
        for (String header : new String[]{"X-CSRF-TOKEN", "X-XSRF-TOKEN"}) {
            String secret = CsrfTokens.newSecret();
            Request request = request("PATCH", null);
            given(request.header(header)).willReturn(CsrfTokens.mask(secret));
            MiddlewareChain chain = mock(MiddlewareChain.class);

            new CsrfMiddleware(sessionWith(secret)).handle(request, mock(Response.class), chain);

            verify(chain).next();
        }
    }

    @Test
    public void unsafeMethodsWithoutAValidTokenAreRefusedWith403() throws Exception {
        String secret = CsrfTokens.newSecret();
        for (String method : new String[]{"POST", "PUT", "PATCH", "DELETE", "post"}) {
            for (String token : new String[]{null, "", "junk", secret, CsrfTokens.mask(CsrfTokens.newSecret())}) {
                Request request = request(method, null);
                given(request.input("__token")).willReturn(token);
                MiddlewareChain chain = mock(MiddlewareChain.class);
                Response resp = mock(Response.class);

                new CsrfMiddleware(sessionWith(secret)).handle(request, resp, chain);

                verify(chain, never()).next();
                verify(resp).json(any(HashMap.class), eq(403));
            }
        }
    }

    @Test
    public void postIsRefusedWhenTheSessionHasNoSecret() throws Exception {
        Request request = request("POST", null);
        given(request.input("__token")).willReturn(CsrfTokens.mask(CsrfTokens.newSecret()));
        MiddlewareChain chain = mock(MiddlewareChain.class);
        Response resp = mock(Response.class);

        new CsrfMiddleware(sessionWith(null)).handle(request, resp, chain);

        verify(chain, never()).next();
        verify(resp).json(any(HashMap.class), eq(403));
    }

    @Test
    public void tokensFromBeforeMaskingAreReplacedOnGetAndRefusedOnPost() throws Exception {
        String legacy = UUID.randomUUID().toString();

        MapSession session = signedIn();
        session.put("csrfToken", legacy);
        new CsrfMiddleware(session).handle(request("GET", null), mock(Response.class), mock(MiddlewareChain.class));
        assertTrue(CsrfTokens.isValidSecret(session.getString("csrfToken")));

        Request post = request("POST", null);
        given(post.input("__token")).willReturn(legacy);
        MiddlewareChain chain = mock(MiddlewareChain.class);
        new CsrfMiddleware(sessionWith(legacy)).handle(post, mock(Response.class), chain);
        verify(chain, never()).next();
    }

    @Test
    public void safeMethodsArentChecked() throws Exception {
        for (String method : new String[]{"GET", "HEAD", "OPTIONS", "TRACE"}) {
            MiddlewareChain chain = mock(MiddlewareChain.class);
            new CsrfMiddleware(sessionWith(CsrfTokens.newSecret())).handle(request(method, null), mock(Response.class), chain);
            verify(chain).next();
        }
    }

    @Test
    public void skippedRoutesArentCheckedAndGetNoToken() throws Exception {
        RouteExecutor route = new RouteExecutor("/api", HttpMethod.POST, new ArrayList<>(), true, (r, s, c) -> {});
        Session session = sessionWith(null);
        MiddlewareChain chain = mock(MiddlewareChain.class);
        Response resp = mock(Response.class);

        new CsrfMiddleware(session).handle(request("POST", route), resp, chain);

        verify(chain).next();
        verify(resp, never()).json(any(), anyInt());
        verify(resp, never()).share(anyString(), any());
        verify(session, never()).put(anyString(), any());
    }

    @Test
    public void routesWithoutSkipCsrfAreChecked() throws Exception {
        RouteExecutor route = new RouteExecutor("/form", HttpMethod.POST, new ArrayList<>(), (r, s, c) -> {});
        MiddlewareChain chain = mock(MiddlewareChain.class);

        new CsrfMiddleware(sessionWith(CsrfTokens.newSecret())).handle(request("POST", route), mock(Response.class), chain);

        verify(chain, never()).next();
    }

    @Test
    public void aTokenInTheQueryStringIsRefused() throws Exception {
        String secret = CsrfTokens.newSecret();
        String masked = CsrfTokens.mask(secret);
        Request request = request("POST", null);
        given(request.query("__token")).willReturn(masked);
        given(request.input("__token")).willReturn(masked);
        MiddlewareChain chain = mock(MiddlewareChain.class);
        Response resp = mock(Response.class);

        new CsrfMiddleware(sessionWith(secret)).handle(request, resp, chain);

        verify(chain, never()).next();
        verify(resp).json(any(HashMap.class), eq(403));
    }

    @Test
    public void signedOutVisitorsGetTheSecretInACookieNotTheSession() throws Exception {
        MapSession session = new MapSession();
        Response resp = mock(Response.class);

        new CsrfMiddleware(session).handle(request("GET", null), resp, mock(MiddlewareChain.class));

        assertTrue(session.isEmpty(), "nothing stored, so nothing to save");
        String secret = secretFromCookies(resp);
        assertTrue(CsrfTokens.isValidSecret(secret));
        assertTrue(CsrfTokens.matches(sharedToken(resp), secret));
        String cookie = setCookies(resp).stream().filter(c -> c.startsWith(CsrfMiddleware.secretCookieName())).findFirst().get();
        assertTrue(cookie.contains("HttpOnly") && cookie.contains("SameSite=Lax") && cookie.contains("Path=/"));
        assertFalse(cookie.contains("Domain"));
    }

    @Test
    public void aSignedOutVisitorsCookieIsReusedNotReissued() throws Exception {
        String secret = CsrfTokens.newSecret();
        Response resp = mock(Response.class);

        new CsrfMiddleware(new MapSession()).handle(withSecretCookie(request("GET", null), secret), resp, mock(MiddlewareChain.class));

        assertNull(secretFromCookies(resp));
        assertTrue(CsrfTokens.matches(sharedToken(resp), secret));
    }

    @Test
    public void signedOutPostsAreCheckedAgainstTheCookie() throws Exception {
        String secret = CsrfTokens.newSecret();
        Request ok = withSecretCookie(request("POST", null), secret);
        given(ok.input("__token")).willReturn(CsrfTokens.mask(secret));
        MiddlewareChain chain = mock(MiddlewareChain.class);
        MapSession session = new MapSession();
        new CsrfMiddleware(session).handle(ok, mock(Response.class), chain);
        verify(chain).next();
        assertTrue(session.isEmpty());

        // A token without the matching cookie, e.g. sent from another site.
        Request noCookie = request("POST", null);
        given(noCookie.input("__token")).willReturn(CsrfTokens.mask(secret));
        MiddlewareChain refused = mock(MiddlewareChain.class);
        Response resp = mock(Response.class);
        new CsrfMiddleware(new MapSession()).handle(noCookie, resp, refused);
        verify(refused, never()).next();
        verify(resp).json(any(HashMap.class), eq(403));
    }

    @Test
    public void onceTheSessionHasASecretTheCookieOneIsRefused() throws Exception {
        String cookieSecret = CsrfTokens.newSecret();
        MapSession session = signedIn();
        session.put("csrfToken", CsrfTokens.newSecret());
        Request post = withSecretCookie(request("POST", null), cookieSecret);
        given(post.input("__token")).willReturn(CsrfTokens.mask(cookieSecret));
        MiddlewareChain chain = mock(MiddlewareChain.class);

        new CsrfMiddleware(session).handle(post, mock(Response.class), chain);

        verify(chain, never()).next();
    }

    @Test
    public void aSessionHoldingSomethingWithoutASecretRefusesTheCookiesSecret() throws Exception {
        String cookieSecret = CsrfTokens.newSecret();
        Request post = withSecretCookie(request("POST", null), cookieSecret);
        given(post.input("__token")).willReturn(CsrfTokens.mask(cookieSecret));
        MiddlewareChain chain = mock(MiddlewareChain.class);

        new CsrfMiddleware(signedIn()).handle(post, mock(Response.class), chain);

        verify(chain, never()).next();
    }

    @Test
    public void whenTheSessionFirstHoldsSomethingItGetsItsOwnSecret() throws Exception {
        String secret = CsrfTokens.newSecret();
        MapSession session = new MapSession();
        MiddlewareChain chain = chainThat(() -> session.put("after_login", "/somewhere"));

        new CsrfMiddleware(session).handle(withSecretCookie(request("GET", null), secret), mock(Response.class), chain);

        assertTrue(CsrfTokens.isValidSecret(session.getString("csrfToken")));
        assertNotEquals(secret, session.getString("csrfToken"), "never adopt a secret that may have been seen before");
    }

    @Test
    public void aHandlerThatFailsAfterSigningInStillGetsANewSecret() throws Exception {
        String before = CsrfTokens.newSecret();
        MapSession session = new MapSession();
        Request post = withSecretCookie(request("POST", null), before);
        given(post.input("__token")).willReturn(CsrfTokens.mask(before));
        Response resp = mock(Response.class);
        MiddlewareChain signInThenFail = chainThat(() -> {
            session.id = "new";
            session.put("userid", 7);
            throw new IllegalStateException("boom");
        });

        assertThrows(IllegalStateException.class, () -> new CsrfMiddleware(session).handle(post, resp, signInThenFail));

        assertTrue(CsrfTokens.isValidSecret(session.getString("csrfToken")));
        assertNotEquals(before, session.getString("csrfToken"));
        assertTrue(setCookies(resp).stream().anyMatch(c -> c.contains("Max-Age=0")));
    }

    @Test
    public void signingInStartsANewSecretInTheSession() throws Exception {
        String before = CsrfTokens.newSecret();
        MapSession session = new MapSession();
        Request post = withSecretCookie(request("POST", null), before);
        given(post.input("__token")).willReturn(CsrfTokens.mask(before));
        Response resp = mock(Response.class);
        MiddlewareChain signIn = chainThat(() -> { session.id = "new"; session.put("userid", 7); });

        new CsrfMiddleware(session).handle(post, resp, signIn);

        String after = session.getString("csrfToken");
        assertTrue(CsrfTokens.isValidSecret(after));
        assertNotEquals(before, after);
        assertTrue(setCookies(resp).stream().anyMatch(c -> c.startsWith(CsrfMiddleware.secretCookieName() + "=;") && c.contains("Max-Age=0")));
        String xsrf = setCookies(resp).stream().filter(c -> c.startsWith("XSRF-TOKEN=")).findFirst().get();
        assertTrue(CsrfTokens.matches(xsrf.substring("XSRF-TOKEN=".length(), xsrf.indexOf(';')), after));
    }

    @Test
    public void signingOutStartsANewSecretInTheCookie() throws Exception {
        String before = CsrfTokens.newSecret();
        MapSession session = signedIn();
        session.put("csrfToken", before);
        Request post = request("POST", null);
        given(post.input("__token")).willReturn(CsrfTokens.mask(before));
        Response resp = mock(Response.class);
        MiddlewareChain signOut = chainThat(() -> { session.id = "new"; session.data.clear(); });

        new CsrfMiddleware(session).handle(post, resp, signOut);

        assertTrue(session.isEmpty());
        String after = secretFromCookies(resp);
        assertTrue(CsrfTokens.isValidSecret(after));
        assertNotEquals(before, after);
    }
}
