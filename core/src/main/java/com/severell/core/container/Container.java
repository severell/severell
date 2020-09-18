package com.severell.core.container;

import com.severell.core.http.NeedsRequest;
import com.severell.core.http.Request;
import com.severell.core.http.Response;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.function.Function;

/**
 * The Container is used to manage all the dependency injection for Severell.
 */
public class Container {

    private HashMap<String, Object> ioc;

    /**
     * Initializes a container
     */
    public Container() {
        ioc = new HashMap<String, Object>();
    }


    /**
     * Retrieve a dependency from the container. If object stored is {@link Function<Container, T>}
     * then the function is called to build a new instance of the requested dependency. Otherwise
     * this will return the object stored.
     * @param c Class - Used as the key
     * @param <T> Returns an instance of the Class passed in.
     * @return
     */
    public <T> T make(Class<T> c) {
        return make(c.getName(), c);
    }

    /**
     * Retrieve a dependency from the container. If object stored is {@link Function<Container, T>}
     * then the function is called to build a new instance of the requested dependency. Otherwise
     * this will return the object stored.
     * @param s The key used to look up in the container
     * @param c Class - used to determine the return type.
     * @param <T> Return type
     * @return
     */

    public <T> T make(String s, Class<T> c) {
        Object obj = ioc.get(s);

        if(obj instanceof Function) {
            return (T) ((Function<Container, T>) obj).apply(this);
        }

        return (T) obj;
    }

    /**
     * This binds an object to the container. Bind takes in a {@link Function<Container, Object>}. This
     * is used to "build" an instance of the requested object.
     * @param key - Class used as the key.
     * @param closure - Builder function. This function should return an instance of the object to be bound
     */
    public void bind(Class key, Function<Container,Object> closure) {
        bind(key.getName(), closure);
    }

    /**
     * This binds an object to the container. Bind takes in a {@link Function<Container, Object>}. This
     * is used to "build" an instance of the requested object.
     * @param key - String used as the key.
     * @param closure - Builder function. This function should return an instance of the object to be bound
     */
    public void bind(String key, Function<Container,Object> closure) {
        ioc.put(key, closure);
    }

    /**
     * This binds an instance of the object into the container. The object past in will be returned when
     * requested. It will always be the same instance. Hence the name singleton.
     * @param key - String used to store the object
     * @param obj - Object to store
     */
    public void singleton(String key, Object obj) {
        ioc.put(key, obj);
    }

    /**
     * This binds an instance of the object into the container. The object past in will be returned when
     * requested. It will always be the same instance. Hence the name singleton.
     * @param key - Class used to store the object
     * @param obj - Object to store
     */
    public void singleton(Class key, Object obj) {
        singleton(key.getName(), obj);
    }

    /**
     * Used to resolve dependencies for middleware and controllers. Should not be used outside of the
     * library. This is not used at Runtime but used to precompile the routes.
     * @param req - Request
     * @param resp - Response
     * @param meth - Method to resolve.
     * @return
     */
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

}
