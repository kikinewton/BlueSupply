package com.logistics.supply.controller;

import com.logistics.supply.common.annotations.IntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@IntegrationTest
class RoleControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Test
    @WithMockUser
    void listAllRoles() throws Exception {
        mockMvc.perform(get("/api/roles"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"));
    }

    @Test
    @WithMockUser
    void getRoleById() throws Exception {
        int roleId = 1;
        mockMvc.perform(get("/api/roles/{roleId}", roleId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"));;
    }
}