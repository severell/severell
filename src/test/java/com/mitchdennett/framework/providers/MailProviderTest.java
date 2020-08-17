package com.mitchdennett.framework.providers;

import com.mitchdennett.framework.container.Container;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.mock;

public class MailProviderTest {

    @Test
    public void testMailProvider() {
        Container c = mock(Container.class);
        MailProvider p = new MailProvider(c);
        p.register();
        p.boot();
    }
}
