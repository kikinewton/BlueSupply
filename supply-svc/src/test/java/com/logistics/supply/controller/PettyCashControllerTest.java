package com.logistics.supply.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.logistics.supply.common.annotations.IntegrationTest;
import com.logistics.supply.fixture.PettyCashFixture;
import com.logistics.supply.model.PettyCash;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@IntegrationTest
class PettyCashControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    private final String ACTIVE_EMAIL = "kikinewton@gmail.com";
    @Test
    @WithMockUser(username = ACTIVE_EMAIL)
    void shouldGetPettyCashForEmployee() throws Exception {

        mockMvc.perform(get("/api/pettyCashForEmployee"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.message").value("FETCH SUCCESSFUL"));
    }

    @Test
    @WithMockUser
    void shouldFindAllPettyCashOrder() throws Exception {

        mockMvc.perform(get("/api/pettyCashOrders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.message").value("FETCH SUCCESSFUL"));
      }


      @Test
      @WithMockUser(username = ACTIVE_EMAIL, roles = "HOD")
      void shouldFetchDepartmentRelatedPettyCash() throws Exception {

          mockMvc.perform(get("/api/pettyCashByDepartment"))
                  .andExpect(status().isOk())
                  .andExpect(jsonPath("$.status").value("SUCCESS"))
                  .andExpect(jsonPath("$.message").value("FETCH SUCCESSFUL"));
      }


    @Test
    @WithMockUser(username = ACTIVE_EMAIL, roles = "HOD")
      void shouldEndorsePettyCash() throws Exception {

        PettyCash pettyCash = PettyCashFixture.getPettyCash(100);
        List<PettyCash> pettyCashList = Collections.singletonList(pettyCash);
        String content = objectMapper.writeValueAsString(pettyCashList);

        mockMvc.perform(put("/api/bulkPettyCash/endorse").contentType(MediaType.APPLICATION_JSON).content(content))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.message").value("PETTY CASH ENDORSED"));
      }

    @Test
    @WithMockUser(username = ACTIVE_EMAIL, roles = "GENERAL_MANAGER")
    void shouldApprovePettyCash() throws Exception {

        PettyCash pettyCash = PettyCashFixture.getPettyCash(101);
        List<PettyCash> pettyCashList = Collections.singletonList(pettyCash);
        String content = objectMapper.writeValueAsString(pettyCashList);

        mockMvc.perform(put("/api/bulkPettyCash/approve").contentType(MediaType.APPLICATION_JSON).content(content))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.message").value("FLOAT APPROVED"));
    }
}