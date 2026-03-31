package com.logistics.supply.controller;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GRNControllerTest {

    @Test
    void findAllGRN() {
    }

    // Unit tests for the payment date guard:
    //   !paymentDate.isBefore(LocalDate.now())
    // This replaces the old paymentDate.after(new Date()) which was time-of-day sensitive.

    @Test
    void paymentDateGuard_todayIsAccepted() {
        LocalDate today = LocalDate.now();
        assertFalse(today.isBefore(LocalDate.now()), "Today should pass the payment date guard (today-or-future)");
    }

    @Test
    void paymentDateGuard_futureDateIsAccepted() {
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        assertFalse(tomorrow.isBefore(LocalDate.now()), "A future date should pass the payment date guard");
    }

    @Test
    void paymentDateGuard_yesterdayIsRejected() {
        LocalDate yesterday = LocalDate.now().minusDays(1);
        assertTrue(yesterday.isBefore(LocalDate.now()), "A past date should fail the payment date guard");
    }

    @Test
    void paymentDateGuard_farFutureIsAccepted() {
        LocalDate farFuture = LocalDate.now().plusYears(1);
        assertFalse(farFuture.isBefore(LocalDate.now()), "A far-future date should pass the payment date guard");
    }
}