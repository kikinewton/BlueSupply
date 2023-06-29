package com.logistics.supply.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.logistics.supply.common.annotations.IntegrationTest;
import com.logistics.supply.dto.ItemUpdateDto;
import com.logistics.supply.fixture.ItemUpdateDtoFixture;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@IntegrationTest
 class RequestItemControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Test
    @WithMockUser
    void shouldGetItemStatus() throws Exception {

        String requestItemId = "100";
        mockMvc.perform(get("/api/requestItems/{requestItemId}", requestItemId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"));
    }

    @Test
    @WithMockUser(username = "chulk@mail.com", roles = "HOD")
    void shouldGetItemsForHOD() throws Exception {

        mockMvc.perform(get("/api/requestItems/departmentHistory"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"));

    }


    @Test
    void shouldFetchListOfRequestItems() throws Exception {

        mockMvc.perform(get("/api/requestItems/departmentHistory"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"));

    }

    @Test
    @WithMockUser(username = "kikinewton@gmail.com")
    void shouldUpdateQuantityForNotEndorsedRequest() throws Exception {

        ItemUpdateDto itemUpdateDto = ItemUpdateDtoFixture.getItemUpdateDto();
        String content = objectMapper.writeValueAsString(itemUpdateDto);
        int requestItemId = 102;

        mockMvc.perform(put("/api/requestItems/{requestItemId}", requestItemId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(content))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"));

    }
}
