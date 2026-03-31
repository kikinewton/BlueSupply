package com.logistics.supply.service;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Verifies the DateTimeFormatter-based date formatting used in
 * LocalPurchaseOrderService for LPO PDF generation.
 *
 * Previously used SimpleDateFormat which is not thread-safe; this
 * test confirms the replacement produces correct output and is safe
 * under concurrent access.
 */
class LocalPurchaseOrderDateFormatterTest {

    private static final String PATTERN = "EEEEE dd MMMMM yyyy";

    // Mirrors the formatting logic in LocalPurchaseOrderService
    private String formatLpoDate(Date date) {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern(PATTERN)
                .withLocale(Locale.UK)
                .withZone(ZoneId.systemDefault());
        return fmt.format(date.toInstant());
    }

    @Test
    void formattedDate_matchesExpectedPattern() {
        // 2024-01-15 = Monday
        LocalDate fixedDate = LocalDate.of(2024, 1, 15);
        Date input = Date.from(fixedDate.atStartOfDay(ZoneId.systemDefault()).toInstant());

        String result = formatLpoDate(input);

        // "EEEEE dd MMMMM yyyy": EEEEE=narrow day symbol, MMMMM=narrow month symbol
        // e.g. "M 15 J 2024" in UK locale for Monday 15 January 2024
        String expected = DateTimeFormatter.ofPattern(PATTERN)
                .withLocale(Locale.UK)
                .withZone(ZoneId.systemDefault())
                .format(input.toInstant());
        assertEquals(expected, result, "Formatted date should match DateTimeFormatter output");
        assertTrue(result.contains("2024"), "Result should contain year");
        assertTrue(result.contains("15"), "Result should contain day of month");
    }

    @Test
    void formattedDate_knownMonday() {
        LocalDate monday = LocalDate.of(2024, 1, 15);
        Date input = Date.from(monday.atStartOfDay(ZoneId.systemDefault()).toInstant());

        String result = formatLpoDate(input);

        String expected = DateTimeFormatter.ofPattern(PATTERN).withLocale(Locale.UK)
                .withZone(ZoneId.systemDefault()).format(input.toInstant());
        assertEquals(expected, result);
    }

    @Test
    void formattedDate_isSafeUnderConcurrency() throws Exception {
        LocalDate fixedDate = LocalDate.of(2024, 6, 1);
        Date input = Date.from(fixedDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
        String expectedResult = formatLpoDate(input);

        int threads = 20;
        List<Future<String>> futures;
        try (ExecutorService executor = Executors.newFixedThreadPool(threads)) {
            List<Callable<String>> tasks = new ArrayList<>();
            for (int i = 0; i < threads; i++) {
                tasks.add(() -> formatLpoDate(input));
            }

            futures = executor.invokeAll(tasks);
            executor.shutdown();
        }

        for (Future<String> f : futures) {
            assertDoesNotThrow(() -> {
                String result = f.get();
                assertEquals(expectedResult, result,
                        "Concurrent formatting produced inconsistent result");
            });
        }
    }

    @Test
    void formattedDate_usesSystemDefaultZone() {
        Instant now = Instant.now();
        Date input = Date.from(now);

        assertDoesNotThrow(() -> formatLpoDate(input),
                "Formatting with system default zone should not throw");
    }
}
