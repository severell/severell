package com.severell.core.mail;

import com.severell.core.config.Config;
import com.severell.core.container.Container;
import com.severell.core.exceptions.ViewException;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.util.Properties;

public class MailSMTPDriver extends BaseMailDriver {

    private final TransportFacade transport;
    private boolean isSSL;

    public MailSMTPDriver(Container c) {
        super(c);
        transport = c.make(TransportFacade.class);
        isSSL = Boolean.valueOf(Config.get("MAIL_SMTP_SSL"));
    }

    @Override
    public void send() {
        try {
            Properties props = new Properties();
            props.put("mail.smtp.auth", String.valueOf(Config.isSet("MAIL_USERNAME") && Config.isSet("MAIL_PASSWORD")));
            props.put("mail.smtp.starttls.enable", Config.get("MAIL_SMTP_STARTTLS", "false"));
            props.put("mail.smtp.host", Config.get("MAIL_HOST"));
            props.put("mail.smtp.port", Config.get("MAIL_PORT"));

            // SSL Factory
            if(isSSL) {
                props.put("mail.smtp.socketFactory.class",
                        "javax.net.ssl.SSLSocketFactory");
            }

            // Get the Session object
            javax.mail.Session session = javax.mail.Session.getInstance(props,
                new javax.mail.Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(Config.get("MAIL_USERNAME"), Config.get("MAIL_PASSWORD"));
                    }
                });

            // Create a default MimeMessage object.
            MimeMessage message = new MimeMessage(session);

            // Set From: header field of the header.
            message.setFrom(new InternetAddress(from));

            // Set To: header field of the header.
            for(String toAddress : to) {
                message.addRecipient(Message.RecipientType.TO, new InternetAddress(toAddress));
            }

            for(String toAddress : cc) {
                message.addRecipient(Message.RecipientType.CC, new InternetAddress(toAddress));
            }

            for(String toAddress : bcc) {
                message.addRecipient(Message.RecipientType.BCC, new InternetAddress(toAddress));
            }

            // Set Subject: header field
            message.setSubject(subject);

            final Multipart mp = new MimeMultipart("alternative");

            if(text != null) {
                final MimeBodyPart textPart = new MimeBodyPart();
                textPart.setContent(text, "text/plain");
                mp.addBodyPart(textPart);
            }

            if(template != null) {
                // HTML version
                final MimeBodyPart htmlPart = new MimeBodyPart();
                htmlPart.setContent(this.getHTML(), "text/html");
                mp.addBodyPart(htmlPart);
            }

            // Now set the actual message
            message.setContent(mp);

            // Send message
            transport.send(message);
        } catch (MessagingException | ViewException mex) {
            mex.printStackTrace();
        }
    }
}
