package com.severell.core.session;

import com.severell.core.config.Config;

/**
 * Which session store to use and how long sessions last.
 *
 * SESSION_DRIVER: Memory (default; one server only) or Database (shared by every server; needs
 * a "sessions" table). SESSION_LIFETIME: minutes a session lasts without being used (default 30).
 * SESSION_MAX_LIFETIME: minutes a session lasts at most, however often it's used (default 14 days),
 * so a stolen session cookie stops working. Signing in starts a new session, and a new clock.
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
        return minutes("SESSION_LIFETIME", "30") * 60;
    }

    /**
     * @throws IllegalArgumentException if it's shorter than SESSION_LIFETIME
     */
    public static int maxLifetimeSeconds() {
        int max = minutes("SESSION_MAX_LIFETIME", String.valueOf(14 * 24 * 60)) * 60;
        if (max < lifetimeSeconds()) {
            throw new IllegalArgumentException("SESSION_MAX_LIFETIME can't be shorter than SESSION_LIFETIME");
        }
        return max;
    }

    private static int minutes(String key, String defaultValue) {
        String value = Config.get(key, defaultValue).trim();
        int minutes;
        try {
            minutes = Integer.parseInt(value);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(key + " must be a number of minutes, not \"" + value + "\"");
        }
        if (minutes < 1 || minutes > Integer.MAX_VALUE / 60) {
            throw new IllegalArgumentException(key + " must be between 1 and " + Integer.MAX_VALUE / 60 + " minutes");
        }
        return minutes;
    }
}
