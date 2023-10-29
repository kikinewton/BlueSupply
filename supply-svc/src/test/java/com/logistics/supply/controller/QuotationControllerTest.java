package com.logistics.supply.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.logistics.supply.common.annotations.IntegrationTest;
import com.logistics.supply.dto.CreateQuotationRequest;
import com.logistics.supply.dto.GeneratedQuoteDto;
import com.logistics.supply.dto.MapQuotationsToRequestItemsDto;
import com.logistics.supply.fixture.CreateQuotationRequestFixture;
import com.logistics.supply.fixture.GeneratedQuoteDtoFixture;
import com.logistics.supply.fixture.MapQuotationsToRequestItemsDtoFixture;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
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

        mockMvc.perform(post("/api/quotations")
                        .content(content).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.message")
                        .value("QUOTATION ASSIGNED TO REQUEST ITEMS"));
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
                .andExpect(jsonPath("$.message")
                        .value("FETCH ALL QUOTATIONS LINKED NOT LINKED TO LPO"));
    }

    @Test
    @WithMockUser(username = "kikinewton@gmail.com", roles = "AUDITOR")
    void shouldFetchQuotationsUnderReview() throws Exception {

        mockMvc.perform(get("/api/quotations/underReview"))
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

        mockMvc.perform(get("/api/quotations/underReview"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.message").value("FETCH SUCCESSFUL"));
    }

    @Test
    @WithMockUser(username = "kikinewton@gmail.com", roles = "HOD")
    void shouldFetchQuotationsRequiringHodApproval() throws Exception {

        mockMvc.perform(get("/api/quotations/underReview"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.message").value("FETCH SUCCESSFUL"));
    }

    @Test
    @WithMockUser(username = "eric.mensah@blueskies.com", roles = "PROCUREMENT_MANAGER")
    void shouldFetchQuotationsWithAuditorComments() throws Exception {

        mockMvc.perform(get("/api/quotations/underReview"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.message").value("FETCH SUCCESSFUL"))
                .andExpect(jsonPath("$.data[0].quotation.id").value(111));
    }


    @Test
    @WithMockUser(username = "kikinewton@gmail.com", roles = "AUDITOR")
    void shouldFailToApproveQuotationByAuditorWhenPayloadIsEmpty() throws Exception {

        List<Integer> quotationIds = Collections.EMPTY_LIST;
        String content = objectMapper.writeValueAsString(quotationIds);

        mockMvc.perform(put("/api/quotations/approvals")
                        .content(content).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.message")
                        .value("com.logistics.supply.controller.QuotationController " +
                               "approveBatchOfQuotations.quotationIds: Quotation Id is empty"));
    }

    @Test
    @WithMockUser(username = "kikinewton@gmail.com", roles = "AUDITOR")
    void shouldFailApproveQuotationByAuditorWhenHodReviewIsFalse() throws Exception {

        List<Integer> quotationIds = List.of(1);
        String content = objectMapper.writeValueAsString(quotationIds);

        mockMvc.perform(put("/api/quotations/approvals")
                        .content(content).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.message")
                        .value("No quotations were updated because hod endorse is false for all IDs"));
    }


    @Test
    @WithMockUser(username = "kikinewton@gmail.com", roles = "AUDITOR")
    void shouldApproveQuotationByAuditor() throws Exception {

        List<Integer> quotationIds = List.of(110);
        String content = objectMapper.writeValueAsString(quotationIds);

        mockMvc.perform(put("/api/quotations/approvals")
                        .content(content).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.message").value("FETCH SUCCESSFUL"));
    }

    @Test
    @WithMockUser(username = "kikinewton@gmail.com", roles = "PROCUREMENT_OFFICER")
    void shouldAssignQuotationsToRequestItems() throws Exception {


        MapQuotationsToRequestItemsDto mapQuotationsToRequestItemsDto =
                MapQuotationsToRequestItemsDtoFixture.getMapQuotationsToRequestItemsDto();
        String content = objectMapper.writeValueAsString(mapQuotationsToRequestItemsDto);

        mockMvc.perform(put("/api/quotations/assignToRequestItems")
                        .content(content).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.message").value("QUOTATION ASSIGNMENT SUCCESSFUL"));
    }
}