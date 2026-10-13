package com.severell.core.session;

import com.severell.core.config.Config;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

public class SessionSettingsTest {

    @AfterEach
    public void tearDown() {
        Config.unload();
        Config.setDir(null);
    }

    private void load(Path dir, String env) throws Exception {
        Files.writeString(dir.resolve(".env"), env);
        Config.unload();
        Config.setDir(dir.toString());
        Config.loadConfig();
    }

    @Test
    public void defaultsToMemoryForThirtyMinutes(@TempDir Path dir) throws Exception {
        load(dir, "OTHER=1\n");
        assertEquals(SessionSettings.MEMORY, SessionSettings.driver());
        assertEquals(1800, SessionSettings.lifetimeSeconds());
    }

    @Test
    public void databaseDriverInAnyCase(@TempDir Path dir) throws Exception {
        load(dir, "SESSION_DRIVER=database\nSESSION_LIFETIME=120\n");
        assertEquals(SessionSettings.DATABASE, SessionSettings.driver());
        assertEquals(7200, SessionSettings.lifetimeSeconds());
    }

    @Test
    public void unknownDriversAndBadLifetimesFail(@TempDir Path dir) throws Exception {
        load(dir, "SESSION_DRIVER=Redis\nSESSION_LIFETIME=soon\n");
        assertThrows(IllegalStateException.class, SessionSettings::driver);
        assertThrows(IllegalArgumentException.class, SessionSettings::lifetimeSeconds);
    }

    @Test
    public void lifetimeMustBePositive(@TempDir Path dir) throws Exception {
        load(dir, "SESSION_LIFETIME=0\n");
        assertThrows(IllegalArgumentException.class, SessionSettings::lifetimeSeconds);
    }
}
