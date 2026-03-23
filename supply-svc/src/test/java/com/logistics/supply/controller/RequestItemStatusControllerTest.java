package com.logistics.supply.controller;

import com.logistics.supply.common.annotations.IntegrationTest;
import com.logistics.supply.enums.RequestReview;
import com.logistics.supply.fixture.RequestItemFixture;
import com.logistics.supply.fixture.LocalPurchaseOrderFixture;
import com.logistics.supply.model.RequestItem;
import com.logistics.supply.repository.LocalPurchaseOrderRepository;
import com.logistics.supply.repository.RequestItemRepository;
import org.junit.jupiter.api.Disabled;
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

    @Autowired
    LocalPurchaseOrderRepository localPurchaseOrderRepository;

    @Test
    @WithMockUser
    void shouldReturn404WhenRequestItemDoesNotExist() throws Exception {
        mockMvc.perform(get(STATUS_URL, 9999))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    void shouldReturnEarlyStageWhenRequestItemNotApproved() throws Exception {

        mockMvc.perform(get(STATUS_URL, 101))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.data.approval").value("PENDING"))
                .andExpect(jsonPath("$.data.lpoIssued").doesNotExist())
                .andExpect(jsonPath("$.data.grnIssued").doesNotExist());
    }

    @Test
    @WithMockUser
    @Disabled
    void shouldReturnLpoStageForApprovedRequestItemWithLpoOnly() throws Exception {

        RequestItem saved = requestItemRepository.save(
                RequestItemFixture
                        .endorsed()   // HOD endorses
                        .processed()  // Procurement processes
                        .hodReview()  // HOD reviews quotation
                        .approved()   // GM approves
                        .build()
        );

        localPurchaseOrderRepository.save(
                LocalPurchaseOrderFixture.approved(saved).build());

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
    @Disabled
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
                .andExpect(jsonPath( "$.data.requestReviewDate").isNotEmpty());
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
