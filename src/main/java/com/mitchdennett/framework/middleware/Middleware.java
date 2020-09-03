package com.mitchdennett.framework.middleware;

import com.mitchdennett.framework.http.MiddlewareChain;
import com.mitchdennett.framework.http.Request;
import com.mitchdennett.framework.http.Response;

public interface Middleware {

    public void handle(Request request, Response response, MiddlewareChain chain) throws Exception;
}
