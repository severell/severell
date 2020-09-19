package com.severell.core.mail;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Transport;

public class TransportFacade {

    /**
     * Used to send a Javax Mail Message
     * @param message Email Message
     * @throws MessagingException
     */
    public void send(Message message) throws MessagingException {
        Transport.send(message);
    }

}
