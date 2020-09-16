package com.severell.core.http;

import java.util.function.Consumer;

/**
 * This class abstracts the underlying server container (i.e Jetty)
 */
public class AppServer {

    private final String PORT;
    private Consumer listener;

    /**
     * Create a new AppServer.
     * @param port Port for the server to listen on
     */
    public AppServer(String port) {
        this.PORT = port;
    }

    /**
     * Get the port the server is listening on
     * @return
     */
    public String getPort() {
        return this.PORT;
    }

    /**
     * Used by the underlying server to register a listener to be notified of any
     * events.
     * @param listener
     */
    public void registerListener(Consumer listener) {
        this.listener = listener;
    }

    /**
     * Notify the underlying server container to start
     */
    public void start() {
        this.listener.accept(null);
    }
}
