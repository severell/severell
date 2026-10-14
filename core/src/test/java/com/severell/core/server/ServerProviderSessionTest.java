package com.severell.core.server;

import com.severell.core.config.Config;
import com.severell.core.container.Container;
import io.undertow.server.session.InMemorySessionManager;
import io.undertow.server.session.SessionAttachmentHandler;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class ServerProviderSessionTest {

    @TempDir
    Path dir;

    private void config(String env) throws Exception {
        Files.writeString(dir.resolve(".env"), env);
        Config.unload();
        Config.setDir(dir.toString());
        Config.loadConfig();
    }

    @AfterEach
    public void reset() {
        Config.unload();
        Config.setDir(null);
    }

    @Test
    public void databaseDriverUsesTheSessionsTable() throws Exception {
        String url = "jdbc:h2:mem:" + UUID.randomUUID() + ";MODE=PostgreSQL;DB_CLOSE_DELAY=-1";
        try (Connection c = DriverManager.getConnection(url); Statement s = c.createStatement()) {
            s.execute("CREATE TABLE sessions (id varchar(64) PRIMARY KEY, data text NOT NULL, created_at timestamp, expires_at timestamp NOT NULL)");
        }
        config("ENV=TEST\nSESSION_DRIVER=Database\nDB_DRIVER=org.h2.Driver\nDB_CONNSTRING=" + url + "\nDB_USERNAME=\nDB_PASSWORD=\n");

        SessionAttachmentHandler handler = new ServerProvider(new Container()).sessionHandler();

        assertTrue(handler.getSessionManager() instanceof DatabaseSessionManager);
        handler.getSessionManager().stop();
    }

    @Test
    public void databaseDriverFailsClearlyWithoutTheTable() throws Exception {
        String url = "jdbc:h2:mem:" + UUID.randomUUID() + ";DB_CLOSE_DELAY=-1";
        config("ENV=TEST\nSESSION_DRIVER=Database\nDB_DRIVER=org.h2.Driver\nDB_CONNSTRING=" + url + "\n");

        IllegalStateException e = assertThrows(IllegalStateException.class, () -> new ServerProvider(new Container()).sessionHandler());
        assertTrue(e.getMessage().contains("sessions"));
    }

    @Test
    public void memoryIsTheDefaultAndInsecureCookiesOutsideLocalStillStart() throws Exception {
        config("ENV=PRODUCTION\nSESSION_COOKIE_SECURE=false\n");

        SessionAttachmentHandler handler = new ServerProvider(new Container()).sessionHandler();

        assertTrue(handler.getSessionManager() instanceof InMemorySessionManager);
    }
}
