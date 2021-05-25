package com.severell.core.http;

import com.severell.core.container.Container;

import java.util.Iterator;
import java.util.List;
import java.util.Objects;

/**
 * This class represents a chain of Middleware. Each Middleware is executed one after the
 * other. Eventually the target controller function gets called.
 */
public class MiddlewareChain {

    private Iterator<MiddlewareExecutor> middleware;
    private RouteExecutor target;
    private Container container;

    private Response response;
    private Request request;

    public void setMiddleware(List<MiddlewareExecutor> middleware) {
        Objects.requireNonNull(middleware);
        this.middleware = middleware.iterator();
    }

    public void setTarget(RouteExecutor target) {
        this.target = target;
    }

    /**
     * Executes each middleware. One after the other.
     * @param c
     * @param request
     * @param response
     * @throws Exception
     * @return
     */
    public void execute(Container c, Request request, Response response) throws Exception {
        this.container = c;
        this.request = request;
        this.response = response;

        if(middleware.hasNext()) {
            MiddlewareExecutor mapper = middleware.next();
            mapper.execute(request, response, container, this);
        } else {
            target.execute(request, response, container);
        }
    }

    /**
     * Continues onto the next Middleware in the chain.
     * @throws Exception
     */
    public void next() throws Exception {
        this.next(this.request, this.response);
    }

    /**
     * Continues onto the next Middleware in the chain or onto the controller function.
     * @param request The Request to be passed onto the Controller function
     * @param response The Response to be passed onto the Controller function
     * @throws Exception
     */
    public void next(Request request, Response response) throws Exception {
        this.request = request;
        this.response = response;

        if(middleware.hasNext()) {
            MiddlewareExecutor mapper = middleware.next();
            mapper.execute(request, response, container, this);
        } else {
            target.execute(request, response, container);
        }
    }

}
