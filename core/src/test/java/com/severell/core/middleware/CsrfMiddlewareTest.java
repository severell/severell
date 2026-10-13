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
        public Object get(String key) { return data.get(key); }
        public String getString(String key) { return (String) data.get(key); }
        public <T> T get(String key, Class<T> c) { return c.cast(data.get(key)); }
        public String getId() { return "id"; }
        public void put(String key, Object value) { data.put(key, value); }
        public void setRequest(Request r) { }
    }

    @Test
    public void getStoresANewSecretAndSharesAMaskedToken() throws Exception {
        Response resp = mock(Response.class);
        MapSession session = new MapSession();
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

        MapSession session = new MapSession();
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
    public void cookieUsesTheSecretInPlaceAfterTheRequest() throws Exception {
        // Signing in replaces the session and drops its secret while the request runs.
        String before = CsrfTokens.newSecret();
        Session session = mock(Session.class);
        given(session.getString("csrfToken")).willReturn(before, before, (String) null);
        Response resp = mock(Response.class);

        new CsrfMiddleware(session).handle(request("GET", null), resp, mock(MiddlewareChain.class));

        ArgumentCaptor<Object> after = ArgumentCaptor.forClass(Object.class);
        verify(session).put(eq("csrfToken"), after.capture());
        ArgumentCaptor<String> header = ArgumentCaptor.forClass(String.class);
        verify(resp).header(eq("Set-Cookie"), header.capture());
        String cookie = header.getValue();
        String token = cookie.substring("XSRF-TOKEN=".length(), cookie.indexOf(';'));
        assertTrue(CsrfTokens.matches(token, (String) after.getValue()));
        assertFalse(CsrfTokens.matches(token, before));
    }
}
