package com.severell.core.mail;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Transport;

public class TransportFacade {

    public void send(Message message) throws MessagingException {
        Transport.send(message);
    }

}
