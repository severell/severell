package com.mitchdennett.framework.http;

import com.mitchdennett.framework.container.Container;
import com.mitchdennett.framework.exceptions.MiddlewareException;
import com.mitchdennett.framework.middleware.MiddlewareMapper;
import org.eclipse.jetty.http.HttpMethod;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;

public class Route {

    private String path;
    private Method method;
    private String httpMethod;
    private ArrayList middleware;

    protected Route(String path, String method, String httpMethod) throws ClassNotFoundException, NoSuchMethodException {
        this.path = path;
        String clazz = method.split("::")[0];
        String meth = method.split("::")[1];
        this.method = getMethodLike(Class.forName(clazz), meth);
        this.httpMethod = httpMethod;
        this.middleware = new ArrayList();
    }

    public void middleware(Class... middleware) throws MiddlewareException{
        for(Class p : middleware) {
            try {
                Object midd = instantiateMiddleware(p);
                Method meth = midd.getClass().getMethod("handle", Request.class, Response.class, MiddlewareChain.class);
                this.middleware.add(new MiddlewareMapper(meth, midd));
            }catch (NoSuchMethodException ex) {
                throw new MiddlewareException(String.format("Unable to resolve middleware"));
            }
        }
    }

    private Object instantiateMiddleware(Class p) throws MiddlewareException {
        try {
            return p.getConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new MiddlewareException(String.format("Middlware: %s has no valid constructor.", p.getSimpleName()));
        }
    }

    protected ArrayList<MiddlewareMapper> getMiddleware() {
        return this.middleware;
    }

    protected Method getMethod() {
        return this.method;
    }

    protected String getPath() {
        return this.path;
    }

    protected String getHttpMethod() {
        return this.httpMethod;
    }

    private static Method getMethodLike(Class c, String name) {
        final Optional<Method> matchedMethod = Arrays.asList(c.getDeclaredMethods()).stream().filter(method ->
                method.getName().toLowerCase().equals(name.toLowerCase())).findAny();

        if (!matchedMethod.isPresent()) {
            throw new RuntimeException("No method containing: " + name);
        }

        return matchedMethod.get();
    }
}
