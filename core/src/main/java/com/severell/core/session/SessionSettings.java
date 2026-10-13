package com.severell.core.session;

import com.severell.core.config.Config;

/**
 * Which session store to use and how long sessions last.
 *
 * SESSION_DRIVER: Memory (default; one server only) or Database (shared by every server; needs
 * a "sessions" table). SESSION_LIFETIME: minutes a session lasts without being used (default 30).
 */
public class SessionSettings {

    public static final String MEMORY = "Memory";
    public static final String DATABASE = "Database";

    /**
     * @throws IllegalStateException for an unknown driver
     */
    public static String driver() {
        String driver = Config.get("SESSION_DRIVER", MEMORY).trim();
        if (driver.equalsIgnoreCase(MEMORY)) {
            return MEMORY;
        }
        if (driver.equalsIgnoreCase(DATABASE)) {
            return DATABASE;
        }
        throw new IllegalStateException("Unsupported SESSION_DRIVER \"" + driver + "\". Use Memory or Database"
                + " (Redis sessions were removed).");
    }

    public static int lifetimeSeconds() {
        String value = Config.get("SESSION_LIFETIME", "30").trim();
        int minutes;
        try {
            minutes = Integer.parseInt(value);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("SESSION_LIFETIME must be a number of minutes, not \"" + value + "\"");
        }
        if (minutes < 1) {
            throw new IllegalArgumentException("SESSION_LIFETIME must be at least 1 minute");
        }
        return minutes * 60;
    }
}
