package com.severell.core.exceptions;

/**
 * Used when exceptions are thrown in controllers
 */
public class ControllerException extends Exception {

    /**
     * Creates a new ControllerException
     * @param e
     */
    public ControllerException(Exception e) {
        super(e);
    }
}
