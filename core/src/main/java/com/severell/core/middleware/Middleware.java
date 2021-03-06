package com.severell.core.middleware;

import com.severell.core.http.*;

public interface Middleware {

    /**
     * Implement this when creating middleware.
     * @param request
     * @param response
     * @param chain
     * @throws Exception
     */
    void handle(Request request, Response response, MiddlewareChain chain) throws Exception;

}
