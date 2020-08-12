package com.mitchdennett.framework.providers;

import com.mitchdennett.framework.annotations.After;
import com.mitchdennett.framework.annotations.Before;
import com.mitchdennett.framework.container.Container;
import com.mitchdennett.framework.middleware.MiddlewareMapper;
import com.mitchdennett.main.Middleware;
import com.mitchdennett.main.Providers;

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
    public void boot() {
        ArrayList defaultMiddlewareBefore = new ArrayList<>();
        for(Class p : Middleware.MIDDLEWARE) {
            try {
                Object midd = p.getDeclaredConstructor().newInstance();
                for (Method method : midd.getClass().getDeclaredMethods()) {
                    if (method.isAnnotationPresent(Before.class)) {
                        method.setAccessible(true);
                        defaultMiddlewareBefore.add(new MiddlewareMapper(method, midd));
                    }
                }
            }catch (Exception e) {
                e.printStackTrace();
                System.exit(1);
            }
        }
        c.bind("DefaultMiddlewareBefore",defaultMiddlewareBefore);

        ArrayList defaultMiddlewareAfter = new ArrayList<>();
        for(Class p : Middleware.MIDDLEWARE) {
            try {
                Object midd = p.getDeclaredConstructor().newInstance();
                for (Method method : midd.getClass().getDeclaredMethods()) {
                    if (method.isAnnotationPresent(After.class)) {
                        method.setAccessible(true);
                        defaultMiddlewareAfter.add(new MiddlewareMapper(method, midd));
                    }
                }
            }catch (Exception e) {
                e.printStackTrace();
                System.exit(1);
            }
        }
        c.bind("DefaultMiddlewareAfter",defaultMiddlewareAfter);
    }
}
