package com.severell.core.session;

import com.severell.core.config.Config;

/**
 * Cookie flags for the session cookie and the XSRF-TOKEN cookie.
 *
 * Defaults are secure everywhere except local development (ENV=TEST), where Secure is off so
 * the app works over http://localhost. Each flag can be overridden:
 * SESSION_COOKIE_SECURE, SESSION_COOKIE_HTTP_ONLY and SESSION_COOKIE_SAME_SITE (Strict, Lax or None).
 */
public class SessionCookieSettings {

    public static boolean secure() {
        return flag("SESSION_COOKIE_SECURE", !Config.isLocal());
    }

    public static boolean httpOnly() {
        return flag("SESSION_COOKIE_HTTP_ONLY", true);
    }

    public static String sameSite() {
        String value = Config.get("SESSION_COOKIE_SAME_SITE", "Lax").trim();
        if (value.equalsIgnoreCase("strict")) {
            return "Strict";
        }
        if (value.equalsIgnoreCase("none")) {
            return "None";
        }
        // Anything unrecognized falls back to Lax rather than dropping the protection.
        return "Lax";
    }

    private static boolean flag(String key, boolean defaultValue) {
        String value = Config.get(key);
        if (value == null || value.trim().isEmpty()) {
            return defaultValue;
        }
        return Boolean.parseBoolean(value.trim());
    }
}
