package com.severell.core.jetty;

import com.severell.core.container.Container;
import com.severell.core.http.*;
import org.apache.commons.dbcp2.BasicDataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class BasicServletTest {

    private Container c;
    private Router r;
    private Dispatcher dispatcher;
    private HttpServletRequest req;
    private HttpServletResponse resp;
    private RouteExecutor executor;

    @BeforeEach
    public void setup() throws IOException {
        c = mock(Container.class);
        r = mock(Router.class);

        req = mock(HttpServletRequest.class);
        resp = mock(HttpServletResponse.class);
        given(resp.getWriter()).willReturn(mock(PrintWriter.class));
        executor = mock(RouteExecutor.class);
        given(c.make(Router.class)).willReturn(r);
        dispatcher = new Dispatcher(c);
        dispatcher.initRouter();
        given(c.make(Dispatcher.class)).willReturn(dispatcher);
    }

    @Test
    public void testDoGet() throws Exception {
        setUpRequest(c, r, req, "GET", new ArrayList<>(), executor);

        BasicServlet servlet = new BasicServlet(c);
        servlet.doGet(req, resp);

        ArgumentCaptor<Request> reqCapt = ArgumentCaptor.forClass(Request.class);
        ArgumentCaptor<Response> resCapt = ArgumentCaptor.forClass(Response.class);
        ArgumentCaptor<Container> containerArgumentCaptor = ArgumentCaptor.forClass(Container.class);

        verify(executor).execute(reqCapt.capture(), resCapt.capture(), containerArgumentCaptor.capture());

    }

    @Test
    public void testDoPost() throws Exception {
        setUpRequest(c, r, req, "POST",new ArrayList<>(), executor);

        BasicServlet servlet = new BasicServlet(c);
        servlet.doPost(req, resp);

        ArgumentCaptor<Request> reqCapt = ArgumentCaptor.forClass(Request.class);
        ArgumentCaptor<Response> resCapt = ArgumentCaptor.forClass(Response.class);
        ArgumentCaptor<Container> containerArgumentCaptor = ArgumentCaptor.forClass(Container.class);

        verify(executor).execute(reqCapt.capture(), resCapt.capture(), containerArgumentCaptor.capture());

        assertEquals("POST", reqCapt.getValue().getMethod());
    }

    @Test
    public void testDoPut() throws Exception {
        setUpRequest(c, r, req, "PUT",new ArrayList<>(), executor);

        BasicServlet servlet = new BasicServlet(c);
        servlet.doPut(req, resp);

        ArgumentCaptor<Request> reqCapt = ArgumentCaptor.forClass(Request.class);
        ArgumentCaptor<Response> resCapt = ArgumentCaptor.forClass(Response.class);
        ArgumentCaptor<Container> containerArgumentCaptor = ArgumentCaptor.forClass(Container.class);

        verify(executor).execute(reqCapt.capture(), resCapt.capture(), containerArgumentCaptor.capture());

        assertEquals("PUT", reqCapt.getValue().getMethod());
    }

    @Test
    public void testDoOptions() throws Exception {
        setUpRequest(c, r, req, "OPTIONS",new ArrayList<>(), executor);

        BasicServlet servlet = new BasicServlet(c);
        servlet.doOptions(req, resp);

        ArgumentCaptor<Request> reqCapt = ArgumentCaptor.forClass(Request.class);
        ArgumentCaptor<Response> resCapt = ArgumentCaptor.forClass(Response.class);
        ArgumentCaptor<Container> containerArgumentCaptor = ArgumentCaptor.forClass(Container.class);

        verify(executor).execute(reqCapt.capture(), resCapt.capture(), containerArgumentCaptor.capture());

        assertEquals("OPTIONS", reqCapt.getValue().getMethod());
    }

    @Test
    public void testDoDelete() throws Exception {
        setUpRequest(c, r, req, "DELETE",new ArrayList<>(), executor);

        BasicServlet servlet = new BasicServlet(c);
        servlet.doDelete(req, resp);

        ArgumentCaptor<Request> reqCapt = ArgumentCaptor.forClass(Request.class);
        ArgumentCaptor<Response> resCapt = ArgumentCaptor.forClass(Response.class);
        ArgumentCaptor<Container> containerArgumentCaptor = ArgumentCaptor.forClass(Container.class);

        verify(executor).execute(reqCapt.capture(), resCapt.capture(), containerArgumentCaptor.capture());

        assertEquals("DELETE", reqCapt.getValue().getMethod());
    }

    @Test
    public void testDoPatch() throws Exception {
        setUpRequest(c, r, req, "PATCH",new ArrayList<>(), executor);

        BasicServlet servlet = new BasicServlet(c);
        servlet.service(req, resp);

        ArgumentCaptor<Request> reqCapt = ArgumentCaptor.forClass(Request.class);
        ArgumentCaptor<Response> resCapt = ArgumentCaptor.forClass(Response.class);
        ArgumentCaptor<Container> containerArgumentCaptor = ArgumentCaptor.forClass(Container.class);

        verify(executor).execute(reqCapt.capture(), resCapt.capture(), containerArgumentCaptor.capture());

        assertEquals("PATCH", reqCapt.getValue().getMethod());
    }

    private void setUpRequest(Container c, Router r, HttpServletRequest req, String method, ArrayList middleware, RouteExecutor route) throws IOException {
        BasicDataSource ds = mock(BasicDataSource.class);
        given(c.make(BasicDataSource.class)).willReturn(ds);
        given(req.getMethod()).willReturn(method);
        given(req.getRequestURI()).willReturn("/");
        given(r.lookup(any(String.class), any(String.class), any(Request.class))).willReturn(route);
        given(route.getMiddleware()).willReturn(middleware);
//        given(route.getMiddlewareAfter()).willReturn(middleware);
        BufferedReader reader = mock(BufferedReader.class);
        given(req.getReader()).willReturn(reader);
    }
}
