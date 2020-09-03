package com.mitchdennett.framework.http;

import com.mitchdennett.framework.container.Container;
import com.mitchdennett.framework.middleware.MiddlewareMapper;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

public class MiddlewareManager {

    private MiddlewareChain chain;
    private Route ref;
    private Container container;

    public MiddlewareManager(Route ref, Container container) {
        this.ref = ref;
        this.chain = new MiddlewareChain();
        this.container = container;
    }

    public void filterRequest(Request request, Response response) throws InvocationTargetException, IllegalAccessException {
        List<MiddlewareMapper> middleware = getMiddleware();
        this.chain.setMiddleware(middleware);
        this.chain.setTarget(this.ref);
        this.chain.execute(this.container, request, response);
    }

    private List<MiddlewareMapper> getMiddleware() {
        ArrayList<MiddlewareMapper> toRun = ref.getMiddleware();
        ArrayList<MiddlewareMapper> defaultMiddleware = container.make("DefaultMiddleware", ArrayList.class);
        defaultMiddleware = defaultMiddleware == null ? new ArrayList<MiddlewareMapper>() : defaultMiddleware;

        ArrayList<MiddlewareMapper> middlewareList = new ArrayList<MiddlewareMapper>();
        middlewareList.addAll(defaultMiddleware);
        middlewareList.addAll(toRun);

        return middlewareList;
    }


}
