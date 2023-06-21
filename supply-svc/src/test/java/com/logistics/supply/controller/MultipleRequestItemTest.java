package com.logistics.supply.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.logistics.supply.common.annotations.IntegrationTest;
import com.logistics.supply.dto.BulkRequestItemDto;
import com.logistics.supply.dto.FloatOrPettyCashDto;
import com.logistics.supply.dto.MultipleItemDto;
import com.logistics.supply.fixture.BulkRequestItemDtoFixture;
import com.logistics.supply.fixture.FloatOrPettyCashDtoFixture;
import com.logistics.supply.fixture.MultipleItemDtoFixture;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
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
    String content = objectMapper.writeValueAsString(bulkFloat);

    mockMvc.perform(post("/api/bulkFloat")
            .contentType(MediaType.APPLICATION_JSON)
            .content(content))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("SUCCESS"))
            .andExpect(jsonPath("$.message").value("CREATED FLOAT ITEMS"));
  }

  @Test
  @WithMockUser(username = ACTIVE_EMAIL)
  void shouldAddBulkPettyCash() throws Exception {

    FloatOrPettyCashDto bulkPettyCash = FloatOrPettyCashDtoFixture.getBulkPettyCash();
    String content = objectMapper.writeValueAsString(bulkPettyCash);

    mockMvc.perform(post("/api/bulkPettyCash")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(content))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("SUCCESS"))
            .andExpect(jsonPath("$.message").value("CREATED PETTY CASH ITEMS"));
  }

  @Test
  @WithMockUser(username = "chulk@mail.com", roles = "HOD")
  void shouldEndorseBulkRequestItemsByUpdate() throws Exception {

    BulkRequestItemDto bulkRequestItemDto = BulkRequestItemDtoFixture.getBulkRequestItemDto();
    String content = objectMapper.writeValueAsString(bulkRequestItemDto);

    mockMvc.perform(put("/api/requestItems/updateStatus/ENDORSE")
            .content(content)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("SUCCESS"));
  }


  @Test
  @WithMockUser(username = "chulk@mail.com", roles = "HOD")
  void shouldEndorseBulkRequestItems() throws Exception {

    BulkRequestItemDto bulkRequestItemDto = BulkRequestItemDtoFixture.getBulkRequestItemDto();
    String content = objectMapper.writeValueAsString(bulkRequestItemDto);

    mockMvc.perform(put("/api/requestItems/updateStatus/endorse")
                    .content(content)
                    .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("SUCCESS"));
  }

}
