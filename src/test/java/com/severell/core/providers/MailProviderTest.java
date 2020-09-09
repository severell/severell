package com.severell.core.providers;

import com.severell.core.container.Container;
import com.severell.core.drivers.MailSMTPDriver;
import com.severell.core.managers.MailManager;
import org.junit.jupiter.api.Test;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

public class MailProviderTest {

    @Test
    public void testMailProvider() {
        Container c = mock(Container.class);
        MailManager manager = mock(MailManager.class);
        MailSMTPDriver driver = mock(MailSMTPDriver.class);
        given(manager.create_driver(anyString())).willReturn(driver);
        given(c.make(MailManager.class)).willReturn(manager);
        MailProvider p = new MailProvider(c);
        p.register();
        p.boot();
        //TODO need to actually test something here
    }
}
