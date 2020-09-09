package com.mitchdennett.framework.container;

import com.mitchdennett.framework.http.NeedsRequest;
import com.mitchdennett.framework.http.Request;
import com.mitchdennett.framework.http.Response;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.function.Function;

public class Container {

    private HashMap<String, Object> ioc;

    public Container() {
        ioc = new HashMap<String, Object>();
    }

    public <T> T make(Class<T> c) {
        return make(c.getName(), c);
    }

    public <T> T make(String s, Class<T> c) {
        T obj =(T) ioc.get(s);

        if(obj instanceof Function) {
            return ((Function<Container, T>) obj).apply(this);
        }

        return obj;
    }

    public void bind(Class key, Function<Container,Object> closure) {
        bind(key.getName(), closure);
    }

    public void bind(String key, Function<Container,Object> closure) {
        ioc.put(key, closure);
    }

    public void singleton(String key, Object obj) {
        ioc.put(key, obj);
    }

    public void singleton(Class key, Object obj) {
        singleton(key.getName(), obj);
    }

    public Object[] resolve(Request req, Response resp, Method meth) {
        Class[] params = meth.getParameterTypes();
        ArrayList<Object> paramsToPass = new ArrayList<Object>(params.length);
        for (Class p : params) {
            if (p != Request.class && p != Response.class) {

                Object obj = make(p);

                if(obj instanceof NeedsRequest) {
                    ((NeedsRequest) obj).setRequest(req);
                }

                paramsToPass.add(p.cast(obj));
            } else {
                if (p == Request.class) {
                    paramsToPass.add(req);
                } else {
                    paramsToPass.add(resp);
                }
            }
        }

        return paramsToPass.toArray();
    }

    public void invoke(Request req, Response resp, Method meth, Object inst) throws InvocationTargetException, IllegalAccessException {
        meth.invoke(inst, resolve(req, resp, meth));
    }
}
