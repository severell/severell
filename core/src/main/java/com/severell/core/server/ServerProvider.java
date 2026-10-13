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
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
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
        c.bind("ServerRedisSession", (container -> new UndertowRedisSession(c)));

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
        InMemorySessionManager sessionManager = new InMemorySessionManager("severell");
        SessionAttachmentHandler sessionAttachmentHandler = new SessionAttachmentHandler(sessionManager,
                new SeverellSessionCookieConfig(id -> sessionExists(sessionManager, id)));
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

    /**
     * A session id is only accepted if we issued it: it's live in memory, or (with Redis
     * sessions) saved in Redis, e.g. from before a restart or by another server.
     */
    private boolean sessionExists(InMemorySessionManager sessionManager, String id) {
        if (sessionManager.getSession(id) != null) {
            return true;
        }
        if ("redis".equalsIgnoreCase(Config.get("SESSION_DRIVER", "Memory"))) {
            JedisPool pool = c.make(JedisPool.class);
            try (Jedis jedis = pool.getResource()) {
                return jedis.exists(UndertowRedisSession.key(id));
            }
        }
        return false;
    }
}
