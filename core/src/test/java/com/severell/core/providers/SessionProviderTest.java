package com.severell.core.providers;

import com.severell.core.container.Container;
import com.severell.core.drivers.SessionMemoryDriver;
import com.severell.core.managers.SessionManager;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

public class SessionProviderTest {

    @Test
    public void sessionProviderTest() {
        Container c = mock(Container.class);
        SessionManager manager = mock(SessionManager.class);
        SessionMemoryDriver driver = mock(SessionMemoryDriver.class);
        given(manager.create_driver(anyString())).willReturn(driver);
        given(c.make(SessionManager.class)).willReturn(manager);
        SessionProvider p = new SessionProvider(c);

        p.register();


        ArgumentCaptor<Function<Container, Object>> objCaptor = ArgumentCaptor.forClass(Function.class);
        ArgumentCaptor<Object> sesssionCaptor = ArgumentCaptor.forClass(Object.class);
        ArgumentCaptor<Function<Container, Object>> sessCaptor = ArgumentCaptor.forClass(Function.class);

        verify(c).bind(any(String.class), objCaptor.capture());
        verify(c).singleton(any(Class.class), sesssionCaptor.capture());

        assertTrue(sesssionCaptor.getValue() instanceof SessionManager);
        assertTrue(objCaptor.getValue().apply(null) instanceof SessionMemoryDriver);

        p.boot();

        verify(c).bind(any(Class.class), sessCaptor.capture());


    }
}
