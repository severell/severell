package com.mitchdennett.framework.providers;

import com.github.mustachejava.DefaultMustacheFactory;
import com.mitchdennett.framework.container.Container;
import com.mitchdennett.framework.error.ErrorHandler;
import io.ebean.Database;
import io.ebean.config.DatabaseConfig;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

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

        verify(c, times(4)).bind(classCaptor.capture());

        assertTrue(classCaptor.getAllValues().get(0) instanceof DefaultMustacheFactory);
        assertTrue(classCaptor.getAllValues().get(1) instanceof ErrorHandler);
        assertTrue(classCaptor.getAllValues().get(2) instanceof ServletContextHandler);
        assertTrue(classCaptor.getAllValues().get(3) instanceof Database);

        verify(server).setHandler(handlerCaptor.capture());

        ContextHandlerCollection collect = (ContextHandlerCollection) handlerCaptor.getValue();
        Handler[] handlers = collect.getHandlers();
        assertEquals(1, handlers.length);
    }
}
