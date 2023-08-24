package com.logistics.supply.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.logistics.supply.common.annotations.IntegrationTest;
import com.logistics.supply.dto.CreateQuotationRequest;
import com.logistics.supply.dto.GeneratedQuoteDto;
import com.logistics.supply.fixture.CreateQuotationRequestFixture;
import com.logistics.supply.fixture.GeneratedQuoteDtoFixture;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@IntegrationTest
class QuotationControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;
    @Test
    @WithMockUser
    void shouldGenerateQuoteForUnRegisteredSupplier() throws Exception {
        GeneratedQuoteDto generatedQuoteDto = GeneratedQuoteDtoFixture.generatedQuoteDto();
        String content = objectMapper.writeValueAsString(generatedQuoteDto);

        mockMvc.perform(post("/api/quotations/generateQuoteForSupplier").content(content)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.message").value("GENERATED QUOTATION"));
    }

    @Test
    @WithMockUser(username = "kikinewton@gmail.com", roles = "PROCUREMENT_MANAGER")
    void shouldTestCreateQuotation() throws Exception {

        CreateQuotationRequest createQuotationRequest = CreateQuotationRequestFixture.getCreateQuotationRequest();
        String content = objectMapper.writeValueAsString(createQuotationRequest);

        mockMvc.perform(post("/api/quotations").content(content).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.message").value("QUOTATION ASSIGNED TO REQUEST ITEMS"));
    }

    @Test
    @WithMockUser(roles = "PROCUREMENT_MANAGER")
    void shouldFetchAllQuotations() throws Exception {

        mockMvc.perform(get("/api/quotations"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.message").value("FETCH SUCCESSFUL"));
    }

    @Test
    @WithMockUser(roles = "PROCUREMENT_MANAGER")
    void shouldFetchQuotationsLinkedToLpo() throws Exception {

        mockMvc.perform(get("/api/quotations/linkedToLpo"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.message").value("FETCH ALL QUOTATIONS"));
    }

    @Test
    @WithMockUser(roles = "PROCUREMENT_MANAGER")
    void shouldFetchQuotationsNotLinkedToLpo() throws Exception {

        mockMvc.perform(get("/api/quotations/notLinkedToLpo"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.message").value("FETCH ALL QUOTATIONS LINKED NOT LINKED TO LPO"));
    }

    @Test
    @WithMockUser(username = "kikinewton@gmail.com", roles = "PROCUREMENT_MANAGER")
    void shouldFetchQuotationsUnderReview() throws Exception {

        mockMvc.perform(get("/api/quotations/underReview")
                        .param("role", "hod"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.message").value("FETCH SUCCESSFUL"));
    }

    @Test
    @WithMockUser(username = "kikinewton@gmail.com", roles = "GENERAL_MANAGER")
    void shouldFetchApprovedQuotations() throws Exception {

        mockMvc.perform(get("/api/quotations/approved"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.message").value("FETCH SUCCESSFUL"));
    }

    @Test
    @WithMockUser
    void findRequestItemsWithoutDocsInQuotation() throws Exception {

        mockMvc.perform(get("/api/requestItems/quotations?withoutDocs=true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"));
    }

    @Test
    @WithMockUser(username = "kikinewton@gmail.com", roles = "AUDITOR")
    void shouldFetchQuotationsRequiringAuditorApproval() throws Exception {

        mockMvc.perform(get("/api/quotations/underReview")
                .param("role", "auditor"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.message").value("FETCH SUCCESSFUL"));

    }
}