package com.severell.core.managers;

import com.severell.core.container.Container;
import com.severell.core.session.BaseSessionDriver;
import com.severell.core.session.Session;

import java.util.HashMap;

/**
 * The SessionManager is responsible for managing the different Session drivers.
 */
public class SessionManager extends Manager{

    private final ThreadLocal<HashMap<String, Session>> sessionThreadLocal = ThreadLocal.withInitial(() -> new HashMap<>());

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
        Session session;
        if(sessionThreadLocal.get().get(driver) != null) {
            session = sessionThreadLocal.get().get(driver);
        } else {
            session = super.create_driver(driver, Session.class);
            sessionThreadLocal.get().put(driver, session);
        }

        if(session != null) {
            ((BaseSessionDriver) session).setContainer(this.container);
        }
        return session;
    }
}
