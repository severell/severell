package com.severell.core.view;

import com.severell.core.container.Container;
import com.severell.core.http.Router;
import com.severell.core.providers.RouteProvider;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

public class ViewProviderTest {

    @Test
    public void testViewProvider() throws Exception {
        Container c = mock(Container.class);
        ViewMustacheDriver driver = new ViewMustacheDriver(c);
        given(c.make("ViewMustacheDriver", View.class)).willReturn(driver);
        ViewManager manager = new ViewManager(c);
        given(c.make(ViewManager.class)).willReturn(manager);

        ViewProvider p = new ViewProvider(c);
        p.register();

        ArgumentCaptor<Class> classCapt = ArgumentCaptor.forClass(Class.class);
        ArgumentCaptor<ViewManager> objCaptor = ArgumentCaptor.forClass(ViewManager.class);
        verify(c).singleton(classCapt.capture(), objCaptor.capture());
        assertEquals(ViewManager.class, classCapt.getValue());
        assertTrue(objCaptor.getValue() instanceof ViewManager);

        ArgumentCaptor<String> cCapt = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Function> fCapt = ArgumentCaptor.forClass(Function.class);
        verify(c, times(1)).bind(cCapt.capture(), fCapt.capture());
        assertEquals("ViewMustacheDriver", cCapt.getAllValues().get(0));
        assertTrue(fCapt.getAllValues().get(0).apply(c) instanceof ViewMustacheDriver);

        p.boot();

        classCapt = ArgumentCaptor.forClass(Class.class);
        ArgumentCaptor<View> viewCaptor = ArgumentCaptor.forClass(View.class);
        verify(c,times(2)).singleton(classCapt.capture(), viewCaptor.capture());
        assertTrue(viewCaptor.getAllValues().get(1) instanceof ViewMustacheDriver);



    }
}
