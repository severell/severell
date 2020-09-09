package com.severell.core.providers;

public interface Provider {
    void register();
    void boot() throws Exception;
}
