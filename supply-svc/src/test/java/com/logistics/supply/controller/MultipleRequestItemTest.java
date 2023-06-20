package com.logistics.supply.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.logistics.supply.common.annotations.IntegrationTest;
import com.logistics.supply.dto.FloatOrPettyCashDto;
import com.logistics.supply.dto.MultipleItemDto;
import com.logistics.supply.enums.ProcurementType;
import com.logistics.supply.fixture.FloatOrPettyCashDtoFixture;
import com.logistics.supply.fixture.MultipleItemDtoFixture;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@IntegrationTest
class MultipleRequestItemTest {

  @Autowired MockMvc mockMvc;

  @Autowired ObjectMapper objectMapper;

  private final String ACTIVE_EMAIL = "kikinewton@gmail.com";

  @Test
  @WithMockUser(username = ACTIVE_EMAIL)
  void shouldAddMultipleRequestItems() throws Exception {

    MultipleItemDto goodsRequestItems = MultipleItemDtoFixture.getMultipleGoodsRequestItems();
    String content = objectMapper.writeValueAsString(goodsRequestItems);

    mockMvc
        .perform(
            post("/api/multipleRequestItems")
                .contentType(MediaType.APPLICATION_JSON)
                .content(content))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("SUCCESS"))
        .andExpect(jsonPath("$.message").value("CREATED REQUEST ITEMS"));
  }

  @Test
  @WithMockUser(username = ACTIVE_EMAIL)
  void shouldAddBulkFloat() throws Exception {
    FloatOrPettyCashDto bulkFloat = FloatOrPettyCashDtoFixture.getBulkFloat();
    ProcurementType aFloat = ProcurementType.FLOAT;
    String content = objectMapper.writeValueAsString(bulkFloat);

    mockMvc.perform(post("/api/bulkFloatOrPettyCash/%s".formatted(aFloat))
            .contentType(MediaType.APPLICATION_JSON)
            .content(content))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("SUCCESS"))
            .andExpect(jsonPath("$.message").value("CREATED FLOAT ITEMS"));
  }

  @Test
  @WithMockUser(username = ACTIVE_EMAIL)
  void shouldAddBulkPettyCash() throws Exception {

  }


}
