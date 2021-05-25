package com.severell.core.http;

import com.severell.core.container.Container;
import org.junit.jupiter.api.Test;

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
//            resp.getContentType();
        }));

        list.add(new MiddlewareExecutor((req, resp, c, chain) -> {
            assertNotNull(req);
            chain.next();
//            resp.getLocale();
        }));

        MiddlewareChain chain = new MiddlewareChain();
        chain.setMiddleware(list);
        chain.setTarget(new RouteExecutor("/", HttpMethod.GET, (req, resp, c) -> {
            assertNotNull(req);
            req.path();
        }));
        chain.execute(container, request, response);
        verify(request).path();
//        verify(response).();
//        verify(response).getLocale();
    }

    @Test
    public void testMiddlwareChainNoMiddleware() throws Exception {
        Container container = mock(Container.class);
        Request request = mock(Request.class);
        Response response = mock(Response.class);

        ArrayList<MiddlewareExecutor> list = new ArrayList<>();

        MiddlewareChain chain = new MiddlewareChain();
        chain.setMiddleware(list);
        RouteExecutor ex = new RouteExecutor("/", HttpMethod.GET, new ArrayList<>(), (req, resp, c) -> {
            assertNotNull(req);
            req.path();
        });
        chain.setTarget(ex);
        chain.execute(container, request, response);
        verify(request).path();

        assertEquals(0, ex.getMiddleware().size());
    }
}
