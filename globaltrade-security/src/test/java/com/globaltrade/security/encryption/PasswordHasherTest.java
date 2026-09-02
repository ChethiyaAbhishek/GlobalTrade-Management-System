package com.globaltrade.security.encryption;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class PasswordHasherTest {

    @Test
    @DisplayName("Hash then verify with correct password succeeds")
    void verifyCorrectPassword() {
        String hash = PasswordHasher.hash("password123");
        assertTrue(PasswordHasher.verify("password123", hash));
    }

    @Test
    @DisplayName("Verification fails with wrong password")
    void verifyWrongPasswordFails() {
        String hash = PasswordHasher.hash("password123");
        assertFalse(PasswordHasher.verify("password124", hash));
        assertFalse(PasswordHasher.verify("admin", hash));
    }

    @Test
    @DisplayName("Same password produces different hashes (random salt)")
    void saltsAreRandom() {
        assertNotEquals(PasswordHasher.hash("password123"),
                PasswordHasher.hash("password123"));
    }

    @Test
    @DisplayName("Hash format is iterations:salt:hash with 65536 iterations")
    void hashFormatIsCorrect() {
        String[] parts = PasswordHasher.hash("abc").split(":");
        assertEquals(3, parts.length);
        assertEquals("65536", parts[0]);
    }

    @Test
    @DisplayName("Null / empty passwords are rejected")
    void invalidInputsRejected() {
        assertThrows(IllegalArgumentException.class, () -> PasswordHasher.hash(null));
        assertThrows(IllegalArgumentException.class, () -> PasswordHasher.hash(""));
        assertFalse(PasswordHasher.verify(null, "65536:abc:def"));
        assertFalse(PasswordHasher.verify("x", "not-a-valid-hash"));
    }
}
