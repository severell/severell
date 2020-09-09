package com.mitchdennett.framework.http;

import com.mitchdennett.framework.container.Container;

public class MiddlewareExecutor {

    @FunctionalInterface
    public interface MiddlewareFunction {
        void apply(Request request, Response response, Container container, MiddlewareChain chain) throws Exception;
    }

    private final MiddlewareFunction func;

    public MiddlewareExecutor(MiddlewareFunction func) {
        this.func = func;
    }

    public void execute(Request request, Response response, Container c, MiddlewareChain chain) throws Exception {
        func.apply(request, response, c, chain);
    }
}
