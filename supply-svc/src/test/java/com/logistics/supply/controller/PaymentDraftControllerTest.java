package com.logistics.supply.controller;

import com.logistics.supply.common.annotations.IntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests for PaymentDraftController.
 *
 * The seeded payment draft (id=100, purchase_number='PO-TEST-001') and employees with
 * the required roles are set up in init_script.sql.
 *
 * Approval tests use usernames that match seeded employees so that
 * PaymentDraftController.paymentApproval() can look up the employee's role from the DB.
 * The @WithMockUser role must match that employee's DB role for @PreAuthorize to pass.
 */
@IntegrationTest
class PaymentDraftControllerTest {

    private static final int SEEDED_DRAFT_ID = 100;

    @Autowired
    MockMvc mockMvc;

    // -------------------------------------------------------------------------
    // Fetch endpoints
    // -------------------------------------------------------------------------

    @Test
    @WithMockUser(username = "auditor@test.com", roles = "ROLE_AUDITOR")
    void shouldFetchPaymentDraftById() throws Exception {
        mockMvc.perform(get("/api/paymentDraft/{id}", SEEDED_DRAFT_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.data.id").value(SEEDED_DRAFT_ID));
    }

    @Test
    @WithMockUser(username = "auditor@test.com", roles = "ROLE_AUDITOR")
    void shouldListPaymentDraftsAsAuditor() throws Exception {
        mockMvc.perform(get("/api/paymentDrafts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"));
    }

    @Test
    @WithMockUser(username = "fm@test.com", roles = "ROLE_FINANCIAL_MANAGER")
    void shouldListPaymentDraftsAsFinancialManager() throws Exception {
        mockMvc.perform(get("/api/paymentDrafts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"));
    }

    @Test
    @WithMockUser(username = "gm@test.com", roles = "ROLE_GENERAL_MANAGER")
    void shouldListPaymentDraftsAsGeneralManager() throws Exception {
        mockMvc.perform(get("/api/paymentDrafts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"));
    }

    @Test
    @WithMockUser(username = "account.officer@test.com", roles = "ROLE_ACCOUNT_OFFICER")
    void shouldFetchPaymentDraftHistory() throws Exception {
        mockMvc.perform(get("/api/paymentDrafts/history"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "auditor@test.com", roles = "ROLE_AUDITOR")
    void shouldFetchPaymentDraftHistoryWithAllFlag() throws Exception {
        mockMvc.perform(get("/api/paymentDrafts/history")
                        .param("all", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"));
    }

    @Test
    @WithMockUser(username = "account.officer@test.com", roles = "ROLE_ACCOUNT_OFFICER")
    void shouldFetchGrnWithoutCompletePayment() throws Exception {
        mockMvc.perform(get("/api/paymentDraft/grnWithoutPayment")
                        .param("paymentStatus", "PARTIAL"))
                .andExpect(status().isOk());
    }

    // -------------------------------------------------------------------------
    // Approval workflow — Auditor → Financial Manager → General Manager
    // Each test runs against a fresh seeded draft (DB is cleared before each test)
    // -------------------------------------------------------------------------

    @Test
    @WithMockUser(username = "auditor@test.com", roles = "ROLE_AUDITOR")
    void shouldApprovePaymentDraftAsAuditor() throws Exception {
        mockMvc.perform(put("/api/paymentDraft/{id}/approval", SEEDED_DRAFT_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.message").value("APPROVAL SUCCESSFUL"));
    }

    @Test
    @WithMockUser(username = "fm@test.com", roles = "ROLE_FINANCIAL_MANAGER")
    void shouldApprovePaymentDraftAsFinancialManager() throws Exception {
        mockMvc.perform(put("/api/paymentDraft/{id}/approval", SEEDED_DRAFT_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.message").value("APPROVAL SUCCESSFUL"));
    }

    @Test
    @WithMockUser(username = "gm@test.com", roles = "ROLE_GENERAL_MANAGER")
    void shouldApprovePaymentDraftAsGeneralManager() throws Exception {
        mockMvc.perform(put("/api/paymentDraft/{id}/approval", SEEDED_DRAFT_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.message").value("APPROVAL SUCCESSFUL"));
    }

    // -------------------------------------------------------------------------
    // Delete
    // -------------------------------------------------------------------------

    @Test
    @WithMockUser(username = "account.officer@test.com", roles = "ROLE_ACCOUNT_OFFICER")
    void shouldDeletePaymentDraft() throws Exception {
        mockMvc.perform(delete("/api/paymentDrafts/{id}", SEEDED_DRAFT_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.message").value("PAYMENT DRAFT DELETED"));
    }
}
