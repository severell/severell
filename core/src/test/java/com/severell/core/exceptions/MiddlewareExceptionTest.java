package com.severell.core.exceptions;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class MiddlewareExceptionTest {

    @Test
    public void testMiddlewareException() {
        Exception e = new Exception("My Message");
        MiddlewareException ex = new MiddlewareException(e);
        assertEquals("java.lang.Exception: My Message", ex.getMessage());

        ex = new MiddlewareException("My Message");
        assertEquals("My Message", ex.getMessage());
    }}
