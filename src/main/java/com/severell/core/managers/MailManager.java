package com.severell.core.managers;

import com.severell.core.container.Container;
import com.severell.core.drivers.Mail;

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
