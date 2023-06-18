package com.logistics.supply.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.logistics.supply.common.annotations.IntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;



@IntegrationTest
class EmployeeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired private ObjectMapper objectMapper;

    @Test
    @WithMockUser
    void shouldFailForNonExistentEmployeeId() throws Exception {

        mockMvc.perform(get("/api/employees/1002")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldEnableEmployeeStatus() throws Exception {

        mockMvc.perform(put("/api/changeActiveState/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.message").value("STATUS CHANGE SUCCESSFUL"));
    }

    @Test
    @WithMockUser
    void shouldGetAllEmployees() throws Exception {
        mockMvc.perform(get("/api/employees")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.data", hasSize(1)));
    }

    void shouldUpdateEmployee() throws Exception {
        mockMvc.perform(put("/qpi/employee/1"))
    }
}