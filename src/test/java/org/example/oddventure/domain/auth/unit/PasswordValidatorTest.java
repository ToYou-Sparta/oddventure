package org.example.oddventure.domain.auth.unit;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.example.oddventure.domain.auth.validation.PasswordValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class PasswordValidatorTest {

    private final PasswordValidator passwordValidator = new PasswordValidator();

    @BeforeEach
    void setup() {
        passwordValidator.initialize(null);
    }

    @Test
    void validPassword() {
        assertTrue(passwordValidator.isValid("hello123!@#", null));
    }

    @Test
    void tooShortPassword() {
        assertFalse(passwordValidator.isValid("h1!", null));
    }

    @Test
    void missingSpecialCharacter() {
        assertFalse(passwordValidator.isValid("hello123", null));
    }

    @Test
    void nullPassword() {
        assertFalse(passwordValidator.isValid(null, null));
    }
}
