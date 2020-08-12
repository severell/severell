package com.mitchdennett.framework.http;

import com.mitchdennett.framework.annotations.Before;
import com.mitchdennett.framework.middleware.MiddlewareMapper;
import org.eclipse.jetty.http.HttpMethod;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;

public class Route {

    private String path;
    private Method method;
    private HttpMethod httpMethod;
    private ArrayList middlewareBefore;


    protected Route(String path, String method, HttpMethod httpMethod) throws ClassNotFoundException, NoSuchMethodException {
        this.path = path;
        String clazz = method.split("::")[0];
        String meth = method.split("::")[1];
        this.method = getMethodLike(Class.forName(clazz), meth);
        this.httpMethod = httpMethod;
        this.middlewareBefore = new ArrayList();
    }

    public void middleware(Class... middleware) {
        for(Class p : middleware) {
            try {
                Object midd = p.getDeclaredConstructor().newInstance();
                for (Method method : midd.getClass().getDeclaredMethods()) {
                    if (method.isAnnotationPresent(Before.class)) {
                        method.setAccessible(true);
                        middlewareBefore.add(new MiddlewareMapper(method, midd));
                    }
                }
            }catch (Exception e) {
                e.printStackTrace();
                System.exit(1);
            }
        }
    }

    protected ArrayList<MiddlewareMapper> getMiddlewareBefore() {
        return this.middlewareBefore;
    }

    protected Method getMethod() {
        return this.method;
    }

    protected String getPath() {
        return this.path;
    }

    protected HttpMethod getHttpMethod() {
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
