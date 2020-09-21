package com.severell.core.providers;

public interface Provider {
    /**
     * Register the service provider
     */
    void register();

    /**
     * Boot the service provider
     * @throws Exception
     */
    void boot() throws Exception;
}
