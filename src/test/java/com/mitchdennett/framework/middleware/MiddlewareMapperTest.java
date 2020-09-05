package com.mitchdennett.framework.middleware;

import com.mitchdennett.framework.container.Container;
import com.mitchdennett.framework.http.MiddlewareChain;
import com.mitchdennett.framework.http.Request;
import com.mitchdennett.framework.http.Response;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static org.mockito.Mockito.mock;

public class MiddlewareMapperTest {

    @Test
    public void testMiddlewareMapper() throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        Method mockMethod = this.getClass().getMethod("mockMethod", Request.class, Response.class, MiddlewareChain.class);
        MiddlewareMapper middlewareMapper = new MiddlewareMapper(mockMethod, null);
        Container c = mock(Container.class);
        middlewareMapper.run(c, null, null, null);
        mockMethod.invoke(null, null, null, null);
    }

    public static void mockMethod(Request request, Response response, MiddlewareChain chain) {
        Assertions.assertTrue(true);
    }
}
