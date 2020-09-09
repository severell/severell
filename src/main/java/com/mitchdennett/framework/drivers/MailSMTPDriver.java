package com.mitchdennett.framework.drivers;

import com.mitchdennett.framework.config.Config;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;

public class MailSMTPDriver extends BaseMailDriver {

    @Override
    public void send() {
        try {
            Properties props = new Properties();
            props.put("mail.smtp.auth", String.valueOf(Config.isSet("MAIL_USERNAME") && Config.isSet("MAIL_PASSWORD")));
            props.put("mail.smtp.starttls.enable", Config.get("MAIL_SMTP_STARTTLS", "false"));
            props.put("mail.smtp.host", Config.get("MAIL_HOST"));
            props.put("mail.smtp.port", Config.get("MAIL_PORT"));

            // SSL Factory
            if(Boolean.valueOf(Config.get("MAIL_SMTP_SSL"))) {
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

            // Set Subject: header field
            message.setSubject(subject);

            // Now set the actual message
            message.setText(text);

            // Send message
            Transport.send(message);
        } catch (MessagingException mex) {
            mex.printStackTrace();
        }
    }
}
