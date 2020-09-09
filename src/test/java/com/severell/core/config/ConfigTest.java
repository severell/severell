package com.severell.core.config;

import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ConfigTest {

    @Order(1)
    @Test
    public void testConfig() throws Exception {
        assertNull(Config.get("NullTest"));
        Config.setDir("src/test/resources");
        Config.loadConfig();
        assertEquals("hello", Config.get("TEST"));

        assertThrows(Exception.class, () -> {
            Config.loadConfig();
        });

        Config.unload();
    }
}
