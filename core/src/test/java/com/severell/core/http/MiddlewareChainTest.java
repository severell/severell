package com.severell.core.http;

import com.severell.core.container.Container;
import com.severell.core.middleware.Middleware;
import org.junit.jupiter.api.Test;

import java.sql.ResultSet;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class MiddlewareChainTest {

    @Test
    public void testMiddlwareChain() throws Exception {
        Container container = mock(Container.class);
        Request request = mock(Request.class);
        Response response = mock(Response.class);

        ArrayList<MiddlewareExecutor> list = new ArrayList<>();
        list.add(new MiddlewareExecutor((req, resp, c, chain) -> {
            assertNotNull(req);
            chain.next();
            resp.getContentType();
        }));

        list.add(new MiddlewareExecutor((req, resp, c, chain) -> {
            assertNotNull(req);
            chain.next();
            resp.getLocale();
        }));

        MiddlewareChain chain = new MiddlewareChain();
        chain.setMiddleware(list);
        chain.setTarget(new RouteExecutor("/", "GET", (req, resp, c) -> {
            assertNotNull(req);
            req.getRequest();
        }));
        chain.execute(container, request, response);
        verify(request).getRequest();
        verify(response).getContentType();
        verify(response).getLocale();
    }

    @Test
    public void testMiddlwareChainNoMiddleware() throws Exception {
        Container container = mock(Container.class);
        Request request = mock(Request.class);
        Response response = mock(Response.class);

        ArrayList<MiddlewareExecutor> list = new ArrayList<>();

        MiddlewareChain chain = new MiddlewareChain();
        chain.setMiddleware(list);
        RouteExecutor ex = new RouteExecutor("/", "GET", new ArrayList<>(), (req, resp, c) -> {
            assertNotNull(req);
            req.getRequest();
        });
        chain.setTarget(ex);
        chain.execute(container, request, response);
        verify(request).getRequest();

        assertEquals(0, ex.getMiddleware().size());
    }
}
