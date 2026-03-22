package com.logistics.supply.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.logistics.supply.common.annotations.IntegrationTest;
import com.logistics.supply.dto.RequestCategoryDto;
import com.logistics.supply.fixture.RequestCategoryDtoFixture;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@IntegrationTest
class RequestCategoryControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Test
    @WithMockUser
    void testGetAllRequestCategories() throws Exception {

        mockMvc.perform(get("/api/requestCategories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.message").value("FETCH SUCCESSFUL"));
    }

    @Test
    @WithMockUser(roles = "PROCUREMENT_OFFICER")
    void shouldCreateRequestCategory() throws Exception {

        RequestCategoryDto requestCategoryDto = RequestCategoryDtoFixture.withDefaults()
                .withName("Mechanical ")
                .withDescription("Mechanical duties")
                .build();
        String content = objectMapper.writeValueAsString(requestCategoryDto);

        mockMvc.perform(post("/api/requestCategories").contentType(MediaType.APPLICATION_JSON).content(content))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.message").value("REQUEST CATEGORY CREATED"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldUpdateRequestCategory() throws Exception {

        RequestCategoryDto requestCategoryDto = RequestCategoryDtoFixture.withDefaults()
                .withName("Bagel")
                .withDescription("Snacks")
                .build();
        String content = objectMapper.writeValueAsString(requestCategoryDto);
        int requestCategoryId = 100;

        mockMvc.perform(put("/api/requestCategories/{requestCategoryId}", requestCategoryId).contentType(MediaType.APPLICATION_JSON).content(content))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.message").value("UPDATE SUCCESSFUL"));
    }
}