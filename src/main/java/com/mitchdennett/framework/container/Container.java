package com.mitchdennett.framework.container;

import com.mitchdennett.framework.http.NeedsRequest;
import com.mitchdennett.framework.http.Request;
import com.mitchdennett.framework.http.Response;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;

public class Container {

    private HashMap<String, Object> ioc;

    public Container() {
        ioc = new HashMap<String, Object>();
    }

    public <T> T make(Class<T> c) {
        T obj =(T) ioc.get(c.getName());
        return obj;
    }

    public <T> T make(String s, Class<T> c) {
        T obj =(T) ioc.get(s);
        return obj;
    }

    public void bind(String c, Object obj) {
        ioc.put(c, obj);
    }

    public void bind(Object c) {
        ioc.put(c.getClass().getName(), c);
    }

    public void bind(Class c, Object obj) {
        ioc.put(c.getName(), obj);
    }

    public void invoke(Request req, Response resp, Method meth, Object inst) throws InvocationTargetException, IllegalAccessException {
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

        meth.invoke(inst, paramsToPass.toArray());
    }
}
