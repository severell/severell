package com.severell.core.server;

import com.severell.core.config.Config;
import com.severell.core.container.Container;
import com.severell.core.http.AppServer;
import com.severell.core.providers.ServiceProvider;
import io.undertow.Handlers;
import io.undertow.Undertow;
import io.undertow.predicate.Predicates;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.handlers.form.EagerFormParsingHandler;
import io.undertow.server.handlers.form.FormDataParser;
import io.undertow.server.handlers.form.FormParserFactory;
import io.undertow.server.handlers.form.MultiPartParserDefinition;
import io.undertow.server.handlers.resource.ClassPathResourceManager;
import io.undertow.server.handlers.resource.PathResourceManager;
import io.undertow.server.handlers.resource.ResourceManager;
import io.undertow.server.session.InMemorySessionManager;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.severell.core.session.SessionCookieSettings;
import com.severell.core.session.SessionSettings;
import org.apache.logging.log4j.LogManager;
import org.apache.commons.dbcp2.BasicDataSource;
import javax.sql.DataSource;
import io.undertow.server.session.SessionAttachmentHandler;
import org.xnio.Options;


import java.nio.file.Path;

import static io.undertow.UndertowOptions.ENABLE_HTTP2;


public class ServerProvider extends ServiceProvider {

    public ServerProvider(Container c) {
        super(c);
    }

    @Override
    public void register() {
        c.bind(UndertowTransformer.class, (container -> new UndertowTransformer(c)));
        c.bind("ServerMemorySession", (container -> new UndertowInMemorySession(c)));
        c.bind("ServerDatabaseSession", (container -> new UndertowDatabaseSession(c)));

    }

    @Override
    public void boot() throws Exception {

        AppServer appServer = c.make(AppServer.class);
        Undertow ser = null;
        if (appServer != null) {
            ser = Undertow.builder()
                    // HOST=0.0.0.0 is needed in containers so the server is reachable from outside.
                    .addHttpListener(Integer.parseInt(appServer.getPort()), Config.get("HOST", "localhost"))
                    .setServerOption(ENABLE_HTTP2, true)
                    .setWorkerOption(Options.WORKER_IO_THREADS, Integer.parseInt(Config.get("IO_THREADS", "16")))
                    .setWorkerOption(Options.WORKER_TASK_CORE_THREADS, Integer.parseInt(Config.get("CORE_THREADS", "80")))
                    .setHandler(new EagerFormParsingHandler(
                            FormParserFactory.builder()
                                    .addParsers(new MultiPartParserDefinition())
                                    .build()
                    ).setNext(getMainHandler())).build();
        }

        Undertow finalSer = ser;
        appServer.registerListener((s) -> finalSer.start());
    }

    private HttpHandler getMainHandler() {
        SessionAttachmentHandler sessionAttachmentHandler = sessionHandler();
        EagerFormParsingHandler formParsingHandler = new EagerFormParsingHandler(FormParserFactory.builder().addParsers(new MultiPartParserDefinition()).build());
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        ResourceManager manager = null;

        if(Config.isLocal()) {
            manager = new PathResourceManager(Path.of("src/main/resources"));
        } else {
            manager = new ClassPathResourceManager(loader);
        }


        return Handlers.rewrite( "path-prefix('/static')","/compiled${remaining}", loader,
                Handlers.predicate(
                        Predicates.prefix("/compiled"),
                        Handlers.resource(manager),
                        new HttpHandler() {
                            @Override
                            public void handleRequest(final HttpServerExchange exchange) throws Exception {
                                sessionAttachmentHandler.handleRequest(exchange);
                                formParsingHandler.handleRequest(exchange);
                                UndertowTransformer transformer = c.make(UndertowTransformer.class);
                                transformer.dispatch(exchange);
                            }
                        }
                ));
    }

    SessionAttachmentHandler sessionHandler() {
        if (!SessionCookieSettings.secure() && !Config.isLocal()) {
            LogManager.getLogger(ServerProvider.class).warn("SESSION_COOKIE_SECURE is off outside local development: "
                    + "session and CSRF cookies lose their __Host- protection and can be planted by other subdomains");
        }
        int lifetime = SessionSettings.lifetimeSeconds();
        if (SessionSettings.DATABASE.equals(SessionSettings.driver())) {
            DatabaseSessionManager manager = new DatabaseSessionManager(sessionDataSource(), new ObjectMapper(), lifetime,
                    SessionSettings.maxLifetimeSeconds());
            // A plain mapper of its own: an app-wide one could be configured with polymorphic
            // typing, which would let stored JSON choose classes to create.
            manager.start();
            // The manager only ever loads sessions that exist and never adopts the browser's id
            // for a new one, so the cookie's id can be passed through as is.
            return new SessionAttachmentHandler(manager, new SeverellSessionCookieConfig(id -> true));
        }
        InMemorySessionManager manager = new InMemorySessionManager("severell");
        manager.setDefaultSessionTimeout(lifetime);
        // A session id is only accepted if we issued it and it hasn't ended: it's live in memory.
        return new SessionAttachmentHandler(manager, new SeverellSessionCookieConfig(id -> manager.getSession(id) != null));
    }

    private static DataSource sessionDataSource() {
        BasicDataSource pool = new BasicDataSource();
        pool.setDriverClassName(Config.get("DB_DRIVER"));
        pool.setUrl(Config.get("DB_CONNSTRING"));
        pool.setUsername(Config.get("DB_USERNAME"));
        pool.setPassword(Config.get("DB_PASSWORD"));
        pool.setMaxTotal(Integer.parseInt(Config.get("SESSION_DB_POOL_SIZE", "8")));
        // Fail the request rather than hang every server when the database is slow or busy.
        pool.setMaxWaitMillis(5000);
        return pool;
    }
}
