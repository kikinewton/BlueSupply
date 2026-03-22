package com.logistics.supply.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.logistics.supply.common.annotations.IntegrationTest;
import com.logistics.supply.dto.BulkRequestItemDto;
import com.logistics.supply.dto.FloatOrPettyCashDto;
import com.logistics.supply.dto.MultipleItemDto;
import com.logistics.supply.fixture.BulkRequestItemDtoFixture;
import com.logistics.supply.fixture.FloatOrPettyCashDtoFixture;
import com.logistics.supply.fixture.MultipleItemDtoFixture;
import com.logistics.supply.fixture.RequestItemFixture;
import com.logistics.supply.model.RequestItem;
import com.logistics.supply.repository.RequestItemRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.List;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.anonymous;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@IntegrationTest
class MultiplierItemsControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private RequestItemRepository requestItemRepository;

    private static final String HOD_EMAIL = "chulk@mail.com";
    private static final String GM_EMAIL = "gm@test.com";
    private static final String REGULAR_EMAIL = "kikinewton@gmail.com";

    // -------------------------------------------------------------------------
    // POST /api/multipleRequestItems
    // -------------------------------------------------------------------------

    @Test
    @WithMockUser(username = REGULAR_EMAIL)
    void shouldCreateMultipleRequestItems() throws Exception {
        MultipleItemDto body = MultipleItemDtoFixture.getMultipleGoodsRequestItems();

        mockMvc.perform(post("/api/multipleRequestItems")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.message").value("CREATED REQUEST ITEMS"))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    void shouldReturn401_whenUnauthenticated_forCreateMultipleRequestItems() throws Exception {
        MultipleItemDto body = MultipleItemDtoFixture.getMultipleGoodsRequestItems();

        mockMvc.perform(post("/api/multipleRequestItems")
                        .with(anonymous())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = REGULAR_EMAIL)
    void shouldReturn400_whenMultipleRequestItemsListIsEmpty() throws Exception {
        MultipleItemDto body = new MultipleItemDto(Collections.emptyList());

        mockMvc.perform(post("/api/multipleRequestItems")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest());
    }

    // -------------------------------------------------------------------------
    // POST /api/bulkFloat
    // -------------------------------------------------------------------------

    @Test
    @WithMockUser(username = REGULAR_EMAIL)
    void shouldCreateBulkFloat() throws Exception {
        FloatOrPettyCashDto body = FloatOrPettyCashDtoFixture.getBulkFloat();

        mockMvc.perform(post("/api/bulkFloat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.message").value("CREATED FLOAT ITEMS"));
    }

    @Test
    void shouldReturn401_whenUnauthenticated_forBulkFloat() throws Exception {
        FloatOrPettyCashDto body = FloatOrPettyCashDtoFixture.getBulkFloat();

        mockMvc.perform(post("/api/bulkFloat")
                        .with(anonymous())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isUnauthorized());
    }

    // -------------------------------------------------------------------------
    // POST /api/bulkPettyCash
    // -------------------------------------------------------------------------

    @Test
    @WithMockUser(username = REGULAR_EMAIL)
    void shouldCreateBulkPettyCash() throws Exception {
        FloatOrPettyCashDto body = FloatOrPettyCashDtoFixture.getBulkPettyCash();

        mockMvc.perform(post("/api/bulkPettyCash")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.message").value("CREATED PETTY CASH ITEMS"));
    }

    @Test
    void shouldReturn401_whenUnauthenticated_forBulkPettyCash() throws Exception {
        FloatOrPettyCashDto body = FloatOrPettyCashDtoFixture.getBulkPettyCash();

        mockMvc.perform(post("/api/bulkPettyCash")
                        .with(anonymous())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isUnauthorized());
    }

    // -------------------------------------------------------------------------
    // PUT /api/requestItems/bulkEndorse  (ROLE_HOD only)
    // -------------------------------------------------------------------------

    @Test
    @WithMockUser(username = HOD_EMAIL, roles = "HOD")
    void shouldEndorseBulkRequestItems() throws Exception {
        BulkRequestItemDto body = BulkRequestItemDtoFixture.getBulkRequestItemDto();

        mockMvc.perform(put("/api/requestItems/bulkEndorse")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.message").value("REQUEST ENDORSED"));
    }

    @Test
    @WithMockUser(username = REGULAR_EMAIL, roles = "REGULAR")
    void shouldReturn403_whenNotHod_forBulkEndorse() throws Exception {
        BulkRequestItemDto body = BulkRequestItemDtoFixture.getBulkRequestItemDto();

        mockMvc.perform(put("/api/requestItems/bulkEndorse")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldReturn401_whenUnauthenticated_forBulkEndorse() throws Exception {
        BulkRequestItemDto body = BulkRequestItemDtoFixture.getBulkRequestItemDto();

        mockMvc.perform(put("/api/requestItems/bulkEndorse")
                        .with(anonymous())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isUnauthorized());
    }

    // -------------------------------------------------------------------------
    // PUT /api/requestItems/bulkApprove  (ROLE_GENERAL_MANAGER only)
    // -------------------------------------------------------------------------

    @Test
    @WithMockUser(username = GM_EMAIL, roles = "GENERAL_MANAGER")
    void shouldApproveBulkRequestItems() throws Exception {
        // item 101: endorsement=ENDORSED, approval=PENDING — ready for GM approval
        RequestItem item = RequestItemFixture.processed().endorsed().build();
        requestItemRepository.save(item);
        BulkRequestItemDto bulkRequestItemDto = new BulkRequestItemDto();
        bulkRequestItemDto.setRequestItems(List.of(item));

        mockMvc.perform(put("/api/requestItems/bulkApprove")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bulkRequestItemDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.message").value("REQUEST APPROVED"));
    }

    @Test
    @WithMockUser(username = HOD_EMAIL, roles = "HOD")
    void shouldReturn403_whenNotGm_forBulkApprove() throws Exception {
        BulkRequestItemDto body = BulkRequestItemDtoFixture.getBulkRequestItemDto();

        mockMvc.perform(put("/api/requestItems/bulkApprove")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldReturn401_whenUnauthenticated_forBulkApprove() throws Exception {
        BulkRequestItemDto body = BulkRequestItemDtoFixture.getBulkRequestItemDto();

        mockMvc.perform(put("/api/requestItems/bulkApprove")
                        .with(anonymous())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isUnauthorized());
    }

    // -------------------------------------------------------------------------
    // PUT /api/requestItems/bulkHodReview  (ROLE_HOD only)
    // -------------------------------------------------------------------------

    @Test
    @WithMockUser(username = HOD_EMAIL, roles = "HOD")
    void shouldHodReviewBulkRequestItems() throws Exception {
        BulkRequestItemDto body = BulkRequestItemDtoFixture.getBulkRequestItemDto();

        mockMvc.perform(put("/api/requestItems/bulkHodReview")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.message").value("HOD REVIEW SUCCESSFUL"));
    }

    @Test
    @WithMockUser(username = REGULAR_EMAIL, roles = "REGULAR")
    void shouldReturn403_whenNotHod_forBulkHodReview() throws Exception {
        BulkRequestItemDto body = BulkRequestItemDtoFixture.getBulkRequestItemDto();

        mockMvc.perform(put("/api/requestItems/bulkHodReview")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldReturn401_whenUnauthenticated_forBulkHodReview() throws Exception {
        BulkRequestItemDto body = BulkRequestItemDtoFixture.getBulkRequestItemDto();

        mockMvc.perform(put("/api/requestItems/bulkHodReview")
                        .with(anonymous())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isUnauthorized());
    }

    // -------------------------------------------------------------------------
    // PUT /api/requestItems/bulkCancel  (ROLE_HOD or ROLE_GENERAL_MANAGER)
    // -------------------------------------------------------------------------

    @Test
    @WithMockUser(username = HOD_EMAIL, roles = "HOD")
    void shouldCancelBulkRequestItems_asHod() throws Exception {
        // item 103: belongs to dept 10 (Culinary) which has chulk@mail.com as HOD

        BulkRequestItemDto body = BulkRequestItemDtoFixture.getBulkRequestItemDto();

        mockMvc.perform(put("/api/requestItems/bulkCancel")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.message").value("CANCELLED REQUEST"));
    }

    @Test
    @WithMockUser(username = GM_EMAIL, roles = "GENERAL_MANAGER")
    void shouldCancelBulkRequestItems_asGm() throws Exception {
        // item 103: belongs to dept 10 which has a HOD — cancel is valid from GM too
        RequestItem item = RequestItemFixture.pending().build();
        item.setId(103);
        BulkRequestItemDto body = new BulkRequestItemDto();
        body.setRequestItems(List.of(item));

        mockMvc.perform(put("/api/requestItems/bulkCancel")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.message").value("CANCELLED REQUEST"));
    }

    @Test
    @WithMockUser(username = REGULAR_EMAIL, roles = "REGULAR")
    void shouldReturn403_whenNotHodOrGm_forBulkCancel() throws Exception {
        BulkRequestItemDto body = BulkRequestItemDtoFixture.getBulkRequestItemDto();

        mockMvc.perform(put("/api/requestItems/bulkCancel")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldReturn401_whenUnauthenticated_forBulkCancel() throws Exception {
        BulkRequestItemDto body = BulkRequestItemDtoFixture.getBulkRequestItemDto();

        mockMvc.perform(put("/api/requestItems/bulkCancel")
                        .with(anonymous())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isUnauthorized());
    }
}