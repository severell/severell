package com.severell.core.mail;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.severell.core.container.Container;
import com.severell.core.exceptions.ViewException;
import com.severell.core.view.View;
import com.severell.core.view.ViewMustacheDriver;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.HashMap;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

public class MailLogDriverTest {

    private static ByteArrayOutputStream outContent;
    private static ByteArrayOutputStream errContent;
    private static final PrintStream originalOut = System.out;
    private static final PrintStream originalErr = System.err;

    @BeforeEach
    public void setUpStreams() {
        outContent = new ByteArrayOutputStream();
        errContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));
        System.setErr(new PrintStream(errContent));
    }

    @AfterEach
    public void restoreStreams() {
        System.setOut(originalOut);
        System.setErr(originalErr);
    }

    @Test
    public void logDriverPlainTextTest() throws ViewException {
        Container c = mock(Container.class);
        Mail mail = new MailLogDriver(c);
        mail.from("test@example.com")
                .to("to@example.com")
                .cc("cc@example.com")
                .bcc("bcc@example.com")
                .subject("subject")
                .text("hello").send();
        String expectedString = "**************************************************************\n" +
                "To: to@example.com\n" +
                "From: test@example.com\n" +
                "Subject: subject\n" +
                "CC: cc@example.com\n" +
                "BCC: bcc@example.com\n" +
                "Plain Text Message: hello\n" +
                "**************************************************************\n";
        assertEquals(expectedString, outContent.toString());
    }

    @Test
    public void logDriverHTMLTest() throws ViewException {
        Container c = mock(Container.class);
        ViewMustacheDriver driver = new ViewMustacheDriver(c);
        given(c.make(View.class)).willReturn(driver);
        MailLogDriver mail = new MailLogDriver(c);
        mail.setBaseTemplate("internaltemplates/");
        mail.from("test@example.com")
                .to("to@example.com")
                .subject("subject")
                .cc("cc@example.com")
                .bcc("bcc@example.com")
                .template("test.mustache", new HashMap<>()).send();
        String expectedString = "**************************************************************\n" +
                "To: to@example.com\n" +
                "From: test@example.com\n" +
                "Subject: subject\n" +
                "CC: cc@example.com\n" +
                "BCC: bcc@example.com\n" +
                "HTML Message: <html><head></head><body><h1>test</h1></body></html>\n" +
                "**************************************************************\n";
        assertEquals(expectedString, outContent.toString());
    }
}
