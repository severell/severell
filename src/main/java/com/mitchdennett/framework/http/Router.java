package com.mitchdennett.framework.http;

import com.mitchdennett.framework.container.Container;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

import javax.servlet.MultipartConfigElement;
import java.util.ArrayList;
import java.util.HashMap;

public class Router {

    private HashMap<String, RouteNode> trees;
    private static ArrayList<Route> routes = new ArrayList<Route>();
    private static ArrayList<RouteExecutor> compiledRoutes = new ArrayList<RouteExecutor>();

    public static Route Get(String path, String method) throws NoSuchMethodException, ClassNotFoundException {
        Route route = new Route(path, method, "GET");
        Route headRoute = new Route(path, method, "HEAD");
        routes.add(route);
        routes.add(headRoute);
        return route;
    }

    public static Route Post(String path, String method) throws NoSuchMethodException, ClassNotFoundException {
        Route route = new Route(path, method, "POST");
        routes.add(route);
        return route;
    }

    public static Route Put(String path, String method) throws NoSuchMethodException, ClassNotFoundException {
        Route route = new Route(path, method, "PUT");
        routes.add(route);
        return route;
    }

    public static Route Patch(String path, String method) throws NoSuchMethodException, ClassNotFoundException {
        Route route = new Route(path, method, "PATCH");
        routes.add(route);
        return route;
    }

    public static Route Delete(String path, String method) throws NoSuchMethodException, ClassNotFoundException {
        Route route = new Route(path, method, "DELETE");
        routes.add(route);
        return route;
    }

    public static Route Options(String path, String method) throws NoSuchMethodException, ClassNotFoundException {
        Route route = new Route(path, method, "OPTIONS");
        routes.add(route);
        return route;
    }

    protected static void clearRoutes() {
        routes = new ArrayList<>();
        compiledRoutes = new ArrayList<>();
    }

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

    public static void setCompiledRoutes(ArrayList<RouteExecutor> routes) {
        compiledRoutes = routes;
    }

    public ArrayList<Route> getRoutes() {
        return Router.routes;
    }

    public void compileRoutes(ServletContextHandler context, Container c) throws Exception {
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

        BasicServlet defaultServlet = new BasicServlet(c, this);
        ServletHolder holderPwd = new ServletHolder("default", defaultServlet);
        holderPwd.getRegistration().setMultipartConfig(new MultipartConfigElement("/tmp"));
        context.addServlet(holderPwd, "/*");
    }

}
