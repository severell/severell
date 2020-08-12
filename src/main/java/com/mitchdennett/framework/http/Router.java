package com.mitchdennett.framework.http;

import com.mitchdennett.framework.container.Container;
import com.mitchdennett.main.Routes;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

import java.util.ArrayList;
import java.util.HashMap;

public class Router {

    private HashMap<HttpMethod, RouteNode> trees;
    private static ArrayList<Route> routes = new ArrayList<Route>();

    public static Route Get(String path, String method) throws NoSuchMethodException, ClassNotFoundException {
        Route route = new Route(path, method, HttpMethod.GET);
        routes.add(route);
        return route;
    }

    public static Route Post(String path, String method) throws NoSuchMethodException, ClassNotFoundException {
        Route route = new Route(path, method, HttpMethod.POST);
        routes.add(route);
        return route;
    }

    public Route lookup(String path, HttpMethod httpMethod, Request req)
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

    public ArrayList<Route> getRoutes() {
        return Router.routes;
    }

    public void compileRoutes(ServletContextHandler context, Container c) throws Exception {
        try {
            Routes.init();
        }catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }

        trees = new HashMap<>();


        for(Route r : getRoutes()) {
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
        context.addServlet(holderPwd, "/*");
    }

}
