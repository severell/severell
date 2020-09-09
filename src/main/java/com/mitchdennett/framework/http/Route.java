package com.mitchdennett.framework.http;

import com.mitchdennett.framework.exceptions.MiddlewareException;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;

public class Route {

    private String path;
    private Method method;
    private String httpMethod;
    private ArrayList<Class> middlewareClassList;

    protected Route(String path, String method, String httpMethod) throws ClassNotFoundException, NoSuchMethodException {
        this.path = path;
        String clazz = method.split("::")[0];
        String meth = method.split("::")[1];
        this.method = getMethodLike(Class.forName(clazz), meth);
        this.httpMethod = httpMethod;
    }

    public void middleware(Class... middleware) throws MiddlewareException{
        middlewareClassList = new ArrayList<>();
        middlewareClassList.addAll(Arrays.asList(middleware));
    }

    public Method getMethod() {
        return this.method;
    }

    public String getPath() {
        return this.path;
    }

    public String getHttpMethod() {
        return this.httpMethod;
    }

    public ArrayList<Class> getMiddlewareClassList() {
        return this.middlewareClassList;
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
