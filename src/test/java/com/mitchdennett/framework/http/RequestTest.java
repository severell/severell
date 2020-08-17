package com.mitchdennett.framework.http;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;

public class RequestTest {

    @Test
    public void parametersTest() {
        HttpServletRequest req = mock(HttpServletRequest.class);
        Request r = new Request(req);


        r.addParam("test1", "test2");
        r.addParam("key1", "val1");


        assertEquals("test2", r.getParam("test1"));
        assertEquals("val1", r.getParam("key1"));
        assertNull(r.getParam("val999"));
        assertNull(r.getParam(null));

    }

    @Test
    public void parametersTestWithNull() {
        HttpServletRequest req = mock(HttpServletRequest.class);
        Request r = new Request(req);

        assertNull(r.getParam(null));

    }

    @Test
    public void inputTestWithNull() {
        HttpServletRequest req = mock(HttpServletRequest.class);
        Request r = new Request(req);

        assertNull(r.input(null));

    }
}
