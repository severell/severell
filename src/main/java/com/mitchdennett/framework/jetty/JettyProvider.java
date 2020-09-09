package com.mitchdennett.framework.jetty;

import com.mitchdennett.framework.config.Config;
import com.mitchdennett.framework.container.Container;
import com.mitchdennett.framework.http.AppServer;
import com.mitchdennett.framework.providers.AppProvider;
import com.mitchdennett.framework.providers.ServiceProvider;
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
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.resource.Resource;

import javax.servlet.MultipartConfigElement;
import java.net.URL;

public class JettyProvider extends ServiceProvider {

    public JettyProvider(Container c) {
        super(c);
    }

    @Override
    public void register() {
        this.c.singleton(BasicServlet.class, new BasicServlet(this.c));
        this.c.singleton(ServletContextHandler.class, new ServletContextHandler(ServletContextHandler.SESSIONS));

    }

    @Override
    public void boot() throws Exception {
        AppServer appServer = c.make(AppServer.class);
        if(appServer != null) {
            Server server = new Server(Integer.parseInt(appServer.getPort()));

            appServer.registerListener((val) -> {
                try {
                    server.start();
                    server.join();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });


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

            c.singleton(Database.class, database);


            server.setHandler(contexts);

            BasicServlet defaultServlet = c.make(BasicServlet.class);
            ServletHolder holderPwd = new ServletHolder("default", defaultServlet);
            holderPwd.getRegistration().setMultipartConfig(new MultipartConfigElement("/tmp"));
            context.addServlet(holderPwd, "/*");
        }
    }

    protected Database createDatabse(DatabaseConfig config) {
        return DatabaseFactory.create(config);
    }
}
