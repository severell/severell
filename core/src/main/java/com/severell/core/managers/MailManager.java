package com.severell.core.managers;

import com.severell.core.container.Container;
import com.severell.core.mail.Mail;

/**
 * The MailManager is responsible for managing the different Mail drivers.
 */
public class MailManager extends Manager {

    public MailManager(Container c) {
        super(c);
        driver_prefix = "Mail";
        config = "mail";
    }

    /**
     * Create a new concrete instance of {@link Mail} interface.
     * @param driver Driver name - I.E. SMTP, LOG
     * @return
     */
    public Mail create_driver(String driver) {
        return super.create_driver(driver, Mail.class);
    }
}
