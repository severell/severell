package com.severell.core.providers;

import com.github.mustachejava.DefaultMustacheFactory;
import com.severell.core.container.Container;
import com.severell.core.error.ErrorHandler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertTrue;
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

        provSpy.register();

        provSpy.boot();

        ArgumentCaptor<Class> classCaptor = ArgumentCaptor.forClass(Class.class);
        ArgumentCaptor<Object> objCapture = ArgumentCaptor.forClass(Object.class);
//        ArgumentCaptor<Handler> handlerCaptor = ArgumentCaptor.forClass(Handler.class);

        verify(c, times(4)).singleton(classCaptor.capture(),objCapture.capture());

        assertTrue(objCapture.getAllValues().get(0) instanceof DefaultMustacheFactory);
        assertTrue(objCapture.getAllValues().get(1) instanceof ErrorHandler);
//        assertTrue(objCapture.getAllValues().get(2) instanceof ServletContextHandler);
//        assertTrue(objCapture.getAllValues().get(3) instanceof Database);

//        verify(server).setHandler(handlerCaptor.capture());

//        ContextHandlerCollection collect = (ContextHandlerCollection) handlerCaptor.getValue();
//        Handler[] handlers = collect.getHandlers();
//        assertEquals(1, handlers.length);
    }
}
