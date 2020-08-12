package com.mitchdennett.framework.managers;

import com.mitchdennett.framework.container.Container;
import com.mitchdennett.framework.drivers.Session;

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
