package com.mitchdennett.framework.providers;

import com.github.mustachejava.DefaultMustacheFactory;
import com.mitchdennett.framework.config.Config;
import com.mitchdennett.framework.container.Container;
import com.mitchdennett.framework.error.ErrorHandler;
import com.mitchdennett.framework.http.Router;
import io.ebean.Database;
import io.ebean.DatabaseFactory;
import io.ebean.config.DatabaseConfig;
import io.ebean.datasource.DataSourceConfig;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.server.session.SessionHandler;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.util.resource.Resource;

import java.io.IOException;
import java.net.URL;

public class AppProvider extends ServiceProvider{

    public AppProvider(Container c) {
        super(c);
    }

    @Override
    public void register() {
        Router.setContainer(c);
        c.bind(new DefaultMustacheFactory());
        c.bind(new ErrorHandler(c));
        c.bind(new ServletContextHandler(ServletContextHandler.SESSIONS));
    }

    @Override
    public void boot() throws IOException {
        Server server = c.make(Server.class);

        ServletContextHandler context = c.make(ServletContextHandler.class);
        context.setContextPath("/");

        SessionHandler sessionHandler = new SessionHandler();
        context.setSessionHandler(sessionHandler);

        ContextHandlerCollection contexts = new ContextHandlerCollection();

        ResourceHandler rh0 = new ResourceHandler();
        rh0.setDirectoriesListed(false);

        ContextHandler context0 = new ContextHandler();
        context0.setContextPath("/static");

        URL baseUrl  = AppProvider.class.getResource( "/compiled" );
        if(baseUrl != null) {
            String basePath = baseUrl.toExternalForm();
            context0.setBaseResource(Resource.newResource(basePath));
            context0.setHandler(rh0);
            contexts.addHandler(context0);
        }

        contexts.addHandler(context);
        DataSourceConfig dataSourceConfig = new DataSourceConfig();

        dataSourceConfig.setUsername(Config.get("DB_USERNAME"));
        dataSourceConfig.setPassword(Config.get("DB_PASSWORD"));
        dataSourceConfig.setDriver(Config.get("DB_DRIVER"));
        dataSourceConfig.setUrl(Config.get("DB_CONNSTRING"));
        DatabaseConfig config = new DatabaseConfig();

        if(Config.get("MODELPACKAGE") != null) {
            config.addPackage(Config.get("MODELPACKAGE"));
        }

        config.setDataSourceConfig(dataSourceConfig);

        Database database = createDatabse(config);

        c.bind(database);

        server.setHandler(contexts);


    }

    protected Database createDatabse(DatabaseConfig config) {
        return DatabaseFactory.create(config);
    }
}
