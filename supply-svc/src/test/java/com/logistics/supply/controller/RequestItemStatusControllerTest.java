package com.logistics.supply.controller;

import com.logistics.supply.common.annotations.IntegrationTest;
import com.logistics.supply.enums.RequestReview;
import com.logistics.supply.fixture.RequestItemFixture;
import com.logistics.supply.model.RequestItem;
import com.logistics.supply.repository.RequestItemRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@IntegrationTest
class RequestItemStatusControllerTest {

    private static final String STATUS_URL = "/api/requestItems/{requestItemId}/status";

    @Autowired
    MockMvc mockMvc;

    @Autowired
    RequestItemRepository requestItemRepository;

    @Test
    @WithMockUser
    void shouldReturn404WhenRequestItemDoesNotExist() throws Exception {
        mockMvc.perform(get(STATUS_URL, 9999))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    void shouldReturnEarlyStageWhenRequestItemNotApproved() throws Exception {

        RequestItem requestItem = RequestItemFixture.endorsed().build();
        RequestItem saved = requestItemRepository.save(requestItem);
        // Request item 100 has approval=PENDING — no LPO should be linked
        mockMvc.perform(get(STATUS_URL, saved.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.data.approval").value("PENDING"))
                .andExpect(jsonPath("$.data.lpoIssued").doesNotExist())
                .andExpect(jsonPath("$.data.grnIssued").doesNotExist());
    }

    @Test
    @WithMockUser
    void shouldReturnLpoStageForApprovedRequestItemWithLpoOnly() throws Exception {

        RequestItem requestItem = RequestItemFixture
                .endorsed()
                .processed()
                .approved()
                .build();

        RequestItem saved = requestItemRepository.save(requestItem);
        // Request item 105: approval=APPROVED, linked to LPO 100, no GRN
        mockMvc.perform(get(STATUS_URL, saved.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.data.approval").value("APPROVED"))
                .andExpect(jsonPath("$.data.lpoIssued").value("LPO ISSUED"))
                .andExpect(jsonPath("$.data.lpoIssuedDate").isNotEmpty())
                .andExpect(jsonPath("$.data.grnIssued").doesNotExist());
    }

    @Test
    @WithMockUser
    void shouldReturnGrnStageForRequestItemWithHodApprovedGrn() throws Exception {
        // Request item 106: approval=APPROVED, LPO 101, GRN 101 with HOD approval
        mockMvc.perform(get(STATUS_URL, 106))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.data.lpoIssued").value("LPO ISSUED"))
                .andExpect(jsonPath("$.data.grnIssued").value("GRN ISSUED"))
                .andExpect(jsonPath("$.data.grnIssuedDate").isNotEmpty())
                .andExpect(jsonPath("$.data.grnHodEndorse").value("GRN HOD ENDORSED"))
                .andExpect(jsonPath("$.data.grnHodEndorseDate").isNotEmpty())
                .andExpect(jsonPath("$.data.paymentInitiated").doesNotExist());
    }

    @Test
    @WithMockUser
    void shouldReturnRequestReviewDateWhenRequestItemHasBeenReviewed() throws Exception {
        RequestItem requestItem = RequestItemFixture
                .endorsed()
                .reviewed(RequestReview.HOD_REVIEW)
                .build();
        RequestItem saved = requestItemRepository.save(requestItem);

        mockMvc.perform(get(STATUS_URL, saved.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.data.requestReview").value("HOD_REVIEW"))
                .andExpect(jsonPath("$.data.requestReviewDate").isNotEmpty());
    }

    @Test
    @WithMockUser
    void shouldReturnPaymentInProgressStageForRequestItemWithDraftPayment() throws Exception {
        // Request item 107: approval=APPROVED, LPO 102, GRN 102, payment 101 (DRAFT)
        mockMvc.perform(get(STATUS_URL, 107))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.data.lpoIssued").value("LPO ISSUED"))
                .andExpect(jsonPath("$.data.grnIssued").value("GRN ISSUED"))
                .andExpect(jsonPath("$.data.paymentInitiated").value("ACCOUNT INITIATED PAYMENT"))
                .andExpect(jsonPath("$.data.paymentInitiatedDate").isNotEmpty())
                // Payment is DRAFT — auditor/FM/GM approvals not yet set
                .andExpect(jsonPath("$.data.paymentAuditorCheck").doesNotExist())
                .andExpect(jsonPath("$.data.paymentFMAuthorise").doesNotExist())
                .andExpect(jsonPath("$.data.paymentGMApprove").doesNotExist());
    }
}
