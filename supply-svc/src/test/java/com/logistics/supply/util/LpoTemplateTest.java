package com.logistics.supply.util;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertTrue;

class LpoTemplateTest {

    private static final int LPO_ID = 42;
    private static final String SUPPLIER = "ACME Corporation";
    private static final String COMMENT = "Deliver by end of month";
    private static final String TABLE_HTML = "<thead><tr><th>Item</th><th>Qty</th></tr></thead><tbody><tr><td>Laptop</td><td>5</td></tr></tbody>";
    private static final LocalDate FIXED_DATE = LocalDate.of(2024, 1, 15);

    @Test
    void lpoCompose_shouldContainLpoId() {
        String result = LpoTemplate.lpoCompose(LPO_ID, SUPPLIER, COMMENT, TABLE_HTML, FIXED_DATE);

        assertTrue(result.contains(String.valueOf(LPO_ID)));
    }

    @Test
    void lpoCompose_shouldContainSupplierName() {
        String result = LpoTemplate.lpoCompose(LPO_ID, SUPPLIER, COMMENT, TABLE_HTML, FIXED_DATE);

        assertTrue(result.contains(SUPPLIER));
    }

    @Test
    void lpoCompose_shouldContainComment() {
        String result = LpoTemplate.lpoCompose(LPO_ID, SUPPLIER, COMMENT, TABLE_HTML, FIXED_DATE);

        assertTrue(result.contains(COMMENT));
    }

    @Test
    void lpoCompose_shouldContainTableHtml() {
        String result = LpoTemplate.lpoCompose(LPO_ID, SUPPLIER, COMMENT, TABLE_HTML, FIXED_DATE);

        assertTrue(result.contains(TABLE_HTML));
    }

    @Test
    void lpoCompose_shouldContainFormattedDate() {
        String expectedDate = DateTimeFormatter.ofPattern("EEEEE dd MMMMM yyyy")
                .withLocale(Locale.UK)
                .format(FIXED_DATE);

        String result = LpoTemplate.lpoCompose(LPO_ID, SUPPLIER, COMMENT, TABLE_HTML, FIXED_DATE);

        assertTrue(result.contains(expectedDate));
    }

    @Test
    void lpoCompose_defaultOverload_usesTodaysDate() {
        String expectedDate = DateTimeFormatter.ofPattern("EEEEE dd MMMMM yyyy")
                .withLocale(Locale.UK)
                .format(LocalDate.now());

        String result = LpoTemplate.lpoCompose(LPO_ID, SUPPLIER, COMMENT, TABLE_HTML);

        assertTrue(result.contains(expectedDate));
    }

    @Test
    void lpoCompose_shouldReturnXhtmlDocument() {
        String result = LpoTemplate.lpoCompose(LPO_ID, SUPPLIER, COMMENT, TABLE_HTML, FIXED_DATE);

        assertTrue(result.contains("<!DOCTYPE html"));
        assertTrue(result.contains("</html>"));
    }
}
