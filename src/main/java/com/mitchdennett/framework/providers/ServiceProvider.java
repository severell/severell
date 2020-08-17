package com.mitchdennett.framework.providers;

import com.mitchdennett.framework.container.Container;

public abstract class ServiceProvider implements Provider {

    protected final Container c;

    public ServiceProvider(Container c) {
        this.c = c;
    }

    public abstract void register();

    public abstract void boot() throws Exception;

}
