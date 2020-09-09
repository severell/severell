package com.severell.core.http;

import com.severell.core.container.Container;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

public class MiddlewareManager {

    private MiddlewareChain chain;
    private RouteExecutor ref;
    private Container container;

    public MiddlewareManager(RouteExecutor ref, Container container) {
        this.ref = ref;
        this.chain = new MiddlewareChain();
        this.container = container;
    }

    public void filterRequest(Request request, Response response) throws InvocationTargetException, IllegalAccessException {
        List<MiddlewareExecutor> middleware = getMiddleware();
        this.chain.setMiddleware(middleware);
        this.chain.setTarget(this.ref);
        this.chain.execute(this.container, request, response);
    }

    private List<MiddlewareExecutor> getMiddleware() {
        ArrayList<MiddlewareExecutor> toRun = ref.getMiddleware();
        ArrayList<MiddlewareExecutor> defaultMiddleware = container.make("DefaultMiddleware", ArrayList.class);
        defaultMiddleware = defaultMiddleware == null ? new ArrayList<MiddlewareExecutor>() : defaultMiddleware;

        ArrayList<MiddlewareExecutor> middlewareList = new ArrayList<MiddlewareExecutor>();
        middlewareList.addAll(defaultMiddleware);
        middlewareList.addAll(toRun);

        return middlewareList;
    }
}