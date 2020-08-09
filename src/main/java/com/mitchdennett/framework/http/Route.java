package com.mitchdennett.framework.http;

import org.eclipse.jetty.http.HttpMethod;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Optional;

public class Route {

    private String path;
    private Method method;
    private HttpMethod httpMethod;

    protected Route(String path, String method, HttpMethod httpMethod) throws ClassNotFoundException, NoSuchMethodException {
        this.path = path;
        String clazz = method.split("::")[0];
        String meth = method.split("::")[1];
        this.method = getMethodLike(Class.forName(clazz), meth);
        this.httpMethod = httpMethod;
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
