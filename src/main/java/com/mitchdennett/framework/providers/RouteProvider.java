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
        c.singleton(Router.class, new Router());
    }

    @Override
    public void boot() throws Exception {
        Router r = c.make(Router.class);
        r.compileRoutes(c.make(ServletContextHandler.class), c);
    }
}
