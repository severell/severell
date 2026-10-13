package com.severell.core.middleware;

import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * CSRF tokens, masked against BREACH.
 *
 * The session holds one secret token. Every time it's put in a page or cookie it's masked with
 * a fresh random pad, sent as base64url(pad || pad XOR secret), so its bytes differ in every
 * response and compression can't leak it. Checking a submitted token unmasks it and compares
 * it to the secret in constant time.
 */
public class CsrfTokens {

    /**
     * Where the session keeps its secret. Sessions drop it when their id is regenerated.
     */
    public static final String SESSION_KEY = "csrfToken";

    static final int TOKEN_BYTES = 32;

    private static final SecureRandom random = new SecureRandom();
    private static final Base64.Encoder encoder = Base64.getUrlEncoder().withoutPadding();
    private static final Base64.Decoder decoder = Base64.getUrlDecoder();

    /**
     * @return a new secret token to keep in the session
     */
    public static String newSecret() {
        byte[] secret = new byte[TOKEN_BYTES];
        random.nextBytes(secret);
        return encoder.encodeToString(secret);
    }

    /**
     * @return whether a stored value is a secret this class made, e.g. not one from an older version
     */
    public static boolean isValidSecret(String secret) {
        return decode(secret, TOKEN_BYTES) != null;
    }

    /**
     * @return the secret masked with a fresh pad, to put in a form or cookie
     */
    public static String mask(String secret) {
        byte[] raw = decode(secret, TOKEN_BYTES);
        if (raw == null) {
            throw new IllegalArgumentException("Not a CSRF secret");
        }
        byte[] pad = new byte[TOKEN_BYTES];
        random.nextBytes(pad);
        byte[] masked = new byte[TOKEN_BYTES * 2];
        for (int i = 0; i < TOKEN_BYTES; i++) {
            masked[i] = pad[i];
            masked[TOKEN_BYTES + i] = (byte) (pad[i] ^ raw[i]);
        }
        return encoder.encodeToString(masked);
    }

    /**
     * @return whether a submitted masked token matches the session's secret
     */
    public static boolean matches(String maskedToken, String secret) {
        byte[] raw = decode(secret, TOKEN_BYTES);
        byte[] masked = decode(maskedToken, TOKEN_BYTES * 2);
        if (raw == null || masked == null) {
            return false;
        }
        byte[] unmasked = new byte[TOKEN_BYTES];
        for (int i = 0; i < TOKEN_BYTES; i++) {
            unmasked[i] = (byte) (masked[i] ^ masked[TOKEN_BYTES + i]);
        }
        return MessageDigest.isEqual(unmasked, raw);
    }

    private static byte[] decode(String value, int expectedLength) {
        if (value == null) {
            return null;
        }
        try {
            byte[] bytes = decoder.decode(value);
            return bytes.length == expectedLength ? bytes : null;
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
