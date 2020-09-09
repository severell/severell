package com.mitchdennett.framework.providers;

import com.mitchdennett.framework.container.Container;
import com.mitchdennett.framework.drivers.MailSMTPDriver;
import com.mitchdennett.framework.managers.MailManager;
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
