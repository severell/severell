package com.mitchdennett.framework.middleware;

import com.mitchdennett.framework.drivers.Session;
import com.mitchdennett.framework.drivers.SessionMemoryDriver;
import com.mitchdennett.framework.http.Request;
import com.mitchdennett.framework.http.Response;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.util.UUID;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

public class CsrfMiddlewareTest {

    @Test
    public void shouldStoreNewTokenInSession() throws Exception {
        Request request = mock(Request.class);
        given(request.getMethod()).willReturn("GET");

        Response resp = mock(Response.class);
        Session session = mock(Session.class);

        ArgumentCaptor<String> key = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Object> val = ArgumentCaptor.forClass(Object.class);

        CsrfMiddleware middleware = new CsrfMiddleware();
        middleware.before(resp, request, session);

        verify(session).put(key.capture(), val.capture());
        assertEquals("csrfToken", key.getValue());
        assertNotNull(val.getValue());
    }

    @Test
    public void shouldReturnStoredToken() throws Exception {
        String token = UUID.randomUUID().toString();
        Request request = mock(Request.class);
        given(request.getMethod()).willReturn("GET");

        Response resp = mock(Response.class);
        Session session = mock(Session.class);

        given(session.getString("csrfToken")).willReturn(token);

        ArgumentCaptor<String> key = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Function<String,String>> val = ArgumentCaptor.forClass(Function.class);

        CsrfMiddleware middleware = new CsrfMiddleware();
        middleware.before(resp, request, session);

        verify(resp).share(key.capture(), val.capture());
        assertEquals("csrf", key.getValue());
        assertEquals(String.format("<input type='hidden' name='__token' value='%s' />", token), val.getValue().apply("something"));
    }

    @Test
    public void noErrorShouldBeThrownWhenTokensMatch() throws Exception {
        String token = UUID.randomUUID().toString();

        Request request = mock(Request.class);
        given(request.getMethod()).willReturn("POST");

        Response resp = mock(Response.class);
        Session session = mock(Session.class);
        given(session.getString("csrfToken")).willReturn(token);
        given(request.input("__token")).willReturn(token);

        ArgumentCaptor<String> key = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Function<String,String>> val = ArgumentCaptor.forClass(Function.class);

        CsrfMiddleware middleware = new CsrfMiddleware();
        middleware.before(resp, request, session);

        verify(resp).share(key.capture(), val.capture());
        assertEquals("csrf", key.getValue());
        assertEquals(String.format("<input type='hidden' name='__token' value='%s' />", token), val.getValue().apply("something"));
    }

    @Test
    public void errorShouldBeThrownWhenTokensDontMatch() throws Exception {
        String token = UUID.randomUUID().toString();

        Request request = mock(Request.class);
        given(request.getMethod()).willReturn("POST");

        Response resp = mock(Response.class);
        Session session = mock(Session.class);
        given(session.getString("csrfToken")).willReturn(token);
        given(request.input("__token")).willReturn("");

        CsrfMiddleware middleware = new CsrfMiddleware();

        assertThrows(Exception.class, () -> {
            middleware.before(resp, request, session);
        });
    }

    @Test
    public void errorShouldBeThrownWhenNoTokenPassedUp() throws Exception {
        String token = UUID.randomUUID().toString();

        Request request = mock(Request.class);
        given(request.getMethod()).willReturn("POST");

        Response resp = mock(Response.class);
        Session session = mock(Session.class);
        given(session.getString("csrfToken")).willReturn(null);
        given(request.input("__token")).willReturn(null);

        CsrfMiddleware middleware = new CsrfMiddleware();

        assertThrows(Exception.class, () -> {
            middleware.before(resp, request, session);
        });
    }

    @Test
    public void errorShouldBeThrownWhenNoTokenStored() throws Exception {
        Request request = mock(Request.class);
        given(request.getMethod()).willReturn("POST");

        Response resp = mock(Response.class);
        Session session = mock(Session.class);
        given(session.getString("csrfToken")).willReturn("");
        given(request.input("__token")).willReturn(null);

        CsrfMiddleware middleware = new CsrfMiddleware();

        assertThrows(Exception.class, () -> {
            middleware.before(resp, request, session);
        });
    }
}