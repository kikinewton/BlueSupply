package com.logistics.supply.controller;

import com.logistics.supply.common.annotations.IntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import reactor.core.publisher.Flux;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@IntegrationTest
class DashboardControllerTest {

    @Autowired
    MockMvc mockMvc;

    // --- supplier performance ---

    @Test
    @WithMockUser
    void shouldReturnSupplierPerformanceData() throws Exception {
        mockMvc.perform(get("/api/dashboard/supplierPerformance"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.data").isArray());
    }

    // --- payment aging ---

    @Test
    @WithMockUser
    void shouldReturnPaymentAgingData() throws Exception {
        mockMvc.perform(get("/api/dashboard/paymentAging"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.data").isArray());
    }

    // --- cycle time ---

    @Test
    @WithMockUser
    void shouldReturnCycleTimeData() throws Exception {
        mockMvc.perform(get("/api/dashboard/cycleTime"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.data").isArray());
    }

    // --- cancellation rate ---

    @Test
    @WithMockUser
    void shouldReturnCancellationRateData() throws Exception {
        mockMvc.perform(get("/api/dashboard/cancellationRate"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.data").isArray());
    }

    // --- monthly trends ---

    @Test
    @WithMockUser
    void shouldReturnTrendDataWithExplicitMonths() throws Exception {
        mockMvc.perform(get("/api/dashboard/trends").param("months", "3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    @WithMockUser
    void shouldReturnTrendDataWithDefaultMonths() throws Exception {
        mockMvc.perform(get("/api/dashboard/trends"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.data").isArray());
    }

    // --- SSE stream: /api/dashboard/stream ---

    @Test
    @WithMockUser
    void shouldSendInitialDashboardSnapshotOnSubscribe() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/dashboard/stream")
                        .accept(MediaType.TEXT_EVENT_STREAM))
                .andExpect(request().asyncStarted())
                .andReturn();

        // The broadcaster sends a named "dashboard" event synchronously on subscribe
        assertThat(result.getResponse().getContentAsString())
                .contains("event:dashboard");
    }
    // --- auth guard ---

    @Test
    void shouldRequireAuthForSupplierPerformance() throws Exception {
        mockMvc.perform(get("/api/dashboard/supplierPerformance"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldRequireAuthForPaymentAging() throws Exception {
        mockMvc.perform(get("/api/dashboard/paymentAging"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldRequireAuthForCycleTime() throws Exception {
        mockMvc.perform(get("/api/dashboard/cycleTime"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldRequireAuthForCancellationRate() throws Exception {
        mockMvc.perform(get("/api/dashboard/cancellationRate"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldRequireAuthForTrends() throws Exception {
        mockMvc.perform(get("/api/dashboard/trends"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldRequireAuthForDashboardStream() throws Exception {
        mockMvc.perform(get("/api/dashboard/stream"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldRequireAuthForDashboardSummary() throws Exception {
        mockMvc.perform(get("/api/dashboard/summary"))
                .andExpect(status().isUnauthorized());
    }
}
