package com.logistics.supply.annotation.validator;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class NameValidatorTest {

    // --- Valid names ---

    @Test
    void shouldPassForStandardSingleWord() {
        Assertions.assertTrue(NameValidator.isValidName("Mechanical"));
    }

    @Test
    void shouldPassForLowercaseName() {
        Assertions.assertTrue(NameValidator.isValidName("mechanical"));
    }

    @Test
    void shouldPassForUppercaseName() {
        Assertions.assertTrue(NameValidator.isValidName("MECHANICAL"));
    }

    @Test
    void shouldPassForShortName() {
        Assertions.assertTrue(NameValidator.isValidName("John"));
    }

    @Test
    void shouldPassForSingleCharacter() {
        Assertions.assertTrue(NameValidator.isValidName("A"));
    }

    @Test
    void shouldPassForNameWithApostrophe() {
        Assertions.assertTrue(NameValidator.isValidName("O'Brien"));
    }

    @Test
    void shouldPassForNameWithDotAndSpace() {
        Assertions.assertTrue(NameValidator.isValidName("St. James"));
    }

    @Test
    void shouldPassForNameWithHyphen() {
        Assertions.assertTrue(NameValidator.isValidName("Smith-Jones"));
    }

    @Test
    void shouldPassForMaxLengthName() {
        // 25 characters: 1 letter + 24 more
        Assertions.assertTrue(NameValidator.isValidName("Abcdefghijklmnopqrstuvwxy"));
    }

    // --- Invalid names ---

    @Test
    void shouldFailForNameWithDisallowedCharacter() {
        Assertions.assertFalse(NameValidator.isValidName("Mechanical!"));
    }

    @Test
    void shouldFailForNameStartingWithDigit() {
        Assertions.assertFalse(NameValidator.isValidName("1Mechanical"));
    }

    @Test
    void shouldFailForNameStartingWithSpace() {
        Assertions.assertFalse(NameValidator.isValidName(" Mechanical"));
    }

    @Test
    void shouldFailForNameEndingWithSpace() {
        Assertions.assertFalse(NameValidator.isValidName("Mechanical "));
    }

    @Test
    void shouldFailForNameEndingWithDot() {
        Assertions.assertFalse(NameValidator.isValidName("Mechanical."));
    }

    @Test
    void shouldFailForNameEndingWithHyphen() {
        Assertions.assertFalse(NameValidator.isValidName("Mechanical-"));
    }

    @Test
    void shouldFailForNameEndingWithComma() {
        Assertions.assertFalse(NameValidator.isValidName("Mechanical,"));
    }

    @Test
    void shouldFailForNameEndingWithApostrophe() {
        Assertions.assertFalse(NameValidator.isValidName("Mechanical'"));
    }

    @Test
    void shouldFailForNameExceedingMaxLength() {
        // 26 characters
        Assertions.assertFalse(NameValidator.isValidName("Abcdefghijklmnopqrstuvwxyz"));
    }

    @Test
    void shouldFailForEmptyString() {
        Assertions.assertFalse(NameValidator.isValidName(""));
    }

    // --- Null input ---

    @Test
    void shouldThrowForNullInput() {
        Assertions.assertThrows(NullPointerException.class, () -> NameValidator.isValidName(null));
    }
}
