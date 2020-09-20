package com.severell.core.middleware;

import com.severell.core.http.MiddlewareChain;
import com.severell.core.http.Request;
import com.severell.core.http.Response;

/**
 * Base Middleware Class
 */
public abstract class BaseMiddleware implements Middleware {

    protected MiddlewareChain chain;

    public void setChain(MiddlewareChain chain) {
        this.chain = chain;
    }

    /**
     * This function is used to continue the Middleware chain. If next is not called
     * the request does not continue on.
     * @throws Exception
     */
    protected void next() throws Exception {
        chain.next();
    }

    /**
     * This function is used to continue the Middleware chain. If next is not called
     * the request does not continue on.
     * @param request
     * @param response
     * @throws Exception
     */
    protected void next(Request request, Response response) throws Exception {
        chain.next(request, response);
    }
}
