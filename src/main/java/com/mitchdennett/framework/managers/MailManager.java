package com.mitchdennett.framework.managers;

import com.mitchdennett.framework.container.Container;
import com.mitchdennett.framework.drivers.Mail;

public class MailManager extends Manager {

    public MailManager(Container c) {
        super(c);
        driver_prefix = "Mail";
        config = "mail";
    }

    public Mail create_driver(String driver) {
        return super.create_driver(driver, Mail.class);
    }
}
