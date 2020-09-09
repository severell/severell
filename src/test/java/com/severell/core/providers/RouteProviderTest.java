package com.severell.core.providers;

import com.severell.core.container.Container;
import com.severell.core.http.Router;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class RouteProviderTest {

    @Test
    public void routeProviderTest() throws Exception {
        Container c = mock(Container.class);
        RouteProvider p = new RouteProvider(c);
        p.register();

        ArgumentCaptor<Class> classCapt = ArgumentCaptor.forClass(Class.class);
        ArgumentCaptor<Router> objCaptor = ArgumentCaptor.forClass(Router.class);
//        verify(c).singleton(classCapt.capture(), objCaptor.capture());
//        assertNotNull(objCaptor.getValue());
        Router r = mock(Router.class);

        given(c.make(Router.class)).willReturn(r);
        given(c.make(ServletContextHandler.class)).willReturn(mock(ServletContextHandler.class));

        p.boot();

        verify(r).compileRoutes();

    }
}
