package com.severell.core.exceptions;


public class MiddlewareException extends Exception {

    public MiddlewareException(String format) {
        super(format);
    }

    public MiddlewareException(Exception e) {
        super(e);
    }
}
