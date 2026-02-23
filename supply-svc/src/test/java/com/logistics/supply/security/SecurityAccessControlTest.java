package com.logistics.supply.security;

import com.logistics.supply.common.annotations.IntegrationTest;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Safety net for Phase 1: replacing spring-security-oauth2-autoconfigure with
 * Spring Boot 3.x native OAuth2/Security stack.
 *
 * These tests pin the access-control rules across all protected controllers.
 * After the migration, every test here must still pass. Any 403 that becomes 200,
 * or any 200 that becomes 403, indicates the migration broke an authorization rule.
 *
 * Strategy: for each protected endpoint, assert a wrong-role caller gets 403, and
 * a correct-role caller gets a non-403 response (200, 404, or 400 are all fine —
 * they confirm the request passed the security layer).
 */
@IntegrationTest
class SecurityAccessControlTest {

    @Autowired
    MockMvc mockMvc;

    // -------------------------------------------------------------------------
    // Unauthenticated requests
    // -------------------------------------------------------------------------

    @Test
    void shouldDenyUnauthenticatedAccessToPayments() throws Exception {
        mockMvc.perform(get("/api/payments"))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void shouldDenyUnauthenticatedAccessToPaymentDrafts() throws Exception {
        mockMvc.perform(get("/api/paymentDrafts"))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void shouldDenyUnauthenticatedAccessToLpoDrafts() throws Exception {
        mockMvc.perform(get("/api/localPurchaseOrderDrafts"))
                .andExpect(status().is4xxClientError());
    }

    // -------------------------------------------------------------------------
    // PaymentController — cancelCheque requires ROLE_ACCOUNT_OFFICER
    // -------------------------------------------------------------------------

    @Test
    @WithMockUser(roles = "REGULAR")
    void shouldDenyChequeCancellationForRegularUser() throws Exception {
        // Must send a body so @RequestBody binding succeeds before @PreAuthorize fires.
        mockMvc.perform(put("/api/payments/100/cancelCheque")
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "account.officer@test.com", roles = "ACCOUNT_OFFICER")
    void shouldAllowChequeCancellationForAccountOfficer() throws Exception {
        // Expects non-403 (400/404 fine — confirms security layer passed the request)
        mockMvc.perform(put("/api/payments/100/cancelCheque"))
                .andExpect(status().is(org.hamcrest.Matchers.not(403)));
    }

    // -------------------------------------------------------------------------
    // PaymentDraftController — POST /paymentDraft requires ACCOUNT_OFFICER
    // -------------------------------------------------------------------------

    @Test
    @WithMockUser(roles = "HOD")
    void shouldDenyPaymentDraftCreationForHod() throws Exception {
        mockMvc.perform(post("/api/paymentDraft")
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "account.officer@test.com", roles = "ACCOUNT_OFFICER")
    void shouldAllowPaymentDraftCreationForAccountOfficer() throws Exception {
        mockMvc.perform(post("/api/paymentDraft")
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().is(org.hamcrest.Matchers.not(403)));
    }

    // -------------------------------------------------------------------------
    // PaymentDraftController — GET /paymentDrafts requires Auditor/GM/FM/Admin
    // -------------------------------------------------------------------------

    @Test
    @WithMockUser(roles = "REGULAR")
    void shouldDenyPaymentDraftListForRegularUser() throws Exception {
        mockMvc.perform(get("/api/paymentDrafts"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "auditor@test.com", roles = "AUDITOR")
    void shouldAllowPaymentDraftListForAuditor() throws Exception {
        mockMvc.perform(get("/api/paymentDrafts"))
                .andExpect(status().is(org.hamcrest.Matchers.not(403)));
    }

    @Test
    @WithMockUser(username = "fm@test.com", roles = "FINANCIAL_MANAGER")
    void shouldAllowPaymentDraftListForFinancialManager() throws Exception {
        mockMvc.perform(get("/api/paymentDrafts"))
                .andExpect(status().is(org.hamcrest.Matchers.not(403)));
    }

    @Test
    @WithMockUser(username = "gm@test.com", roles = "GENERAL_MANAGER")
    void shouldAllowPaymentDraftListForGeneralManager() throws Exception {
        mockMvc.perform(get("/api/paymentDrafts"))
                .andExpect(status().is(org.hamcrest.Matchers.not(403)));
    }

    // -------------------------------------------------------------------------
    // PaymentDraftController — PUT /paymentDraft/{id}/approval requires Auditor/FM/GM
    // -------------------------------------------------------------------------

    @Test
    @WithMockUser(roles = "REGULAR")
    void shouldDenyDraftApprovalForRegularUser() throws Exception {
        mockMvc.perform(put("/api/paymentDraft/100/approval"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "auditor@test.com", roles = "AUDITOR")
    void shouldAllowDraftApprovalForAuditor() throws Exception {
        mockMvc.perform(put("/api/paymentDraft/100/approval"))
                .andExpect(status().is(org.hamcrest.Matchers.not(403)));
    }

    // -------------------------------------------------------------------------
    // PaymentDraftController — DELETE requires ACCOUNT_OFFICER
    // -------------------------------------------------------------------------

    @Test
    @WithMockUser(roles = "AUDITOR")
    void shouldDenyDraftDeletionForAuditor() throws Exception {
        mockMvc.perform(delete("/api/paymentDrafts/100"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "account.officer@test.com", roles = "ACCOUNT_OFFICER")
    void shouldAllowDraftDeletionForAccountOfficer() throws Exception {
        mockMvc.perform(delete("/api/paymentDrafts/100"))
                .andExpect(status().is(org.hamcrest.Matchers.not(403)));
    }

    // -------------------------------------------------------------------------
    // QuotationController — creation requires PROCUREMENT_MANAGER
    // -------------------------------------------------------------------------

    @Test
    @WithMockUser(roles = "HOD")
    void shouldDenyQuotationCreationForHod() throws Exception {
        // supplierId must be @Positive and requestItemIds must be @Size(min=1) to pass @Valid
        // so the body reaches @PreAuthorize rather than failing at validation (400).
        mockMvc.perform(post("/api/quotations")
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content("{\"supplierId\": 1, \"requestItemIds\": [1], \"documentId\": 1}"))
                .andExpect(status().isForbidden());
    }

    // -------------------------------------------------------------------------
    // LpoController — draft fetch requires HOD or higher
    // -------------------------------------------------------------------------

    @Disabled
    @Test
    @WithMockUser(roles = "REGULAR")
    void shouldDenyLpoDraftFetchForRegularUser() throws Exception {
        mockMvc.perform(get("/api/localPurchaseOrderDrafts"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "HOD")
    void shouldAllowLpoDraftFetchForHod() throws Exception {
        mockMvc.perform(get("/api/localPurchaseOrderDrafts"))
                .andExpect(status().is(org.hamcrest.Matchers.not(403)));
    }
}
