package com.mitchdennett.framework.http;

import com.mitchdennett.framework.container.Container;
import com.mitchdennett.framework.middleware.MiddlewareMapper;

import java.lang.reflect.InvocationTargetException;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

public class MiddlewareChain {

    private Iterator<MiddlewareMapper> middleware;
    private Route target;
    private Container container;

    private Response response;
    private Request request;

    public void setMiddleware(List<MiddlewareMapper> middleware) {
        Objects.requireNonNull(middleware);
        this.middleware = middleware.iterator();
    }

    public void setTarget(Route target) {
        this.target = target;
    }

    public void execute(Container c, Request request, Response response) throws InvocationTargetException, IllegalAccessException {
        this.container = c;
        this.request = request;
        this.response = response;

        if(middleware.hasNext()) {
            MiddlewareMapper mapper = middleware.next();
            mapper.run(c, request, response, this);
        } else {
            try {
                this.container.invoke(request, response, target.getMethod(), null);
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
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
            MiddlewareMapper mapper = middleware.next();
            try {
               mapper.run(container, request, response, this);
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        } else {
            try {
                this.container.invoke(request, response, target.getMethod(), null);
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

}
