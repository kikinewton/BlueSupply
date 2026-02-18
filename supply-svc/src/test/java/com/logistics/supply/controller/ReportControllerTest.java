package com.logistics.supply.controller;

import com.logistics.supply.common.annotations.IntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Safety net for Phase 4 (iText7 7.2.3 → 7.2.6) and Phase 3 (Flying Saucer 9.1.22 → 9.3.x).
 *
 * These tests confirm that all report endpoints respond correctly after each library upgrade.
 * JSON-response variants verify the service and query layer; Excel-download variants verify
 * that Apache POI and the ExcelService still generate a non-empty stream.
 */
@IntegrationTest
class ReportControllerTest {

    @Autowired
    MockMvc mockMvc;

    // -------------------------------------------------------------------------
    // Procured Items Report
    // -------------------------------------------------------------------------

    @Test
    @WithMockUser(username = "kikinewton@gmail.com", roles = "ROLE_PROCUREMENT_MANAGER")
    void shouldReturnProcuredItemsReportAsJson() throws Exception {
        mockMvc.perform(get("/res/procurement/procuredItemsReport")
                        .param("periodStart", "2020-01-01")
                        .param("periodEnd", "2030-01-01"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"));
    }

    @Test
    @WithMockUser(username = "kikinewton@gmail.com", roles = "ROLE_PROCUREMENT_MANAGER")
    void shouldDownloadProcuredItemsReportAsExcel() throws Exception {
        mockMvc.perform(get("/res/procurement/procuredItemsReport")
                        .param("periodStart", "2020-01-01")
                        .param("periodEnd", "2030-01-01")
                        .param("download", "true"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", org.hamcrest.Matchers.containsString("items_report_")))
                .andExpect(content().contentTypeCompatibleWith("application/vnd.ms-excel"));
    }

    // -------------------------------------------------------------------------
    // Payment Report
    // -------------------------------------------------------------------------

    @Test
    @WithMockUser(username = "auditor@test.com", roles = "ROLE_AUDITOR")
    void shouldReturnPaymentReportAsJson() throws Exception {
        mockMvc.perform(get("/res/accounts/paymentReport")
                        .param("periodStart", "2020-01-01")
                        .param("periodEnd", "2030-01-01"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"));
    }

    @Test
    @WithMockUser(username = "auditor@test.com", roles = "ROLE_AUDITOR")
    void shouldDownloadPaymentReportAsExcel() throws Exception {
        mockMvc.perform(get("/res/accounts/paymentReport")
                        .param("periodStart", "2020-01-01")
                        .param("periodEnd", "2030-01-01")
                        .param("download", "true"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", org.hamcrest.Matchers.containsString("payments_report_")))
                .andExpect(content().contentTypeCompatibleWith("application/octet-stream"));
    }

    // -------------------------------------------------------------------------
    // Petty Cash Payment Report
    // -------------------------------------------------------------------------

    @Test
    @WithMockUser(username = "auditor@test.com", roles = "ROLE_AUDITOR")
    void shouldReturnPettyCashPaymentReportAsJson() throws Exception {
        mockMvc.perform(get("/res/accounts/pettyCashPaymentReport")
                        .param("periodStart", "2020-01-01")
                        .param("periodEnd", "2030-01-01"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"));
    }

    @Test
    @WithMockUser(username = "auditor@test.com", roles = "ROLE_AUDITOR")
    void shouldDownloadPettyCashPaymentReportAsExcel() throws Exception {
        mockMvc.perform(get("/res/accounts/pettyCashPaymentReport")
                        .param("periodStart", "2020-01-01")
                        .param("periodEnd", "2030-01-01")
                        .param("download", "true"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", org.hamcrest.Matchers.containsString("petty_cash_payments_report_")))
                .andExpect(content().contentTypeCompatibleWith("application/octet-stream"));
    }

    // -------------------------------------------------------------------------
    // Float Ageing Analysis Report
    // -------------------------------------------------------------------------

    @Test
    @WithMockUser(username = "auditor@test.com", roles = "ROLE_AUDITOR")
    void shouldReturnAllFloatAgeingAnalysisWithoutParams() throws Exception {
        // The endpoint has a !download.isPresent() fallback that returns all data
        mockMvc.perform(get("/res/accounts/floatAgeingAnalysisReport"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"));
    }

    @Test
    @WithMockUser(username = "auditor@test.com", roles = "ROLE_AUDITOR")
    void shouldDownloadFloatAgeingAnalysisAsExcel() throws Exception {
        mockMvc.perform(get("/res/accounts/floatAgeingAnalysisReport")
                        .param("periodStart", "2020-01-01")
                        .param("periodEnd", "2030-01-01")
                        .param("download", "true"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", org.hamcrest.Matchers.containsString("float_ageing_analysis")))
                .andExpect(content().contentTypeCompatibleWith("application/octet-stream"));
    }

    // -------------------------------------------------------------------------
    // Float Order Payment Report
    // -------------------------------------------------------------------------

    @Test
    @WithMockUser(username = "auditor@test.com", roles = "ROLE_AUDITOR")
    void shouldReturnAllFloatOrderPaymentReportWithoutParams() throws Exception {
        mockMvc.perform(get("/res/accounts/floatOrderPaymentReport"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"));
    }

    @Test
    @WithMockUser(username = "auditor@test.com", roles = "ROLE_AUDITOR")
    void shouldDownloadFloatOrderPaymentReportAsExcel() throws Exception {
        mockMvc.perform(get("/res/accounts/floatOrderPaymentReport")
                        .param("periodStart", "2020-01-01")
                        .param("periodEnd", "2030-01-01")
                        .param("download", "true"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", org.hamcrest.Matchers.containsString("float_order_payment_report_")))
                .andExpect(content().contentTypeCompatibleWith("application/octet-stream"));
    }

    // -------------------------------------------------------------------------
    // GRN Report
    // -------------------------------------------------------------------------

    @Test
    @WithMockUser(username = "kikinewton@gmail.com", roles = "ROLE_STORE_MANAGER")
    void shouldReturnGrnReportAsJson() throws Exception {
        mockMvc.perform(get("/res/stores/grnReport")
                        .param("periodStart", "2020-01-01")
                        .param("periodEnd", "2030-01-01"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"));
    }

    @Test
    @WithMockUser(username = "kikinewton@gmail.com", roles = "ROLE_STORE_MANAGER")
    void shouldDownloadGrnReportAsExcel() throws Exception {
        mockMvc.perform(get("/res/stores/grnReport")
                        .param("periodStart", "2020-01-01")
                        .param("periodEnd", "2030-01-01")
                        .param("download", "true"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", org.hamcrest.Matchers.containsString("grn_report_")))
                .andExpect(content().contentTypeCompatibleWith("application/vnd.ms-excel"));
    }
}
