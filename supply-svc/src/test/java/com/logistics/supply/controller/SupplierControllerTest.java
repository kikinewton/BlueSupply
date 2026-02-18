package com.logistics.supply.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.logistics.supply.common.annotations.IntegrationTest;
import com.logistics.supply.dto.SupplierDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests for SupplierController.
 * Suppliers id=1 (Jilorm Ventures, unregistered) and id=2 (Ginet Technology, registered)
 * are seeded in init_script.sql.
 */
@IntegrationTest
class SupplierControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    private SupplierDto buildSupplierDto(String name) {
        SupplierDto dto = new SupplierDto();
        dto.setName(name);
        dto.setDescription("Test supplier");
        dto.setPhoneNo("0551234567");
        dto.setLocation("Accra");
        dto.setRegistered(false);
        return dto;
    }

    @Test
    @WithMockUser(username = "kikinewton@gmail.com", roles = "ROLE_PROCUREMENT_MANAGER")
    void shouldCreateSupplier() throws Exception {
        SupplierDto dto = buildSupplierDto("NewTestSupplier");

        mockMvc.perform(post("/api/suppliers")
                        .content(objectMapper.writeValueAsString(dto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.message").value("SUPPLIER CREATED SUCCESSFULLY"))
                .andExpect(jsonPath("$.data.name").value("NewTestSupplier"));
    }

    @Test
    @WithMockUser(username = "kikinewton@gmail.com", roles = "ROLE_PROCUREMENT_MANAGER")
    void shouldFetchAllSuppliers() throws Exception {
        mockMvc.perform(get("/api/suppliers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    @WithMockUser(username = "kikinewton@gmail.com", roles = "ROLE_PROCUREMENT_MANAGER")
    void shouldFetchSupplierById() throws Exception {
        mockMvc.perform(get("/api/suppliers/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.name").value("Jilorm Ventures"));
    }

    @Test
    @WithMockUser(username = "kikinewton@gmail.com", roles = "ROLE_PROCUREMENT_MANAGER")
    void shouldFetchUnregisteredSuppliers() throws Exception {
        // Supplier id=1 is unregistered (registered=false)
        mockMvc.perform(get("/api/suppliers")
                        .param("unRegisteredSuppliers", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.data[0].registered").value(false));
    }

    @Test
    @WithMockUser(username = "kikinewton@gmail.com", roles = "ROLE_PROCUREMENT_MANAGER")
    void shouldUpdateSupplier() throws Exception {
        SupplierDto dto = buildSupplierDto("Jilorm Ventures Updated");

        mockMvc.perform(put("/api/suppliers/1")
                        .content(objectMapper.writeValueAsString(dto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.message").value("UPDATE SUCCESSFUL"))
                .andExpect(jsonPath("$.data.name").value("Jilorm Ventures Updated"));
    }

    @Test
    @WithMockUser(username = "kikinewton@gmail.com", roles = "ROLE_PROCUREMENT_MANAGER")
    void shouldDeleteSupplier() throws Exception {
        // Create first to avoid affecting other tests that rely on supplier id=1/2
        SupplierDto dto = buildSupplierDto("DeleteTestSupplier");
        String response = mockMvc.perform(post("/api/suppliers")
                        .content(objectMapper.writeValueAsString(dto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        int newId = objectMapper.readTree(response).at("/data/id").asInt();

        mockMvc.perform(delete("/api/suppliers/{id}", newId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("SUPPLIER DELETED"));
    }
}
