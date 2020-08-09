package com.mitchdennett.framework.providers;

import com.mitchdennett.framework.container.Container;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;

public class AppProvider extends ServiceProvider{

    public AppProvider(Container c) {
        super(c);
    };

    @Override
    public void register() {
        c.bind(new ServletContextHandler(ServletContextHandler.SESSIONS));
    }

    @Override
    public void boot() {
        ServletContextHandler context = c.make(ServletContextHandler.class);
        context.setContextPath("/");
        c.make(Server.class).setHandler(context);
    }
}
