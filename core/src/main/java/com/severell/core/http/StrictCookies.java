package com.severell.core.http;

import java.util.regex.Pattern;

/**
 * Reads security cookies (the session id, the CSRF secret) straight from the Cookie header,
 * strictly. Undertow's own parser is lenient: a cookie whose value is {@code "x" __Host-session=...}
 * is read as a second cookie, which lets another subdomain smuggle in a {@code __Host-} cookie
 * the browser would never let it set. Here a cookie only counts if it appears exactly once as a
 * plain {@code name=value} pair, and not at all if any other cookie mentions its name.
 */
public final class StrictCookies {

    private static final Pattern VALUE = Pattern.compile("[A-Za-z0-9_-]{1,512}");

    private StrictCookies() {
    }

    /**
     * @param cookieHeaders every Cookie header in the request (HTTP/2 may send several)
     * @return the cookie's value, or null if it's missing, repeated, malformed or smuggled
     */
    public static String value(Iterable<String> cookieHeaders, String name) {
        if (cookieHeaders == null) {
            return null;
        }
        String found = null;
        for (String header : cookieHeaders) {
            if (header == null) {
                continue;
            }
            for (String part : header.split(";")) {
                String pair = part.trim();
                int eq = pair.indexOf('=');
                String partName = eq < 0 ? pair : pair.substring(0, eq);
                if (partName.equals(name)) {
                    String value = pair.substring(eq + 1);
                    if (found != null || eq < 0 || !VALUE.matcher(value).matches()) {
                        return null;
                    }
                    found = value;
                } else if (pair.contains(name + "=")) {
                    // Another cookie carrying this name: possibly smuggled. Trust neither.
                    return null;
                }
            }
        }
        return found;
    }
}
