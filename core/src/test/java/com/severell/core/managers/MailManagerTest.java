package com.severell.core.managers;

import com.severell.core.container.Container;
import com.severell.core.mail.Mail;
import com.severell.core.mail.MailSMTPDriver;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

public class MailManagerTest {

    @Test
    public void testCreateDriver() {
        Container c = mock(Container.class);
        given(c.make("MailSMTPDriver", Mail.class)).willReturn(mock(MailSMTPDriver.class));
        MailManager m = new MailManager(c);

        Mail mail = m.create_driver("SMTP");
        assertTrue(mail instanceof MailSMTPDriver, "Mail should be instance of MailSMTPDriver");
    }
}
