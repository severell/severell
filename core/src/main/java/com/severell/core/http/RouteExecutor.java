package com.severell.core.http;

import com.severell.core.container.Container;
import com.severell.core.exceptions.ControllerException;

import java.util.ArrayList;

/**
 * This class holds a reference to a {@link RouteFunction}.
 */
public class RouteExecutor {

    /**
     * The RouteFunction is a wrapper function around a Controller function. When executed it
     * calls the Controller function.
     */
    @FunctionalInterface
    public interface RouteFunction {
        void apply(Request request, Response response, Container container) throws Exception;
    }

    private final RouteFunction func;
    private final ArrayList<MiddlewareExecutor> middleware;
    public String path;
    public String httpMethod;

    public RouteExecutor(String path, String httpMethod, RouteFunction func) {
        this.path = path;
        this.httpMethod = httpMethod;
        this.func = func;
        this.middleware = null;
    }

    public RouteExecutor(String path, String httpMethod, ArrayList<MiddlewareExecutor> middleware, RouteExecutor.RouteFunction func) {
        this.path = path;
        this.httpMethod = httpMethod;
        this.func = func;
        this.middleware = middleware;
    }

    /**
     * Execute the associated Controller function.
     * @param request
     * @param response
     * @param cont
     * @throws Exception
     */
    public void execute(Request request, Response response, Container cont) throws Exception {
        func.apply(request, response, cont);
    }

    public RouteFunction getFunc() {
        return func;
    }

    /**
     * Get path from the associated route. I.E. /users
     * @return
     */
    public String getPath() {
        return path;
    }

    /**
     * Get the HTTP method. I.E. POST
     * @return
     */
    public String getHttpMethod() {
        return httpMethod;
    }

    /**
     * Get middleware associated with this route.
     * @return
     */
    public ArrayList<MiddlewareExecutor> getMiddleware() {
        return this.middleware;
    }
}
