package com.severell.core.middleware;

import com.severell.core.http.MiddlewareChain;
import com.severell.core.http.Request;
import com.severell.core.http.Response;

public abstract class BaseMiddleware implements Middleware {

    protected MiddlewareChain chain;

    public void setChain(MiddlewareChain chain) {
        this.chain = chain;
    }

    protected void next() throws Exception {
        chain.next();
    }

    protected void next(Request request, Response response) throws Exception {
        chain.next(request, response);
    }
}
