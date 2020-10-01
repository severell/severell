package com.severell.core.exceptions;


/**
 * Used for 404 exceptions
 */
public class NotFoundException extends Exception {

    /**
     * Create a new NotFoundException
     * @param format
     */
    public NotFoundException(String format) {
        super(format);
    }


}
