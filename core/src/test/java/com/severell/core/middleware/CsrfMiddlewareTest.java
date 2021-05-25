package com.severell.core.middleware;

import com.severell.core.http.*;
import com.severell.core.session.Session;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.UUID;
import java.util.function.Function;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class CsrfMiddlewareTest {

    @Test
    public void shouldStoreNewTokenInSession() throws Exception {
        Request request = mock(Request.class);
        given(request.method()).willReturn("GET");
        MiddlewareChain chain = mock(MiddlewareChain.class);

        Response resp = mock(Response.class);
        Session session = mock(Session.class);

        ArgumentCaptor<String> key = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Object> val = ArgumentCaptor.forClass(Object.class);

        CsrfMiddleware middleware = new CsrfMiddleware(session);

        middleware.handle(request, resp, chain);

        verify(session).put(key.capture(), val.capture());
        assertEquals("csrfToken", key.getValue());
        assertNotNull(val.getValue());
    }

    @Test
    public void shouldReturnStoredToken() throws Exception {
        String token = UUID.randomUUID().toString();
        Request request = mock(Request.class);
        given(request.method()).willReturn("GET");
        MiddlewareChain chain = mock(MiddlewareChain.class);

        Response resp = mock(Response.class);
        Session session = mock(Session.class);

        given(session.getString("csrfToken")).willReturn(token);

        ArgumentCaptor<String> key = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> val = ArgumentCaptor.forClass(String.class);

        CsrfMiddleware middleware = new CsrfMiddleware(session);

        middleware.handle(request, resp, chain);

        verify(resp).share(key.capture(), val.capture());
        assertEquals("csrf", key.getValue());
        assertEquals(token, val.getValue());
    }

    @Test
    public void noErrorShouldBeThrownWhenTokensMatch() throws Exception {
        String token = UUID.randomUUID().toString();

        Request request = mock(Request.class);
        given(request.method()).willReturn("POST");
        MiddlewareChain chain = mock(MiddlewareChain.class);

        Response resp = mock(Response.class);
        Session session = mock(Session.class);
        given(session.getString("csrfToken")).willReturn(token);
        given(request.input("__token")).willReturn(token);

        ArgumentCaptor<String> key = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> val = ArgumentCaptor.forClass(String.class);

        CsrfMiddleware middleware = new CsrfMiddleware(session);


        middleware.handle(request, resp, chain);

        verify(resp).share(key.capture(), val.capture());
        assertEquals("csrf", key.getValue());
        assertEquals(token, val.getValue());
    }

    @Test
    public void errorShouldBeThrownWhenTokensDontMatch() throws Exception {
        String token = UUID.randomUUID().toString();
        MiddlewareChain chain = mock(MiddlewareChain.class);

        Request request = mock(Request.class);
        given(request.method()).willReturn("POST");

        Response resp = mock(Response.class);
        Session session = mock(Session.class);
        given(session.getString("csrfToken")).willReturn(token);
        given(request.input("__token")).willReturn("");

        CsrfMiddleware middleware = new CsrfMiddleware(session);

        assertThrows(Exception.class, () -> {
            middleware.handle(request, resp, chain);
        });
    }

    @Test
    public void errorShouldBeThrownWhenNoTokenPassedUp() throws Exception {
        String token = UUID.randomUUID().toString();
        MiddlewareChain chain = mock(MiddlewareChain.class);

        Request request = mock(Request.class);
        given(request.method()).willReturn("POST");

        Response resp = mock(Response.class);
        Session session = mock(Session.class);
        given(session.getString("csrfToken")).willReturn(null);
        given(request.input("__token")).willReturn(null);

        CsrfMiddleware middleware = new CsrfMiddleware(session);

        assertThrows(Exception.class, () -> {
            middleware.handle(request, resp,  chain);
        });
    }

    @Test
    public void errorShouldBeThrownWhenNoTokenStored() throws Exception {
        Request request = mock(Request.class);
        given(request.method()).willReturn("POST");
        MiddlewareChain chain = mock(MiddlewareChain.class);

        Response resp = mock(Response.class);
        Session session = mock(Session.class);
        given(session.getString("csrfToken")).willReturn("");
        given(request.input("__token")).willReturn(null);

        CsrfMiddleware middleware = new CsrfMiddleware(session);

        assertThrows(Exception.class, () -> {
            middleware.handle(request,resp, chain);
        });
    }
}