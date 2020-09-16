package com.severell.core.providers;

import com.severell.core.config.Config;
import com.severell.core.container.Container;
import com.severell.core.drivers.Session;
import com.severell.core.drivers.SessionMemoryDriver;
import com.severell.core.managers.SessionManager;

public class SessionProvider extends ServiceProvider {

    public SessionProvider(Container c) {
        super(c);
    }

    @Override
    public void register() {
        this.c.bind("SessionMemoryDriver", (container) -> new SessionMemoryDriver());
        this.c.singleton(SessionManager.class, new SessionManager(this.c));
    }

    @Override
    public void boot() {
        this.c.bind(Session.class,  (container) -> container.make(SessionManager.class).create_driver(Config.get("SESSION_DRIVER")));
    }
}
