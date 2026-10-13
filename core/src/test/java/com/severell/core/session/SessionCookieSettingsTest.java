package com.severell.core.session;

import com.severell.core.config.Config;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

public class SessionCookieSettingsTest {

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
    public void productionDefaultsAreSecure(@TempDir Path dir) throws Exception {
        load(dir, "ENV=PRODUCTION\n");
        assertTrue(SessionCookieSettings.secure());
        assertTrue(SessionCookieSettings.httpOnly());
        assertEquals("Lax", SessionCookieSettings.sameSite());
    }

    @Test
    public void missingEnvGetsSecureDefaults(@TempDir Path dir) throws Exception {
        load(dir, "OTHER=1\n");
        assertTrue(SessionCookieSettings.secure());
    }

    @Test
    public void localDevelopmentAllowsHttp(@TempDir Path dir) throws Exception {
        load(dir, "ENV=TEST\n");
        assertFalse(SessionCookieSettings.secure());
        assertTrue(SessionCookieSettings.httpOnly());
        assertEquals("Lax", SessionCookieSettings.sameSite());
    }

    @Test
    public void everyFlagCanBeOverridden(@TempDir Path dir) throws Exception {
        load(dir, "ENV=TEST\nSESSION_COOKIE_SECURE=true\nSESSION_COOKIE_HTTP_ONLY=false\nSESSION_COOKIE_SAME_SITE=strict\n");
        assertTrue(SessionCookieSettings.secure());
        assertFalse(SessionCookieSettings.httpOnly());
        assertEquals("Strict", SessionCookieSettings.sameSite());
    }

    @Test
    public void unknownSameSiteFallsBackToLax(@TempDir Path dir) throws Exception {
        load(dir, "SESSION_COOKIE_SAME_SITE=bogus\n");
        assertEquals("Lax", SessionCookieSettings.sameSite());
    }
}
