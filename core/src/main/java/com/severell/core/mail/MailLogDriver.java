package com.severell.core.mail;

import com.severell.core.container.Container;
import com.severell.core.exceptions.ViewException;
import org.apache.maven.shared.utils.StringUtils;

/**
 * Used to write emails to a log.
 */
public class MailLogDriver extends BaseMailDriver {

    public MailLogDriver(Container c) {
        super(c);
    }

    /**
     * Write email to console
     */
    @Override
    public void send() throws ViewException {
        System.out.println("**************************************************************");
        System.out.println(String.format("To: %s", StringUtils.join(to, ",")));
        System.out.println(String.format("From: %s", from));
        System.out.println(String.format("Subject: %s", subject));
        if(cc != null) {
            System.out.println(String.format("CC: %s",  StringUtils.join(cc, ",")));
        }

        if(bcc != null) {
            System.out.println(String.format("BCC: %s",  StringUtils.join(bcc, ",")));
        }

        if(text != null) {
            System.out.println(String.format("Plain Text Message: %s", text));
        }

        if(html != null){
            System.out.println(String.format("HTML Message: %s", html));
        }

        String htmlTemplate = getHTMLFromTemplate();

        if(htmlTemplate != null) {
            System.out.println(String.format("HTML Message: %s", getHTMLFromTemplate()));
        }
        System.out.println("**************************************************************");
    }
}