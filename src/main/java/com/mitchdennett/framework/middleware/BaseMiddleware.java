package com.mitchdennett.framework.middleware;

import com.mitchdennett.framework.http.MiddlewareChain;
import com.mitchdennett.framework.http.Request;
import com.mitchdennett.framework.http.Response;

public abstract class BaseMiddleware implements Middleware {

    protected MiddlewareChain chain;

    public void setChain(MiddlewareChain chain) {
        this.chain = chain;
    }

    protected void next(){
        chain.next();
    }

    protected void next(Request request, Response response){
        chain.next(request, response);
    }
}
