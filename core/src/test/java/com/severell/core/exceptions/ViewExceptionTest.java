package com.severell.core.exceptions;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ViewExceptionTest {

    @Test
    public void testViewException() {
        Exception e = new Exception("My Message");
        ViewException ex = new ViewException(e);
        assertEquals("java.lang.Exception: My Message", ex.getMessage());

        ex = new ViewException("My Message");
        assertEquals("My Message", ex.getMessage());
    }
}
