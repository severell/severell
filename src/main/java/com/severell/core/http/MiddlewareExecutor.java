package com.severell.core.http;

import com.severell.core.container.Container;
import com.severell.core.exceptions.MiddlewareException;

public class MiddlewareExecutor {

    @FunctionalInterface
    public interface MiddlewareFunction {
        void apply(Request request, Response response, Container container, MiddlewareChain chain) throws MiddlewareException;
    }

    private final MiddlewareFunction func;

    public MiddlewareExecutor(MiddlewareFunction func) {
        this.func = func;
    }

    public void execute(Request request, Response response, Container c, MiddlewareChain chain) throws MiddlewareException {
        func.apply(request, response, c, chain);
    }
}
