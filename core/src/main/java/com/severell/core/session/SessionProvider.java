package com.severell.core.session;

import com.severell.core.config.Config;
import com.severell.core.container.Container;
import com.severell.core.providers.ServiceProvider;
import com.severell.core.managers.SessionManager;

/**
 * The SessionProvider is used to register and setup all functionality
 * around Sessions. It adds all the SessionDrivers into the Container
 * and creates the {@link SessionManager}
 */
public class SessionProvider extends ServiceProvider {

    public SessionProvider(Container c) {
        super(c);
    }

    @Override
    public void register() {
        String driver = Config.get("SESSION_DRIVER", "Memory");
        if(!driver.equalsIgnoreCase("Memory")) {
            // Fail at startup rather than run without sessions.
            throw new IllegalStateException("Unsupported SESSION_DRIVER \"" + driver + "\". Only Memory is supported;"
                    + " Redis sessions were removed.");
        }
        this.c.bind("SessionMemoryDriver", (container) -> new SessionMemoryDriver());
        this.c.singleton(SessionManager.class, new SessionManager(this.c));
    }

    @Override
    public void boot() {
        // Memory is the only driver (checked in register()); don't pass the raw setting, whose
        // spelling ("memory") wouldn't match the driver's binding name.
        this.c.bind(Session.class,  (container) -> container.make(SessionManager.class).create_driver("Memory"));
    }
}
