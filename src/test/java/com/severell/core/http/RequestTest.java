package com.severell.core.http;

import org.junit.jupiter.api.Test;

import javax.servlet.http.HttpServletRequest;

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


        assertEquals("test2", r.param("test1"));
        assertEquals("val1", r.param("key1"));
        assertNull(r.param("val999"));
        assertNull(r.param(null));

    }

    @Test
    public void parametersTestWithNull() {
        HttpServletRequest req = mock(HttpServletRequest.class);
        Request r = new Request(req);

        assertNull(r.param(null));

    }

    @Test
    public void inputTestWithNull() {
        HttpServletRequest req = mock(HttpServletRequest.class);
        Request r = new Request(req);

        assertNull(r.input(null));

    }
}
