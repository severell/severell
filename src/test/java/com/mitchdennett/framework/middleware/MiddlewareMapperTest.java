package com.mitchdennett.framework.middleware;

import com.mitchdennett.framework.container.Container;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationTargetException;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class MiddlewareMapperTest {

    @Test
    public void testMiddlewareMapper() throws InvocationTargetException, IllegalAccessException {
        Container c = mock(Container.class);
        MiddlewareMapper middlewareMapper = new MiddlewareMapper(null, null);
        middlewareMapper.run(c, null, null);
        verify(c).invoke(null, null, null, null);
    }
}
