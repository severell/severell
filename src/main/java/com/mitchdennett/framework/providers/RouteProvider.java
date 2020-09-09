package com.mitchdennett.framework.providers;

import com.mitchdennett.framework.container.Container;
import com.mitchdennett.framework.http.Router;

public class RouteProvider extends ServiceProvider{

    public RouteProvider(Container c) {
        super(c);
    }

    @Override
    public void register() {
    }

    @Override
    public void boot() throws Exception {
        Router r = c.make(Router.class);
        r.compileRoutes();
    }
}
