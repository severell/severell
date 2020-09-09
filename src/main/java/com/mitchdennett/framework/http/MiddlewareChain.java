package com.mitchdennett.framework.http;

import com.mitchdennett.framework.container.Container;

import java.lang.reflect.InvocationTargetException;
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

    public void execute(Container c, Request request, Response response) throws InvocationTargetException, IllegalAccessException {
        this.container = c;
        this.request = request;
        this.response = response;

        if(middleware.hasNext()) {
            MiddlewareExecutor mapper = middleware.next();
            try {
                mapper.execute(request, response, container, this);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            try {
                target.execute(request, response, container);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void next() {
        this.next(this.request, this.response);
    }

    public void next(Request request, Response response) {
        this.request = request;
        this.response = response;

        if(middleware.hasNext()) {
            MiddlewareExecutor mapper = middleware.next();
            try {
               mapper.execute(request, response, container, this);
            } catch (Exception e) {
                e.printStackTrace();
            }

        } else {
            try {
                target.execute(request, response, container);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
