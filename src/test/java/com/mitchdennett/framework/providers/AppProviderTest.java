package com.mitchdennett.framework.providers;

import com.github.mustachejava.DefaultMustacheFactory;
import com.mitchdennett.framework.config.Config;
import com.mitchdennett.framework.container.Container;
import io.ebean.Database;
import io.ebean.DatabaseFactory;
import io.ebean.config.DatabaseConfig;
import io.ebean.datasource.DataSourceConfig;
import org.apache.commons.dbcp2.BasicDataSource;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.server.session.SessionHandler;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.util.resource.PathResource;
import org.junit.Before;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

public class AppProviderTest {

    @Test
    public void appProviderTest() throws Exception {
        Container c = mock(Container.class);
        Server server = mock(Server.class);
        given(c.make(Server.class)).willReturn(server);
        given(c.make(ServletContextHandler.class)).willReturn(new ServletContextHandler());
        AppProvider prov = new AppProvider(c);
        AppProvider provSpy = Mockito.spy(prov);
        doReturn(mock(Database.class)).when(provSpy).createDatabse(any(DatabaseConfig.class));

        provSpy.register();

        provSpy.boot();

        ArgumentCaptor<Object> classCaptor = ArgumentCaptor.forClass(Object.class);
        ArgumentCaptor<Handler> handlerCaptor = ArgumentCaptor.forClass(Handler.class);

        verify(c, times(3)).bind(classCaptor.capture());

        assertTrue(classCaptor.getAllValues().get(0) instanceof DefaultMustacheFactory);
        assertTrue(classCaptor.getAllValues().get(1) instanceof ServletContextHandler);
        assertTrue(classCaptor.getAllValues().get(2) instanceof Database);

        verify(server).setHandler(handlerCaptor.capture());

        ContextHandlerCollection collect = (ContextHandlerCollection) handlerCaptor.getValue();
        Handler[] handlers = collect.getHandlers();
        assertEquals(1, handlers.length);
    }
}
