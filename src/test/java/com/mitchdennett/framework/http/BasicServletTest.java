package com.mitchdennett.framework.http;

import com.mitchdennett.framework.container.Container;
import com.mitchdennett.framework.middleware.MiddlewareMapper;
import org.apache.commons.dbcp2.BasicDataSource;
import org.eclipse.jetty.http.HttpMethod;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class BasicServletTest {

    @Test
    public void testDoGet() throws IOException, InvocationTargetException, IllegalAccessException {
        Container c = mock(Container.class);
        Router r = mock(Router.class);
        BasicDataSource ds = mock(BasicDataSource.class);
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        given(c.make(BasicDataSource.class)).willReturn(ds);
        given(req.getMethod()).willReturn("GET");
        given(req.getRequestURI()).willReturn("/");
        Route route = mock(Route.class);
        given(r.lookup(any(String.class), any(HttpMethod.class), any(Request.class))).willReturn(route);
        given(route.getMiddlewareBefore()).willReturn(new ArrayList<MiddlewareMapper>());

        BufferedReader reader = mock(BufferedReader.class);
        given(req.getReader()).willReturn(reader);

        BasicServlet servlet = new BasicServlet(c, r);
        servlet.doGet(req, resp);

        ArgumentCaptor<Object> objectCaptor = ArgumentCaptor.forClass(Object.class);
        ArgumentCaptor<Method> methodCaptor = ArgumentCaptor.forClass(Method.class);
        ArgumentCaptor<Request> reqCaptor = ArgumentCaptor.forClass(Request.class);
        ArgumentCaptor<Response> respCaptor = ArgumentCaptor.forClass(Response.class);

        verify(c).invoke(reqCaptor.capture(), respCaptor.capture(), methodCaptor.capture(), objectCaptor.capture());

        assertEquals("GET", reqCaptor.getValue().getMethod());


    }

    @Test
    public void testDoPost() throws InvocationTargetException, IllegalAccessException, IOException {
        Container c = mock(Container.class);
        Router r = mock(Router.class);
        BasicDataSource ds = mock(BasicDataSource.class);
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        given(c.make(BasicDataSource.class)).willReturn(ds);
        given(req.getMethod()).willReturn("POST");
        given(req.getRequestURI()).willReturn("/");
        Route route = mock(Route.class);
        given(r.lookup(any(String.class), any(HttpMethod.class), any(Request.class))).willReturn(route);
        given(route.getMiddlewareBefore()).willReturn(new ArrayList<MiddlewareMapper>());
        BufferedReader reader = mock(BufferedReader.class);
        given(req.getReader()).willReturn(reader);

        BasicServlet servlet = new BasicServlet(c, r);
        servlet.doPost(req, resp);

        ArgumentCaptor<Object> objectCaptor = ArgumentCaptor.forClass(Object.class);
        ArgumentCaptor<Method> methodCaptor = ArgumentCaptor.forClass(Method.class);
        ArgumentCaptor<Request> reqCaptor = ArgumentCaptor.forClass(Request.class);
        ArgumentCaptor<Response> respCaptor = ArgumentCaptor.forClass(Response.class);

        verify(c).invoke(reqCaptor.capture(), respCaptor.capture(), methodCaptor.capture(), objectCaptor.capture());

        assertEquals("POST", reqCaptor.getValue().getMethod());

    }
}
