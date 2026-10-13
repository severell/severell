package com.severell.core.http;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class StrictCookiesTest {

    @Test
    public void readsAPlainCookie() {
        assertEquals("abc", StrictCookies.value(List.of("a=1; __Host-session=abc; b=2"), "__Host-session"));
        assertEquals("abc", StrictCookies.value(List.of("a=1", "__Host-session=abc"), "__Host-session"));
    }

    @Test
    public void missingIsNull() {
        assertNull(StrictCookies.value(List.of("a=1"), "__Host-session"));
        assertNull(StrictCookies.value(List.of(), "__Host-session"));
        assertNull(StrictCookies.value(null, "__Host-session"));
    }

    @Test
    public void aCookieSmuggledInsideAnotherIsRefusedAndSoIsTheRealOne() {
        // Undertow's parser reads this as __Host-session=ATTACKER.
        assertNull(StrictCookies.value(List.of("t1=\"x\" __Host-session=ATTACKER; __Host-session=VICTIM"), "__Host-session"));
        assertNull(StrictCookies.value(List.of("t1=x __Host-session=ATTACKER"), "__Host-session"));
    }

    @Test
    public void repeatedOrMalformedIsRefused() {
        assertNull(StrictCookies.value(List.of("__Host-session=a; __Host-session=b"), "__Host-session"));
        assertNull(StrictCookies.value(List.of("__Host-session=a", "__Host-session=b"), "__Host-session"));
        assertNull(StrictCookies.value(List.of("__Host-session=\"a\""), "__Host-session"));
        assertNull(StrictCookies.value(List.of("__Host-session=a b"), "__Host-session"));
        assertNull(StrictCookies.value(List.of("__Host-session"), "__Host-session"));
        assertNull(StrictCookies.value(List.of("__Host-session="), "__Host-session"));
    }

    @Test
    public void aNamelessCookieCarryingTheNameIsRefused() {
        assertNull(StrictCookies.value(List.of("=__Host-session=ATTACKER"), "__Host-session"));
        assertNull(StrictCookies.value(List.of("t1=\"x\"__Host-session=ATTACKER"), "__Host-session"));
    }

    @Test
    public void otherCookiesThatMerelyEndInTheNameDontInterfere() {
        // e.g. another local app's _csrf cookie next to our plain "csrf" cookie in development.
        assertEquals("abc", StrictCookies.value(List.of("_csrf=zzz; csrf=abc; x=my_csrf=1"), "csrf"));
    }

    @Test
    public void namesAreCaseSensitive() {
        assertNull(StrictCookies.value(List.of("__host-session=abc"), "__Host-session"));
    }
}
