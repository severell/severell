package com.severell.core.exceptions;

public class ViewException extends Exception{

    public ViewException(Exception e) {
        super(e);
    }

    public ViewException(String format) {
        super(format);
    }
}
