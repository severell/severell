package com.severell.core.http;

import java.util.function.Consumer;

public class AppServer {

    private final String PORT;
    private Consumer listener;

    public AppServer(String port) {
        this.PORT = port;
    }

    public String getPort() {
        return this.PORT;
    }

    public void registerListener(Consumer listener) {
        this.listener = listener;
    }

    public void start() {
        this.listener.accept(null);
    }
}
