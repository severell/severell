package com.mitchdennett.framework.middleware;

import com.mitchdennett.framework.container.Container;
import com.mitchdennett.framework.http.MiddlewareChain;
import com.mitchdennett.framework.http.Request;
import com.mitchdennett.framework.http.Response;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class MiddlewareMapper {

    private Method meth;
    private Object inst;

    public MiddlewareMapper(Method meth, Object inst) {
        this.meth = meth;
        this.inst = inst;
    }

    public void run(Container c, Request req, Response resp, MiddlewareChain middlewareChain) throws InvocationTargetException, IllegalAccessException {
        c.hydrate(inst, req);
        meth.invoke(inst, req, resp, middlewareChain);
    }

}
