package com.severell.core.providers;

import com.severell.core.container.Container;
import com.severell.core.http.Router;

/**
 * The RouteProvider registers and setups up all the functionality
 * regarding routes.
 */
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
        r.compileRoutes();
    }
}
