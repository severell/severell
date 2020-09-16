package com.severell.core.http;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * The router holds all the routes and is responsible for returning the route
 * for a given path.
 */
public class Router {

    private HashMap<String, RouteNode> trees;
    private static ArrayList<Route> routes = new ArrayList<Route>();
    private static ArrayList<RouteExecutor> compiledRoutes = new ArrayList<RouteExecutor>();

    /**
     * Register a GET route for the given path
     *
     * @param path
     * @param method
     * @return
     * @throws NoSuchMethodException
     * @throws ClassNotFoundException
     */
    public static Route Get(String path, String method) throws NoSuchMethodException, ClassNotFoundException {
        Route route = new Route(path, method, "GET");
        Route headRoute = new Route(path, method, "HEAD");
        routes.add(route);
        routes.add(headRoute);
        return route;
    }

    /**
     * Register a POST route for the given path
     *
     * @param path
     * @param method
     * @return
     * @throws NoSuchMethodException
     * @throws ClassNotFoundException
     */
    public static Route Post(String path, String method) throws NoSuchMethodException, ClassNotFoundException {
        Route route = new Route(path, method, "POST");
        routes.add(route);
        return route;
    }

    /**
     * Register a PUT route for the given path
     *
     * @param path
     * @param method
     * @return
     * @throws NoSuchMethodException
     * @throws ClassNotFoundException
     */
    public static Route Put(String path, String method) throws NoSuchMethodException, ClassNotFoundException {
        Route route = new Route(path, method, "PUT");
        routes.add(route);
        return route;
    }

    /**
     * Register a Patch route for the given path
     *
     * @param path
     * @param method
     * @return
     * @throws NoSuchMethodException
     * @throws ClassNotFoundException
     */
    public static Route Patch(String path, String method) throws NoSuchMethodException, ClassNotFoundException {
        Route route = new Route(path, method, "PATCH");
        routes.add(route);
        return route;
    }

    /**
     * Register a DELETE route for the given path
     *
     * @param path
     * @param method
     * @return
     * @throws NoSuchMethodException
     * @throws ClassNotFoundException
     */
    public static Route Delete(String path, String method) throws NoSuchMethodException, ClassNotFoundException {
        Route route = new Route(path, method, "DELETE");
        routes.add(route);
        return route;
    }

    /**
     * Register an OPTIONS route for the given path
     *
     * @param path
     * @param method
     * @return
     * @throws NoSuchMethodException
     * @throws ClassNotFoundException
     */
    public static Route Options(String path, String method) throws NoSuchMethodException, ClassNotFoundException {
        Route route = new Route(path, method, "OPTIONS");
        routes.add(route);
        return route;
    }

    /**
     * Clear all routes.
     */
    protected static void clearRoutes() {
        routes = new ArrayList<>();
        compiledRoutes = new ArrayList<>();
    }

    /**
     * This is used to retrieve a route for a given path
     *
     * @param path The path
     * @param httpMethod HTTP method (i.e GET)
     * @param req The request object
     * @return
     */
    public RouteExecutor lookup(String path, String httpMethod, Request req)
    {
        RouteNode traverseNode = trees.get(httpMethod);

        walk:
        for(;;) {
            String prefix = traverseNode.getPath();

            if(path.length() > prefix.length()) {
                if(path.substring(0, prefix.length()).equals(prefix)) {
                    path = path.substring(prefix.length());

                    if(!traverseNode.isWildCard()) {
                        for (RouteNode child : traverseNode.getChildren()) {

                            if (child.getPath().charAt(0) == path.charAt(0)) {
                                traverseNode = child;
                                continue walk;
                            }
                        }

                        return null;
                    }

                    traverseNode = traverseNode.getChildren().get(0);
                    switch (traverseNode.getType()) {
                        case Param:
                            int end = 0;
                            for(;end < path.length() && path.charAt(end) != '/';) {
                                end++;
                            }

                            req.addParam(traverseNode.getPath().substring(1), path.substring(0, end));

                            if(end < path.length()) {
                                if(traverseNode.getChildren().size() > 0) {
                                    path = path.substring(end);
                                    traverseNode = traverseNode.getChildren().get(0);
                                    continue walk;
                                }

                                return null;
                            }

                           if(traverseNode.getHandle() != null){
                               return traverseNode.getHandle();
                           }
                            break;
                        case CatchAll:
                            req.addParam(traverseNode.getPath().substring(2), path);
                            return traverseNode.getHandle();
                        default:
                            return null;
                    }
                }

            } else if (path.equals(prefix)){
                return traverseNode.getHandle();
            }

            //Could do something here for trailing slashes
            return null;
        }
    }

    /**
     * After the RouteBuilder as compiled the routes we set them on the router
     * here.
     * @param routes Compiled Routes.
     */
    public static void setCompiledRoutes(ArrayList<RouteExecutor> routes) {
        compiledRoutes = routes;
    }

    /**
     * Get a list of all Routes.
     * @return
     */
    public ArrayList<Route> getRoutes() {
        return Router.routes;
    }

    /**
     * Create the Trie and finalize the router.
     * @throws Exception
     */
    public void compileRoutes() throws Exception {
        trees = new HashMap<>();

        for(RouteExecutor r : Router.compiledRoutes) {
            if(trees.containsKey(r.getHttpMethod())) {
                RouteNode tree = trees.get(r.getHttpMethod());
                tree.insert(r.getPath(), r);
            } else {
                RouteNode tree = new RouteNode();
                tree.insert(r.getPath(), r);
                trees.put(r.getHttpMethod(), tree);
            }
        }
    }
}
