package com.mitchdennett.framework.providers;

import com.github.mustachejava.DefaultMustacheFactory;
import com.mitchdennett.framework.container.Container;
import com.mitchdennett.framework.error.ErrorHandler;
import com.mitchdennett.framework.http.Dispatcher;
import com.mitchdennett.framework.http.Router;

import java.io.IOException;

public class AppProvider extends ServiceProvider{

    public AppProvider(Container c) {
        super(c);
    }

    @Override
    public void register() {
        c.singleton(DefaultMustacheFactory.class, new DefaultMustacheFactory());
        c.singleton(ErrorHandler.class, new ErrorHandler(c));
        c.singleton(Router.class, new Router());
        c.singleton(Dispatcher.class, new Dispatcher(c));
    }

    @Override
    public void boot() throws IOException {


    }
}
