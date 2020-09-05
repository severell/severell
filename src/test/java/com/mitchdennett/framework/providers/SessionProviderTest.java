package com.mitchdennett.framework.providers;

import com.mitchdennett.framework.container.Container;
import com.mitchdennett.framework.drivers.SessionMemoryDriver;
import com.mitchdennett.framework.managers.SessionManager;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

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

        ArgumentCaptor<Object> objCaptor = ArgumentCaptor.forClass(Object.class);
        ArgumentCaptor<Object> sesssionCaptor = ArgumentCaptor.forClass(SessionMemoryDriver.class);

        verify(c, times(2)).bind(objCaptor.capture());

        assertTrue(objCaptor.getAllValues().get(0) instanceof SessionMemoryDriver);
        assertTrue(objCaptor.getAllValues().get(1) instanceof SessionManager);

        p.boot();

        verify(c).bind(any(Class.class), sesssionCaptor.capture());


    }
}
