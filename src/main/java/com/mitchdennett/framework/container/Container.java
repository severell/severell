package com.mitchdennett.framework.container;

import com.mitchdennett.framework.http.NeedsRequest;
import com.mitchdennett.framework.http.Request;
import com.mitchdennett.framework.http.Response;

import javax.inject.Inject;
import java.lang.annotation.Annotation;
import java.lang.reflect.*;
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

    public void hydrate(Object obj, Request request) throws IllegalAccessException {
        Field[] fields = obj.getClass().getDeclaredFields();

        for (Field p : fields) {

            boolean injectable = p.isAnnotationPresent(Inject.class);
            System.out.println(String.format("%s - %s", p.getName(), injectable));
            if(injectable) {
                p.setAccessible(true);
                System.out.println(p.getType().getSimpleName());
                Object fieldObj = make(p.getType());

                if(fieldObj instanceof NeedsRequest) {
                    ((NeedsRequest) fieldObj).setRequest(request);
                }

                p.set(obj, fieldObj);
            }
        }
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
