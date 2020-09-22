package com.severell.core.time;

import java.time.Clock;
import java.time.LocalDateTime;

/**
 * Use this class to get an instance of LocalDateTime
 * It makes mocking tests easier
 */
public class Time {

    private static Clock clock;

    /**
     * Get an instance of LocalDateTime
     * @return
     */
    public static LocalDateTime now() {
        return LocalDateTime.now(clock == null ? Clock.systemDefaultZone() : clock);
    }

    /**
     * Set a clock for LocalDateTime.now() to use.
     * @param cl
     */
    public static void setClock(Clock cl) {
        clock = cl;
    }

    /**
     * Set clock instance to null
     */
    public static void resetClock() {
        clock = null;
    }
}
