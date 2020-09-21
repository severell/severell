package com.severell.core.managers;

import com.severell.core.container.Container;
import com.severell.core.drivers.Session;
import com.severell.core.mail.Mail;

/**
 * The SessionManager is responsible for managing the different Session drivers.
 */
public class SessionManager extends Manager{

    public SessionManager(Container c) {
        super(c);
        driver_prefix = "Session";
        config = "session";
    }

    /**
     * Create a new concrete instance of {@link Session} interface.
     * @param driver
     * @return
     */
    public Session create_driver(String driver) {
        return super.create_driver(driver, Session.class);
    }
}
