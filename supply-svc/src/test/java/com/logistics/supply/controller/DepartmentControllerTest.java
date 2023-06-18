package com.logistics.supply.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.logistics.supply.common.annotations.IntegrationTest;
import com.logistics.supply.dto.DepartmentDto;
import com.logistics.supply.fixture.DepartmentDtoFixture;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@IntegrationTest
class AddDepartmentTest {

  @Autowired private MockMvc mockMvc;

  @Autowired private ObjectMapper objectMapper;

  @Test
  @WithMockUser
  void shouldFailForNonExistentDepartmentId() throws Exception {
    mockMvc.perform(get("/api/departments/9009"))
            .andExpect(status().isNotFound());
  }

  @Test
  @WithMockUser
  void addDepartment() throws Exception {
    DepartmentDto departmentDto = DepartmentDtoFixture.getDepartmentDto("IT", "IT related");
    mockMvc
        .perform(
            post("/api/departments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(departmentDto)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("SUCCESS"))
        .andExpect(jsonPath("$.message").value("DEPARTMENT ADDED"));
  }
}
