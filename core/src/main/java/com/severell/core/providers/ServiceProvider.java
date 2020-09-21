package com.severell.core.providers;

import com.severell.core.container.Container;

/**
 * The base class for all Providers. All providers should extend this
 * instead if directly implementing the Provider interface.
 */
public abstract class ServiceProvider implements Provider {

    protected final Container c;

    public ServiceProvider(Container c) {
        this.c = c;
    }

    /**
     * Register the service provider
     */
    public abstract void register();

    /**
     * Boot the service provider
     * @throws Exception
     */
    public abstract void boot() throws Exception;

}
