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

import static com.logistics.supply.fixture.IdFixture.DEPARTMENT_ID;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@IntegrationTest
class DepartmentControllerTest {

  @Autowired MockMvc mockMvc;

  @Autowired ObjectMapper objectMapper;


  @Test
  @WithMockUser
  void shouldFailForNonExistentDepartmentId() throws Exception {

    mockMvc.perform(get("/api/departments/9009"))
            .andExpect(status().isNotFound());
  }

  @Test
  @WithMockUser
  void shouldGetDepartmentById() throws Exception {

    mockMvc.perform(get("/api/departments/" + DEPARTMENT_ID))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("SUCCESS"))
            .andExpect(jsonPath("$.data.name").value("Culinary"));
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void addDepartment() throws Exception {

    DepartmentDto departmentDto =
            DepartmentDtoFixture.getDepartmentDto("Front desk", "Customer related");

    mockMvc
        .perform(
            post("/api/departments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(departmentDto)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("SUCCESS"))
        .andExpect(jsonPath("$.message").value("DEPARTMENT ADDED"));
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void updateDepartment() throws Exception {

    DepartmentDto departmentDto = DepartmentDtoFixture.getDepartmentDto("ITX", "IT related");
    mockMvc.perform(put("/api/departments/" + DEPARTMENT_ID)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(departmentDto)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("SUCCESS"))
            .andExpect(jsonPath("$.message").value("DEPARTMENT UPDATED"))
            .andExpect(jsonPath("$.data.name").value("ITX"));
  }

  @Test
  @WithMockUser
  void getAllDepartments() throws Exception {

    mockMvc.perform(get("/api/departments")
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("SUCCESS"))
            .andExpect(jsonPath("$.message").value("FETCH DEPARTMENTS"))
            .andExpect(jsonPath("$.data", hasSize(2)));
    }

}
