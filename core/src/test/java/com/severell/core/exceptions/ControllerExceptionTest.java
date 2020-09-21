package com.severell.core.exceptions;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ControllerExceptionTest {

    @Test
    public void testControllerException() {
        Exception e = new Exception("My Message");
        ControllerException ex = new ControllerException(e);
        assertEquals("java.lang.Exception: My Message", ex.getMessage());
    }
}
