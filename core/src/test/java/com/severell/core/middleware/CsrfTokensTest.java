package com.severell.core.middleware;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class CsrfTokensTest {

    @Test
    public void maskedTokenMatchesItsSecret() {
        String secret = CsrfTokens.newSecret();
        assertTrue(CsrfTokens.isValidSecret(secret));
        assertTrue(CsrfTokens.matches(CsrfTokens.mask(secret), secret));
    }

    @Test
    public void everyMaskLooksDifferent() {
        // BREACH defense: the bytes sent differ every time, though they all unmask to the secret.
        String secret = CsrfTokens.newSecret();
        String first = CsrfTokens.mask(secret);
        String second = CsrfTokens.mask(secret);
        assertNotEquals(first, second);
        assertNotEquals(secret, first);
        assertTrue(CsrfTokens.matches(first, secret));
        assertTrue(CsrfTokens.matches(second, secret));
    }

    @Test
    public void otherSecretsAndJunkDontMatch() {
        String secret = CsrfTokens.newSecret();
        assertFalse(CsrfTokens.matches(CsrfTokens.mask(CsrfTokens.newSecret()), secret));
        // The unmasked secret itself isn't accepted.
        assertFalse(CsrfTokens.matches(secret, secret));
        assertFalse(CsrfTokens.matches(null, secret));
        assertFalse(CsrfTokens.matches("", secret));
        assertFalse(CsrfTokens.matches("not base64 !!", secret));
        assertFalse(CsrfTokens.matches(CsrfTokens.mask(secret), null));
    }

    @Test
    public void tamperedTokenDoesntMatch() {
        String secret = CsrfTokens.newSecret();
        char[] masked = CsrfTokens.mask(secret).toCharArray();
        masked[masked.length - 2] = masked[masked.length - 2] == 'A' ? 'B' : 'A';
        assertFalse(CsrfTokens.matches(new String(masked), secret));
    }

    @Test
    public void tokensFromOlderVersionsArentSecrets() {
        assertFalse(CsrfTokens.isValidSecret(UUID.randomUUID().toString()));
        assertFalse(CsrfTokens.isValidSecret(null));
    }
}
