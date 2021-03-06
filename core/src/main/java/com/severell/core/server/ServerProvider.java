package com.severell.core.server;

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
import io.undertow.server.session.InMemorySessionManager;
import io.undertow.server.session.SessionAttachmentHandler;
import io.undertow.server.session.SessionCookieConfig;


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
                    .addHttpListener(Integer.parseInt(appServer.getPort()), "localhost")
                    .setServerOption(ENABLE_HTTP2, true)
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
        SessionAttachmentHandler sessionAttachmentHandler = new SessionAttachmentHandler(new InMemorySessionManager("severell"), new SessionCookieConfig());
        ClassLoader loader = Thread.currentThread().getContextClassLoader();


        return Handlers.rewrite( "path-prefix('/static')","/compiled${remaining}", loader,
                Handlers.predicate(
                        Predicates.prefix("/compiled"),
                        Handlers.resource(new ClassPathResourceManager(loader)),
                        new HttpHandler() {
                            @Override
                            public void handleRequest(final HttpServerExchange exchange) throws Exception {
                                sessionAttachmentHandler.handleRequest(exchange);
                                UndertowTransformer transformer = c.make(UndertowTransformer.class);
                                transformer.dispatch(exchange);
                            }
                        }
                ));
    }
}
