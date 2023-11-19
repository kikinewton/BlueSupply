package com.logistics.supply.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.logistics.supply.common.annotations.IntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@IntegrationTest
class LpoControllerTest {

    private final String ACTIVE_EMAIL = "kikinewton@gmail.com";
    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Test
    @WithMockUser(username = ACTIVE_EMAIL, roles = "HOD")
    public void shouldFetchLpoDraft() throws Exception {
        mockMvc.perform(get("/api/localPurchaseOrderDrafts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"));
    }

    @Test
    @WithMockUser(username = ACTIVE_EMAIL, roles = "HOD")
    public void shouldFetchLpoDraftUnderHodReview() throws Exception {
        mockMvc.perform(get("/api/localPurchaseOrderDrafts")
                        .param("underReview", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"));
    }
}