package com.logistics.supply.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CommonHelperTest {

    @Test
    void shouldValidateCorrectEmailAddress() {
        Assertions.assertTrue(CommonHelper.isValidEmailAddress("derrick12@gmail.com"));
        Assertions.assertTrue(CommonHelper.isValidEmailAddress("fronk2@gmail.com"));
    }

    @Test
    void shouldFailForInCorrectEmailAddress() {
        Assertions.assertFalse(CommonHelper.isValidEmailAddress("derrick12gmail.com"));
        Assertions.assertFalse(CommonHelper.isValidEmailAddress("fronk2@mailcom"));
        Assertions.assertFalse(CommonHelper.isValidEmailAddress(" "));
    }
}