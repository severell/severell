package com.mitchdennett.framework.providers;

import com.mitchdennett.framework.container.Container;
import com.mitchdennett.framework.http.Router;
import org.eclipse.jetty.servlet.ServletContextHandler;

public class RouteProvider extends ServiceProvider{

    public RouteProvider(Container c) {
        super(c);
    }

    @Override
    public void register() {

    }

    @Override
    public void boot() {
        Router r = new Router();
        try {
            r.compileRoutes(c.make(ServletContextHandler.class), c);
        }catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
}
