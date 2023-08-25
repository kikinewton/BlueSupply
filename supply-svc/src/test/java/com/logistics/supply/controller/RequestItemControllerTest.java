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
    @WithMockUser(username = "chulk@mail.com", roles = "HOD")
    void shouldGetItemsForHODWithParams() throws Exception {

        mockMvc.perform(get("/api/requestItemsByDepartment")
                        .param("supplier", "Jil")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.data[0].name").value("Fridge"));
    }

    @Test
    @WithMockUser(username = "chulk@mail.com", roles = "ADMIN")
    void shouldGetAllRequestItems() throws Exception {

        mockMvc.perform(get("/api/requestItems"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.message").value("FETCH SUCCESSFUL"))
                .andExpect(jsonPath("$.meta.pageSize").value(300));
    }

    @Test
    @WithMockUser(username = "chulk@mail.com", roles = "ADMIN")
    void shouldGetAllRequestItemsToBeApproved() throws Exception {

        mockMvc.perform(get("/api/requestItems")
                        .param("toBeApproved", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.message").value("FETCH SUCCESSFUL"))
                .andExpect(jsonPath("$.meta.pageSize").value(300));
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
                .andExpect(jsonPath("$.status").value("SUCCESS"))
        ;

    }

    @Test
    @WithMockUser(username = "kikinewton@gmail.com")
    void listRequestItemsForEmployee() throws Exception{

        mockMvc.perform(get("/api/requestItemsForEmployee"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.message").value("FETCH SUCCESSFUL"));
      }

    @Test
    @WithMockUser(username = "kikinewton@gmail.com", roles = "PROCUREMENT_MANAGER")
    void listAllEndorsedRequestItems() throws Exception {

        mockMvc.perform(get("/api/requestItems/endorsed"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.message").value("ENDORSED REQUEST ITEMS"));
      }

    @Test
    @WithMockUser(username = "kikinewton@gmail.com", roles = "HOD")
    void listEndorsedRequestItemsForDepartment() throws Exception {

        mockMvc.perform(get("/api/requestItemsByDepartment/endorsed"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.message").value("ENDORSED REQUEST ITEM"));

      }

    @Test
    @WithMockUser(username = "kikinewton@gmail.com")
    void shouldTestListRequestItemsForEmployeeWithRequestItemNameAsParameter() throws Exception {

        mockMvc.perform(get("/api/requestItemsForEmployee")
                        .param("requestItemName", "Flap Disc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.message").value("FETCH SUCCESSFUL"))
                .andExpect(jsonPath("$.meta.total").value("1"))
                .andExpect(jsonPath("$.data[0].name").value("Flap Disc"))
                .andExpect(jsonPath("$.data[0].receivingStore.name").value("Engineering store"));
    }
}
