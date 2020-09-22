package com.severell.core.middleware;

import com.severell.core.http.MiddlewareChain;
import com.severell.core.http.Request;
import com.severell.core.http.Response;

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
