package com.severell.core.mail;

import com.severell.core.container.Container;
import com.severell.core.managers.MailManager;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

public class MailProviderTest {

    @Test
    public void testMailProvider() {
        Container c = mock(Container.class);
        MailManager manager = mock(MailManager.class);
        MailSMTPDriver driver = mock(MailSMTPDriver.class);
        given(manager.create_driver(null)).willReturn(driver);
        given(c.make(MailManager.class)).willReturn(manager);

        MailProvider p = new MailProvider(c);
        p.register();
        p.boot();


        ArgumentCaptor<String> stringCapt = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Function> objCapture = ArgumentCaptor.forClass(Function.class);
        verify(c, times(2)).bind(stringCapt.capture(),objCapture.capture());

        assertTrue(objCapture.getAllValues().get(0).apply(c) instanceof MailSMTPDriver);
        assertTrue(objCapture.getAllValues().get(1).apply(c) instanceof MailLogDriver);

        ArgumentCaptor<Class> classCaptor = ArgumentCaptor.forClass(Class.class);
        ArgumentCaptor<Function> funcCapt = ArgumentCaptor.forClass(Function.class);

        verify(c).bind(classCaptor.capture(),funcCapt.capture());

        assertTrue(funcCapt.getValue().apply(c) instanceof MailSMTPDriver, "Expected MailSMTPDriver");
    }
}
