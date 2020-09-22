package com.severell.core.config;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ConfigTest {

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
    public void testConfig() throws Exception {
        assertNull(Config.get("NullTest"));
        assertEquals("hello", Config.get("TEST"));
        assertTrue(Config.isLoaded());
        assertEquals("notset", Config.get("ISNOTSET", "notset"));
        assertEquals("hello", Config.get("TEST", "isset"));

        assertThrows(Exception.class, () -> {
            Config.loadConfig();
        });
    }

    @Test
    public void testIsLocal() {
        assertTrue(Config.isLocal(), "ENV should be set to TEST");
    }

    @Test
    public void testIsSet() {
        assertTrue(Config.isSet("ENV"));
        assertFalse(Config.isSet("NOTSET"));
        assertFalse(Config.isSet("ISNTINENV"));
    }

    @Test
    public void testIsEqual() {
        assertTrue(Config.equals("ENV", "TEST"));
        assertFalse(Config.equals("NOTSET", "SOMETHING"));
        assertFalse(Config.equals("ISNTINENV", "something"));
    }
}

