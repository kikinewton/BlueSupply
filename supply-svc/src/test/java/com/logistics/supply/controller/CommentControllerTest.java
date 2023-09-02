package com.logistics.supply.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.logistics.supply.common.annotations.IntegrationTest;
import com.logistics.supply.dto.CommentDto;
import com.logistics.supply.enums.RequestProcess;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@IntegrationTest
class CommentControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Test
    @WithMockUser(username = "chulk@mail.com")
    void shouldAddCommentOnQuotation() throws Exception {

        int itemId = 111;
        CommentDto testComment = new CommentDto(
                "Test Comment",
                RequestProcess.REVIEW_QUOTATION_AUDITOR);
        String content = objectMapper.writeValueAsString(testComment);

        mockMvc.perform(post("/api/comments/QUOTATION_COMMENT/{itemId}", itemId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(content)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("COMMENT SAVED SUCCESSFULLY") )
                .andExpect(jsonPath("$.data.description").value("Test Comment"));
    }
}