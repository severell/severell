package com.severell.core.http;

/**
 * If an classes extends this then the HTTP {@link Request} object will
 * automatically get injected. See {@link com.severell.core.drivers.BaseSessionDriver}
 */
public class NeedsRequest {

    protected Request request;

    public void setRequest(Request r) {
        this.request = r;
    }

}
