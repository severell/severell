package com.severell.core.http;

import com.severell.core.container.Container;

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

    /**
     * The key the matched route is stored under in {@link Request#context()} while middleware runs.
     */
    public static final String CONTEXT_KEY = "severell.route";

    private final RouteFunction func;
    private final ArrayList<MiddlewareExecutor> middleware;
    private final boolean skipCsrf;
    public String path;
    public HttpMethod httpMethod;

    public RouteExecutor(String path, HttpMethod httpMethod, RouteFunction func) {
        this.path = path;
        this.httpMethod = httpMethod;
        this.func = func;
        this.middleware = null;
        this.skipCsrf = false;
    }

    public RouteExecutor(String path, HttpMethod httpMethod, ArrayList<MiddlewareExecutor> middleware, RouteExecutor.RouteFunction func) {
        this(path, httpMethod, middleware, false, func);
    }

    /**
     * @param skipCsrf true if the route or its controller has {@link SkipCsrf}
     */
    public RouteExecutor(String path, HttpMethod httpMethod, ArrayList<MiddlewareExecutor> middleware, boolean skipCsrf, RouteExecutor.RouteFunction func) {
        this.path = path;
        this.httpMethod = httpMethod;
        this.func = func;
        this.middleware = middleware;
        this.skipCsrf = skipCsrf;
    }

    /**
     * Whether CSRF checks are turned off for this route with {@link SkipCsrf}.
     */
    public boolean isSkipCsrf() {
        return skipCsrf;
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
    public HttpMethod getHttpMethod() {
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
