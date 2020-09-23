package com.severell.core.http;

import org.junit.jupiter.api.Test;

import javax.servlet.http.HttpServletRequest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.BDDMockito.given;
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

    @Test
    public void testParseQueryString() {
        HttpServletRequest req = mock(HttpServletRequest.class);
        given(req.getQueryString()).willReturn("key=value&key2=value1&empty=");
        Request r = new Request(req);
        assertEquals("value", r.query("key"));
        assertEquals("value1", r.query("key2"));
        assertEquals("", r.query("empty"));

    }

    @Test
    public void testParseQueryStringReturnsNull() {
        HttpServletRequest req = mock(HttpServletRequest.class);
        given(req.getQueryString()).willReturn(null);
        Request r = new Request(req);
        assertEquals(null, r.query("key"));
    }
}
