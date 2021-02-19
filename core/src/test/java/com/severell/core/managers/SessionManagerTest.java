package com.severell.core.managers;

import com.severell.core.container.Container;
import com.severell.core.session.Session;
import com.severell.core.session.SessionMemoryDriver;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

public class SessionManagerTest {

    @Test
    public void managerTest() {
        Container c = mock(Container.class);
        given(c.make("SessionMemoryDriver", Session.class)).willReturn(mock(SessionMemoryDriver.class));
        SessionManager manager = new SessionManager(c);

        Object obj = manager.create_driver("Memory");
        assertTrue(obj instanceof SessionMemoryDriver);
    }

    @Test
    public void driverNotFoundTest() {
        Container c = mock(Container.class);
        given(c.make("SessionMemoryDriver", Session.class)).willReturn(mock(SessionMemoryDriver.class));
        SessionManager manager = new SessionManager(c);
        Object obj = manager.create_driver("MemorySomething");
        assertNull(obj);
    }
}
