package com.mitchdennett.framework.crypto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class PasswordUtilsTest {

    @Test
    public void testHashPasswordCorrectly() {
        String password = "mypassword";
        String hashed = PasswordUtils.hashPassword(password);
        assertNotNull(hashed);

        boolean isValid = PasswordUtils.checkPassword(password, hashed);
        assertTrue(isValid);

        isValid = PasswordUtils.checkPassword("notmypassword", hashed);
        assertFalse(isValid);
    }

    @Test
    public void testHashPasswordCorrectlyHandlesNull() {
        String password = "mypassword";
        String hashed = PasswordUtils.hashPassword(password);

        boolean isValid = PasswordUtils.checkPassword(null, hashed);
        assertFalse(isValid);

        assertThrows(IllegalArgumentException.class, () -> {
            PasswordUtils.checkPassword(null, null);
        });

        assertThrows(IllegalArgumentException.class, () -> {
            PasswordUtils.checkPassword(null, "wronghash");
        });
    }
}
