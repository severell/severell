package com.mitchdennett.framework.providers;

import com.mitchdennett.framework.container.Container;
import org.junit.jupiter.api.BeforeAll;

import static org.mockito.Mockito.mock;

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
