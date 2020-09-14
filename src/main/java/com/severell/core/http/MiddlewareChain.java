package com.severell.core.http;

import com.severell.core.container.Container;
import com.severell.core.exceptions.ControllerException;
import com.severell.core.exceptions.MiddlewareException;

import java.util.Iterator;
import java.util.List;
import java.util.Objects;

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

    public void execute(Container c, Request request, Response response) throws MiddlewareException, ControllerException {
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

    public void next() throws MiddlewareException, ControllerException {
        this.next(this.request, this.response);
    }

    public void next(Request request, Response response) throws MiddlewareException, ControllerException {
        this.request = request;
        this.response = response;

        if(middleware.hasNext()) {
            try{
            MiddlewareExecutor mapper = middleware.next();
            mapper.execute(request, response, container, this);
            }catch (Exception e) {
                throw new MiddlewareException(e);
            }
        } else {
            target.execute(request, response, container);
        }
    }

}
