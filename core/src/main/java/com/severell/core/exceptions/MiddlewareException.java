package com.severell.core.exceptions;


/**
 * Used for exceptions that happen in Middleware
 */
public class MiddlewareException extends Exception {

    /**
     * Create a new MiddlewareException
     * @param format
     */
    public MiddlewareException(String format) {
        super(format);
    }

    /**
     * Create a new MiddlewareException
     * @param e
     */
    public MiddlewareException(Exception e) {
        super(e);
    }
}
