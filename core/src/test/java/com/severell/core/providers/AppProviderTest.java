package com.severell.core.providers;

import com.github.mustachejava.DefaultMustacheFactory;
import com.severell.core.config.Config;
import com.severell.core.container.Container;
import com.severell.core.error.ErrorHandler;
import com.severell.core.http.Dispatcher;
import io.ebean.Database;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

public class AppProviderTest {

    @BeforeAll
    public static void setup() throws Exception {
        Config.setDir("src/test/resources");
        Config.loadConfig();
    }

    @AfterAll
    public static void tearDown() {
        Config.unload();
    }

    @Test
    public void appProviderTest() throws Exception {
        Container c = mock(Container.class);
        Dispatcher dispatcher = mock(Dispatcher.class);
        given(c.make(Dispatcher.class)).willReturn(dispatcher);

        given(c.make("_databaseFactory", Database.class)).willReturn(mock(Database.class));
        AppProvider prov = new AppProvider(c);
        AppProvider provSpy = Mockito.spy(prov);

        provSpy.register();

        provSpy.boot();

        ArgumentCaptor<Class> classCaptor = ArgumentCaptor.forClass(Class.class);
        ArgumentCaptor<Object> objCapture = ArgumentCaptor.forClass(Object.class);

        verify(c, times(4)).singleton(classCaptor.capture(),objCapture.capture());

        assertTrue(objCapture.getAllValues().get(0) instanceof ErrorHandler);
        assertTrue(objCapture.getAllValues().get(3) instanceof Database);

    }

    @Test
    public void appProviderTestWithConfig() throws Exception {
        Container c = mock(Container.class);
        Dispatcher dispatcher = mock(Dispatcher.class);
        given(c.make(Dispatcher.class)).willReturn(dispatcher);

        given(c.make("_databaseFactory", Database.class)).willReturn(mock(Database.class));
        AppProvider prov = new AppProvider(c);
        AppProvider provSpy = Mockito.spy(prov);

        provSpy.register();

        provSpy.boot();

        ArgumentCaptor<Class> classCaptor = ArgumentCaptor.forClass(Class.class);
        ArgumentCaptor<Object> objCapture = ArgumentCaptor.forClass(Object.class);

        verify(c, times(4)).singleton(classCaptor.capture(),objCapture.capture());

        assertTrue(objCapture.getAllValues().get(0) instanceof ErrorHandler);
    }
}
