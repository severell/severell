package com.severell.core.jetty;

import com.severell.core.container.Container;
import com.severell.core.http.AppServer;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

public class JettyProviderTest {

    private static Container c;
    private static AppServer server;
    private static ServletContextHandler handler;
    private static BasicServlet servlet;

    @BeforeAll
    public static void setup() {
        c = mock(Container.class);
        server = mock(AppServer.class);
        servlet = new BasicServlet(c);
        handler = Mockito.spy(ServletContextHandler.class);

        given(c.make(ServletContextHandler.class)).willReturn(handler);
        given(server.getPort()).willReturn("8009");
        given(c.make(AppServer.class)).willReturn(server);
        given(c.make(BasicServlet.class)).willReturn(servlet);
    }

    @Test
    public void testJettyProviderRegisterClass() throws Exception {
        JettyProvider prov = new JettyProvider(c);
        JettyProvider provSpy = Mockito.spy(prov);

        provSpy.register();

        ArgumentCaptor<Class> classCaptor = ArgumentCaptor.forClass(Class.class);
        ArgumentCaptor<Object> objCapture = ArgumentCaptor.forClass(Object.class);

        verify(c, times(2)).singleton(classCaptor.capture(),objCapture.capture());

        assertTrue(objCapture.getAllValues().get(0) instanceof BasicServlet);
        assertTrue(objCapture.getAllValues().get(1) instanceof ServletContextHandler);
    }

    @Test
    public void testJettyProviderBootMethod() throws Exception {
        JettyProvider prov = new JettyProvider(c);
        JettyProvider provSpy = Mockito.spy(prov);

        provSpy.register();
        provSpy.boot();


        ArgumentCaptor<String> pathCapture = ArgumentCaptor.forClass(String.class);
        verify(handler).setContextPath(pathCapture.capture());
        assertEquals("/", pathCapture.getValue());


        ArgumentCaptor<ServletHolder> servletArgumentCaptor = ArgumentCaptor.forClass(ServletHolder.class);
        ArgumentCaptor<String> pathSpecCapture = ArgumentCaptor.forClass(String.class);

        verify(handler).addServlet(servletArgumentCaptor.capture(), pathSpecCapture.capture());
        assertEquals("/*", pathSpecCapture.getValue());
        assertEquals(new ServletHolder("default", servlet), servletArgumentCaptor.getValue());
    }
}
