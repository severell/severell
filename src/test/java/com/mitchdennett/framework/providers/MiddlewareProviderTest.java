package com.mitchdennett.framework.providers;

import com.mitchdennett.framework.container.Container;
import com.mitchdennett.framework.http.Request;
import com.mitchdennett.framework.http.Response;
import com.mitchdennett.framework.middleware.MiddlewareMapper;
import com.mitchdennett.framework.middleware.MockMiddleware;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

public class MiddlewareProviderTest {

    private static Container c;

    @BeforeAll
    public static void setUp() {
        c = mock(Container.class);
    }


    @Test
    public void testThatMiddlewareGetsAddedToList() throws InvocationTargetException, IllegalAccessException {
        Class[] midds = new Class[]{
                MockMiddleware.class
        };
        given(c.make("_MiddlewareList", Class[].class)).willReturn(midds);

        ArgumentCaptor<String> name = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<ArrayList<MiddlewareMapper>> list = ArgumentCaptor.forClass(ArrayList.class);

        MiddlewareProvider provider = new MiddlewareProvider(c);
        provider.register();
        provider.boot();
        verify(c, times(2)).bind(name.capture(), list.capture());

        assertEquals(1, list.getAllValues().get(0).size());
        assertEquals("DefaultMiddlewareBefore", name.getAllValues().get(0));

        assertEquals(1, list.getAllValues().get(1).size());
        assertEquals("DefaultMiddlewareAfter", name.getAllValues().get(1));
    }
}
