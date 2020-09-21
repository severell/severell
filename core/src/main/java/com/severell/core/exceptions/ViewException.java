package com.severell.core.exceptions;

/**
 * Used when exceptions are thrown in the View
 */
public class ViewException extends Exception{

    /**
     * Create a new ViewException.
     * @param e
     */
    public ViewException(Exception e) {
        super(e);
    }

    /**
     * Create a new ViewException.
     * @param e
     */
    public ViewException(String format) {
        super(format);
    }
}
