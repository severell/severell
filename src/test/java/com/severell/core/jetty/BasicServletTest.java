package com.severell.core.jetty;

import com.severell.core.container.Container;
import com.severell.core.http.*;
import org.apache.commons.dbcp2.BasicDataSource;
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

    @Test
    public void testDoGet() throws Exception {
        Container c = mock(Container.class);
        Router r = mock(Router.class);
        RouteExecutor executor = mock(RouteExecutor.class);
        given(c.make(Router.class)).willReturn(r);

        Dispatcher dispatcher = new Dispatcher(c);
        given(c.make(Dispatcher.class)).willReturn(dispatcher);

        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        given(resp.getWriter()).willReturn(mock(PrintWriter.class));
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
        Container c = mock(Container.class);
        Router r = mock(Router.class);
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        given(resp.getWriter()).willReturn(mock(PrintWriter.class));
        RouteExecutor executor = mock(RouteExecutor.class);

        given(c.make(Router.class)).willReturn(r);

        Dispatcher dispatcher = new Dispatcher(c);
        given(c.make(Dispatcher.class)).willReturn(dispatcher);

        setUpRequest(c, r, req, "POST",new ArrayList<>(), executor);

        BasicServlet servlet = new BasicServlet(c);
        servlet.doPost(req, resp);

        ArgumentCaptor<Request> reqCapt = ArgumentCaptor.forClass(Request.class);
        ArgumentCaptor<Response> resCapt = ArgumentCaptor.forClass(Response.class);
        ArgumentCaptor<Container> containerArgumentCaptor = ArgumentCaptor.forClass(Container.class);

        verify(executor).execute(reqCapt.capture(), resCapt.capture(), containerArgumentCaptor.capture());

        assertEquals("POST", reqCapt.getValue().getMethod());

    }

//    @Test
//    public void testDoGetWithBeforeMiddleware() throws InvocationTargetException, IllegalAccessException, IOException {
//        Container c = mock(Container.class);
//        Router r = mock(Router.class);
//        HttpServletRequest req = mock(HttpServletRequest.class);
//        HttpServletResponse resp = mock(HttpServletResponse.class);
//        given(resp.getWriter()).willReturn(mock(PrintWriter.class));
//
//        doMiddlewareTest(c, r, req, resp, "DefaultMiddleware");
//    }

//    private void doMiddlewareTest(Container c, Router r, HttpServletRequest req, HttpServletResponse resp, String middlewareType) throws IOException, InvocationTargetException, IllegalAccessException {
//        MiddlewareMapper mapper = mock(MiddlewareMapper.class);
//        doAnswer(invocation -> {
//            MiddlewareChain chain = invocation.getArgument(3);
//            chain.next();
//            return null;
//        }).when(mapper).run(any(Container.class), any(Request.class), any(Response.class), any(MiddlewareChain.class));
//        ArrayList<MiddlewareMapper> beforeList = new ArrayList<>();
//        beforeList.add(mapper);
//
//        MiddlewareMapper mapperTwo = mock(MiddlewareMapper.class);
//        doAnswer(invocation -> {
//            MiddlewareChain chain = invocation.getArgument(3);
//            chain.next();
//            return null;
//        }).when(mapperTwo).run(any(Container.class), any(Request.class), any(Response.class), any(MiddlewareChain.class));
//
//        ArrayList<MiddlewareMapper> defaultList = new ArrayList<>();
//        defaultList.add(mapperTwo);
//        given(c.make(middlewareType, ArrayList.class)).willReturn(defaultList);
//
//        setUpRequest(c, r, req, "GET", beforeList);
//
//        BasicServlet servlet = new BasicServlet(c, r);
//        servlet.doPost(req, resp);
//
//        ArgumentCaptor<Container> contCaptor = ArgumentCaptor.forClass(Container.class);
//        ArgumentCaptor<Request> reqCaptor = ArgumentCaptor.forClass(Request.class);
//        ArgumentCaptor<Response> respCaptor = ArgumentCaptor.forClass(Response.class);
//        ArgumentCaptor<MiddlewareChain> chainCaptor = ArgumentCaptor.forClass(MiddlewareChain.class);
//
//        verify(mapper).run(contCaptor.capture(), reqCaptor.capture(), respCaptor.capture(), chainCaptor.capture());
//
//        ArgumentCaptor<Container> contCaptor2 = ArgumentCaptor.forClass(Container.class);
//        ArgumentCaptor<Request> reqCaptor2 = ArgumentCaptor.forClass(Request.class);
//        ArgumentCaptor<Response> respCaptor2 = ArgumentCaptor.forClass(Response.class);
//        ArgumentCaptor<MiddlewareChain> chainCaptor2 = ArgumentCaptor.forClass(MiddlewareChain.class);
//
//        verify(mapperTwo).run(contCaptor2.capture(), reqCaptor2.capture(), respCaptor2.capture(), chainCaptor2.capture());
//    }

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
