package com.severell.core.providers;

import com.github.mustachejava.DefaultMustacheFactory;
import com.severell.core.container.Container;
import com.severell.core.error.ErrorHandler;
import io.ebean.Database;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

public class AppProviderTest {

    @Test
    public void appProviderTest() throws Exception {
        Container c = mock(Container.class);
        given(c.make("_databaseFactory", Database.class)).willReturn(mock(Database.class));
        AppProvider prov = new AppProvider(c);
        AppProvider provSpy = Mockito.spy(prov);

        provSpy.register();

        provSpy.boot();

        ArgumentCaptor<Class> classCaptor = ArgumentCaptor.forClass(Class.class);
        ArgumentCaptor<Object> objCapture = ArgumentCaptor.forClass(Object.class);

        verify(c, times(5)).singleton(classCaptor.capture(),objCapture.capture());

        assertTrue(objCapture.getAllValues().get(0) instanceof DefaultMustacheFactory);
        assertTrue(objCapture.getAllValues().get(1) instanceof ErrorHandler);
        assertTrue(objCapture.getAllValues().get(4) instanceof Database);

    }
}
