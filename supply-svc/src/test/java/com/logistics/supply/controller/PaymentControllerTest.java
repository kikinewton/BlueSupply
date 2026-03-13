package com.logistics.supply.controller;

import com.logistics.supply.common.annotations.IntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests for PaymentController.
 * Covers the financial read paths that must continue to work after security upgrades.
 * No payment data is seeded (no GRN/LPO chain), so list endpoints return empty results —
 * this is intentional: the tests verify the endpoint wiring and query layer, not data volume.
 */
@IntegrationTest
class PaymentControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Test
    @WithMockUser(username = "account.officer@test.com", roles = "ACCOUNT_OFFICER")
    void shouldFetchAllPayments() throws Exception {
        mockMvc.perform(get("/api/payments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"));
    }

    @Test
    @WithMockUser(username = "account.officer@test.com", roles = "ACCOUNT_OFFICER")
    void shouldFetchPaymentsBySupplier() throws Exception {
        // Supplier id=1 is seeded; no payments exist for it — returns empty page
        mockMvc.perform(get("/api/payments/supplier/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"));
    }

    @Test
    @WithMockUser(username = "account.officer@test.com", roles = "ACCOUNT_OFFICER")
    void shouldFetchPaymentsByInvoiceNumber() throws Exception {
        mockMvc.perform(get("/api/payments/invoice/INV-NONEXISTENT"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"));
    }

    @Test
    @WithMockUser(username = "account.officer@test.com", roles = "ACCOUNT_OFFICER")
    void shouldFetchPaymentsByPurchaseNumber() throws Exception {
        mockMvc.perform(get("/api/payments/purchaseNumber/PO-NONEXISTENT"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"));
    }

    @Test
    @WithMockUser(username = "account.officer@test.com", roles = "ACCOUNT_OFFICER")
    void shouldFilterPaymentsByInvoiceNumberQueryParam() throws Exception {
        mockMvc.perform(get("/api/payments")
                        .param("invoiceNumber", "INV-NONEXISTENT"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"));
    }

    @Test
    @WithMockUser(username = "account.officer@test.com", roles = "ACCOUNT_OFFICER")
    void shouldFilterPaymentsBySupplierIdQueryParam() throws Exception {
        // Supplier id=2 is seeded
        mockMvc.perform(get("/api/payments")
                        .param("supplierId", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"));
    }
}
