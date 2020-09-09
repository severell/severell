package com.mitchdennett.framework.providers;

import com.mitchdennett.framework.container.Container;
import com.mitchdennett.framework.exceptions.MiddlewareException;
import com.mitchdennett.framework.middleware.SecureHeadersMiddleware;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

public class MiddlewareProviderTest {

    private static Container c;

    @BeforeAll
    public static void setUp() {
        c = mock(Container.class);
    }


//    @Test
//    public void testThatMiddlewareGetsAddedToList() throws InvocationTargetException, IllegalAccessException, MiddlewareException {
//        Class[] midds = new Class[]{
//                SecureHeadersMiddleware.class
//        };
//        given(c.make("_MiddlewareList", Class[].class)).willReturn(midds);
//
//        ArgumentCaptor<String> name = ArgumentCaptor.forClass(String.class);
//        ArgumentCaptor<ArrayList<MiddlewareMapper>> list = ArgumentCaptor.forClass(ArrayList.class);
//
//        MiddlewareProvider provider = new MiddlewareProvider(c);
//        provider.register();
//        provider.boot();
//        verify(c, times(1)).bind(name.capture(), list.capture());
//
//        assertEquals(1, list.getAllValues().get(0).size());
//        assertEquals("DefaultMiddleware", name.getAllValues().get(0));
//    }
}
