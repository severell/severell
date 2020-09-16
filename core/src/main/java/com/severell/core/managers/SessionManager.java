package com.severell.core.managers;

import com.severell.core.container.Container;
import com.severell.core.drivers.Session;

public class SessionManager extends Manager{

    public SessionManager(Container c) {
        super(c);
        driver_prefix = "Session";
        config = "session";
    }

    public Session create_driver(String driver) {
        return super.create_driver(driver, Session.class);
    }
}
