package com.severell.core.mail;

import com.severell.core.config.Config;
import com.severell.core.container.Container;
import com.severell.core.exceptions.ViewException;
import com.severell.core.view.View;
import com.severell.core.view.ViewMustacheDriver;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMultipart;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.HashMap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class MailSMTPDriverTest {

    @BeforeAll
    public static void setup() throws Exception {
        if(!Config.isLoaded()) {
            Config.loadConfig();
        }
    }

    @AfterAll
    public static void tearDown() throws Exception {
       Config.unload();
    }

    @Test
    public void testSMTPPlainText() throws ViewException, MessagingException, IOException {
        Container c = mock(Container.class);
        TransportFacade transport = mock(TransportFacade.class);
        given(c.make(TransportFacade.class)).willReturn(transport);

        Mail mail = new MailSMTPDriver(c);
        mail.from("from@example.com")
                .to("to@example.com")
                .subject("subject")
                .bcc("bcc@example.com")
                .cc("cc@example.com")
                .text("plain text")
                .send();

        ArgumentCaptor<Message> messageArgumentCaptor = ArgumentCaptor.forClass(Message.class);
        verify(transport).send(messageArgumentCaptor.capture());
        Message actualMessage = messageArgumentCaptor.getValue();

        Address[] to = actualMessage.getRecipients(Message.RecipientType.TO);
        assertEquals("to@example.com", to[0].toString());

        Address[] from = actualMessage.getFrom();
        assertEquals("from@example.com", from[0].toString());

        Address[] cc = actualMessage.getRecipients(Message.RecipientType.CC);
        assertEquals("cc@example.com", cc[0].toString());

        Address[] bcc = actualMessage.getRecipients(Message.RecipientType.BCC);
        assertEquals("bcc@example.com", bcc[0].toString());

        assertEquals("subject", actualMessage.getSubject());

        MimeMultipart parts = (MimeMultipart) actualMessage.getContent();

        assertEquals("plain text", parts.getBodyPart(0).getContent());

        assertNull(actualMessage.getSession().getProperties().get("mail.smtp.socketFactory.class"));
    }

    @Test
    public void testSMTPHTMLFromTemplate() throws ViewException, MessagingException, IOException, NoSuchFieldException, IllegalAccessException {
        Container c = mock(Container.class);
        TransportFacade transport = mock(TransportFacade.class);
        given(c.make(TransportFacade.class)).willReturn(transport);
        ViewMustacheDriver driver = new ViewMustacheDriver(c);
        given(c.make(View.class)).willReturn(driver);

        MailSMTPDriver mail = new MailSMTPDriver(c);
        mail.setBaseTemplate("internaltemplates/");

        Field f1 = mail.getClass().getDeclaredField("isSSL");
        f1.setAccessible(true);
        f1.set(mail, true);

        mail.from("from@example.com")
                .to("to@example.com")
                .subject("subject")
                .bcc("bcc@example.com")
                .cc("cc@example.com")
                .template("test.mustache", new HashMap<>())
                .send();

        ArgumentCaptor<Message> messageArgumentCaptor = ArgumentCaptor.forClass(Message.class);
        verify(transport).send(messageArgumentCaptor.capture());
        Message actualMessage = messageArgumentCaptor.getValue();

        MimeMultipart parts = (MimeMultipart) actualMessage.getContent();

        assertEquals("<html><head></head><body><h1>test</h1></body></html>", parts.getBodyPart(0).getContent());
        assertEquals("javax.net.ssl.SSLSocketFactory", actualMessage.getSession().getProperties().get("mail.smtp.socketFactory.class"));
        assertEquals("456", actualMessage.getSession().getProperties().get("mail.smtp.port"));
        assertEquals("true", actualMessage.getSession().getProperties().get("mail.smtp.auth"));
        assertEquals("host", actualMessage.getSession().getProperties().get("mail.smtp.host"));
    }

    @Test
    public void testSMTPHTML() throws ViewException, MessagingException, IOException {
        Container c = mock(Container.class);
        TransportFacade transport = mock(TransportFacade.class);
        given(c.make(TransportFacade.class)).willReturn(transport);

        Mail mail = new MailSMTPDriver(c);
        mail.from("from@example.com")
                .to("to@example.com")
                .subject("subject")
                .bcc("bcc@example.com")
                .cc("cc@example.com")
                .html("<html><head></head><body><h1>test html</h1></body></html>")
                .send();

        ArgumentCaptor<Message> messageArgumentCaptor = ArgumentCaptor.forClass(Message.class);
        verify(transport).send(messageArgumentCaptor.capture());
        Message actualMessage = messageArgumentCaptor.getValue();

        Address[] to = actualMessage.getRecipients(Message.RecipientType.TO);
        assertEquals("to@example.com", to[0].toString());

        Address[] from = actualMessage.getFrom();
        assertEquals("from@example.com", from[0].toString());

        Address[] cc = actualMessage.getRecipients(Message.RecipientType.CC);
        assertEquals("cc@example.com", cc[0].toString());

        Address[] bcc = actualMessage.getRecipients(Message.RecipientType.BCC);
        assertEquals("bcc@example.com", bcc[0].toString());

        assertEquals("subject", actualMessage.getSubject());

        MimeMultipart parts = (MimeMultipart) actualMessage.getContent();

        assertEquals("<html><head></head><body><h1>test html</h1></body></html>", parts.getBodyPart(0).getContent());

        assertNull(actualMessage.getSession().getProperties().get("mail.smtp.socketFactory.class"));
    }

    @Test
    public void testSMTPHTMLAndPlainText() throws ViewException, MessagingException, IOException {
        Container c = mock(Container.class);
        TransportFacade transport = mock(TransportFacade.class);
        given(c.make(TransportFacade.class)).willReturn(transport);
        ViewMustacheDriver driver = new ViewMustacheDriver(c);
        given(c.make(View.class)).willReturn(driver);

        MailSMTPDriver mail = new MailSMTPDriver(c);
        mail.setBaseTemplate("internaltemplates/");
        mail.from("from@example.com")
            .to("to@example.com")
            .subject("subject")
            .bcc("bcc@example.com")
            .cc("cc@example.com")
            .text("plain text")
            .html("<html><head></head><body><h1>test html</h1></body></html>")
            .template("test.mustache", new HashMap<>())
            .send();

        ArgumentCaptor<Message> messageArgumentCaptor = ArgumentCaptor.forClass(Message.class);
        verify(transport).send(messageArgumentCaptor.capture());
        Message actualMessage = messageArgumentCaptor.getValue();

        MimeMultipart parts = (MimeMultipart) actualMessage.getContent();

        assertEquals("plain text", parts.getBodyPart(0).getContent());

        assertEquals("<html><head></head><body><h1>test html</h1></body></html>", parts.getBodyPart(1).getContent());

        assertEquals("<html><head></head><body><h1>test</h1></body></html>", parts.getBodyPart(2).getContent());
    }

    @Test
    public void testSendingWithNoCCAndBCC() throws ViewException, MessagingException, IOException {
        Container c = mock(Container.class);
        TransportFacade transport = mock(TransportFacade.class);
        given(c.make(TransportFacade.class)).willReturn(transport);
        ViewMustacheDriver driver = new ViewMustacheDriver(c);
        given(c.make(View.class)).willReturn(driver);

        MailSMTPDriver mail = new MailSMTPDriver(c);
        mail.setBaseTemplate("internaltemplates/");
        mail.from("from@example.com")
                .to("to@example.com")
                .subject("subject")
                .text("plain text")
                .html("<html><head></head><body><h1>test html</h1></body></html>")
                .template("test.mustache", new HashMap<>())
                .send();

        ArgumentCaptor<Message> messageArgumentCaptor = ArgumentCaptor.forClass(Message.class);
        verify(transport).send(messageArgumentCaptor.capture());
    }
}
