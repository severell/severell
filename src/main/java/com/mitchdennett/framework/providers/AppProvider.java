package com.mitchdennett.framework.providers;

import com.github.mustachejava.DefaultMustacheFactory;
import com.mitchdennett.framework.config.Config;
import com.mitchdennett.framework.container.Container;
import io.ebean.Database;
import io.ebean.DatabaseFactory;
import io.ebean.config.DatabaseConfig;
import io.ebean.datasource.DataSourceConfig;
import org.apache.commons.dbcp2.BasicDataSource;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.server.session.SessionHandler;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.util.resource.PathResource;

import java.nio.file.Paths;

public class AppProvider extends ServiceProvider{

    public AppProvider(Container c) {
        super(c);
    }

    @Override
    public void register() {
        c.bind(new DefaultMustacheFactory());
        c.bind(new ServletContextHandler(ServletContextHandler.SESSIONS));
    }

    @Override
    public void boot() {
        Server server = c.make(Server.class);
        //Enable parsing of jndi-related parts of web.xml and jetty-env.xml
        org.eclipse.jetty.webapp.Configuration.ClassList classlist = org.eclipse.jetty.webapp.Configuration.ClassList.setServerDefault(server);
        classlist.addAfter("org.eclipse.jetty.webapp.FragmentConfiguration", "org.eclipse.jetty.plus.webapp.EnvConfiguration", "org.eclipse.jetty.plus.webapp.PlusConfiguration");


        ServletContextHandler context = c.make(ServletContextHandler.class);
        context.setContextPath("/");

        SessionHandler sessionHandler = new SessionHandler();
        context.setSessionHandler(sessionHandler);

        ResourceHandler rh0 = new ResourceHandler();
        rh0.setDirectoriesListed(false);

        ContextHandler context0 = new ContextHandler();
        context0.setContextPath("/static");
        context0.setBaseResource(new PathResource(Paths.get("src/main/webapp/WEB-INF/compiled")));
        context0.setHandler(rh0);

        ContextHandlerCollection contexts = new ContextHandlerCollection(
                context0, context
        );

        DataSourceConfig dataSourceConfig = new DataSourceConfig();

        dataSourceConfig.setUsername(Config.get("DB_USERNAME"));
        dataSourceConfig.setPassword(Config.get("DB_PASSWORD"));
        dataSourceConfig.setDriver(Config.get("DB_DRIVER"));
        dataSourceConfig.setUrl(Config.get("DB_CONNSTRING"));
        DatabaseConfig config = new DatabaseConfig();
        config.setDataSourceConfig(dataSourceConfig);

        Database database = createDatabse(config);

        c.bind(database);

        server.setHandler(contexts);


    }

    protected Database createDatabse(DatabaseConfig config) {
        return DatabaseFactory.create(config);
    }
}
