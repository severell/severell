package com.severell.core.server;

import com.severell.core.config.Config;
import com.severell.core.container.Container;
import com.severell.core.http.AppServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.net.ServerSocket;
import java.net.Socket;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class ServerProviderTest {

    @BeforeAll
    public static void setup() throws Exception {
        Config.setDir("src/test/resources");
        Config.loadConfig();
    }

    @AfterAll
    public static void tearDown() {
        Config.unload();
    }

    @Test
    public void serverListensOnLocalhostByDefault() throws Exception {
        int port;
        try (ServerSocket socket = new ServerSocket(0)) {
            port = socket.getLocalPort();
        }

        Container c = new Container();
        AppServer appServer = new AppServer(String.valueOf(port));
        c.singleton(AppServer.class, appServer);

        new ServerProvider(c).boot();
        appServer.start();

        try (Socket socket = new Socket("127.0.0.1", port)) {
            assertTrue(socket.isConnected());
        }
    }
}
