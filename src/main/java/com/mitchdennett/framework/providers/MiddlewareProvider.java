package com.mitchdennett.framework.providers;

import com.mitchdennett.framework.container.Container;
import com.mitchdennett.framework.exceptions.MiddlewareException;
import com.mitchdennett.framework.http.MiddlewareChain;
import com.mitchdennett.framework.http.Request;
import com.mitchdennett.framework.http.Response;
import com.mitchdennett.framework.middleware.MiddlewareMapper;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;

public class MiddlewareProvider extends ServiceProvider{


    public MiddlewareProvider(Container c) {
        super(c);
    }

    @Override
    public void register() {

    }

    @Override
    public void boot() throws MiddlewareException {
        Class[] middleware = c.make("_MiddlewareList", Class[].class);

        ArrayList defaultMiddleware = new ArrayList<>();
        for(Class p : middleware) {
            try {
                Object midd = instantiateMiddleware(p);
                Method meth = midd.getClass().getMethod("handle", Request.class, Response.class, MiddlewareChain.class);
                defaultMiddleware.add(new MiddlewareMapper(meth, midd));
            }catch (NoSuchMethodException ex) {
                throw new MiddlewareException(String.format("Unable to resolve middleware"));
            }
        }

        c.bind("DefaultMiddleware",defaultMiddleware);
    }

    private Object instantiateMiddleware(Class p) throws MiddlewareException {
        try {
            return p.getConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new MiddlewareException(String.format("Middlware: %s has no valid constructor.", p.getSimpleName()));
        }
    }
}
