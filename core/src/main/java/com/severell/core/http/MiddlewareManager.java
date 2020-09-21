package com.severell.core.http;

import com.severell.core.container.Container;
import com.severell.core.exceptions.ControllerException;
import com.severell.core.exceptions.MiddlewareException;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

/**
 * The MiddlewareManager is responsible for executing all Middleware and then
 * eventually the target controller method. It sets up the MiddlewareChain and
 * executes it.
 */
public class MiddlewareManager {

    private MiddlewareChain chain;
    private RouteExecutor ref;
    private Container container;

    /**
     * Create a new MiddlewareManager
     * @param ref
     * @param container
     */
    public MiddlewareManager(RouteExecutor ref, Container container) {
        this.ref = ref;
        this.chain = new MiddlewareChain();
        this.container = container;
    }

    /**
     * This function gets the necessary middleware and then starts the Middleware chain
     * @param request
     * @param response
     * @throws Exception
     */
    public void filterRequest(Request request, Response response) throws Exception {
        List<MiddlewareExecutor> middleware = getMiddleware();
        this.chain.setMiddleware(middleware);
        this.chain.setTarget(this.ref);
        this.chain.execute(this.container, request, response);
    }

    /**
     * Gets the default middleware and the route specific middleware
     * @return
     */
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