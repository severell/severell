package com.severell.core.time;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class TimeTest {

    @Test
    public void timeTest() {
        Time t = new Time();

        assertNotNull(Time.now());
    }
}
