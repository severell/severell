package com.severell.core.providers;

import com.severell.core.container.Container;
import com.severell.core.http.Router;

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
