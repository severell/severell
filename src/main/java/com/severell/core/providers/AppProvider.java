package com.severell.core.providers;

import com.github.mustachejava.DefaultMustacheFactory;
import com.severell.core.container.Container;
import com.severell.core.error.ErrorHandler;
import com.severell.core.http.Dispatcher;
import com.severell.core.http.Router;

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
