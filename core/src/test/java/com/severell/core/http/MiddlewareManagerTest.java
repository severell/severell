package com.severell.core.http;

import com.severell.core.container.Container;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

public class MiddlewareManagerTest {

    @Test
    public void middlewareCanSeeTheMatchedRoute() throws Exception {
        RouteExecutor route = new RouteExecutor("/x", HttpMethod.GET, new ArrayList<>(), true, (r, s, c) -> {});
        Request request = mock(Request.class);
        RequestContext context = new RequestContext();
        given(request.context()).willReturn(context);

        new MiddlewareManager(route, new Container()).filterRequest(request, mock(Response.class));

        assertSame(route, context.get(RouteExecutor.CONTEXT_KEY));
    }
}
