package com.logistics.supply.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.logistics.supply.common.annotations.IntegrationTest;
import com.logistics.supply.fixture.ChangePasswordDtoFixture;
import com.logistics.supply.fixture.EmployeeDtoFixture;
import com.logistics.supply.fixture.PasswordResetDtoFixture;
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

  private final String ACTIVE_EMAIL = "kikinewton@gmail.com";
  @Autowired MockMvc mockMvc;

  @Autowired ObjectMapper objectMapper;

  @Test
  @WithMockUser
  void shouldFailForNonExistentEmployeeId() throws Exception {

    mockMvc
        .perform(get("/api/employees/1002").contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isNotFound());
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void shouldEnableEmployeeStatus() throws Exception {
    String employeeId = "100";

    mockMvc
        .perform(put("/api/changeActiveState/{employeeId}", employeeId).contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("SUCCESS"))
        .andExpect(jsonPath("$.message").value("STATUS CHANGE SUCCESSFUL"));
  }

  @Test
  @WithMockUser
  void shouldGetAllEmployees() throws Exception {
    mockMvc
        .perform(get("/api/employees").contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("SUCCESS"))
        .andExpect(jsonPath("$.data", hasSize(3)));
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void shouldUpdateEmployee() throws Exception {

    String employeeDto = objectMapper.writeValueAsString(EmployeeDtoFixture.getEmployeeDto());
    String employeeId = "100";

    mockMvc
        .perform(
            put("/api/employees/{employeeId}", employeeId).contentType(MediaType.APPLICATION_JSON).content(employeeDto))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("SUCCESS"))
        .andExpect(jsonPath("$.message").value("EMPLOYEE UPDATED"));
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void shouldDisableEmployee() throws Exception {

    String employeeId = "100";

    mockMvc
        .perform(put("/api/employees/{employeeId}/disable", employeeId).contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("SUCCESS"))
        .andExpect(jsonPath("$.data.enabled").value(false))
        .andExpect(jsonPath("$.message").value("DISABLE EMPLOYEE"));
  }

  @Test
  @WithMockUser(username = "wrong@gmail.com")
  void shouldFailToChangeEmployeePasswordWithoutAuth() throws Exception {

    mockMvc
        .perform(put("/resetPassword").contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isNotFound());
  }

  @Test
  @WithMockUser(username = ACTIVE_EMAIL)
  void shouldResetPassword() throws Exception {

    mockMvc
        .perform(put("/resetPassword").contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("SUCCESS"))
        .andExpect(jsonPath("$.message").value("VERIFICATION CODE SENT TO EMAIL"));
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void shouldResetPasswordByAdmin() throws Exception {

    mockMvc
        .perform(
            put("/api/admin/employees/2/resetPassword").contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("SUCCESS"))
        .andExpect(jsonPath("$.message").value("VERIFICATION CODE SENT TO EMAIL"));
  }

  @Test
  @WithMockUser
  void shouldResetPasswordConfirmation() throws Exception {

    String content =
        objectMapper.writeValueAsString(
            PasswordResetDtoFixture.getPasswordResetDto("kikinewton@gmail.com", "c2d297-3d0bKd497"));

    mockMvc
        .perform(
            post("/resetPasswordConfirmation")
                .contentType(MediaType.APPLICATION_JSON)
                .content(content))
            .andExpect(status().isOk());
  }

  @Test
  @WithMockUser
  void shouldFailResetPasswordConfirmationIfEmailIsNull() throws Exception {

    String content =
        objectMapper.writeValueAsString(PasswordResetDtoFixture.getPasswordResetDto(null, "c2d297-3d0bKd497"));

    mockMvc
        .perform(
            post("/resetPasswordConfirmation")
                .contentType(MediaType.APPLICATION_JSON)
                .content(content))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.status").value("BAD_REQUEST"))
        .andExpect(jsonPath("$.message").value("Email must not be blank"));
  }

  @Test
  @WithMockUser
  void shouldFailResetPasswordConfirmationIfEmailIsNullAndPasswordEmpty() throws Exception {

    String content =
        objectMapper.writeValueAsString(
            PasswordResetDtoFixture.getPasswordResetDtoWithEmptyPassword(null));

    mockMvc
        .perform(
            post("/resetPasswordConfirmation")
                .contentType(MediaType.APPLICATION_JSON)
                .content(content))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.status").value("BAD_REQUEST"));
  }

  @Test
  @WithMockUser(username = ACTIVE_EMAIL)
  void employeeSelfChangePassword() throws Exception {

    String content =
        objectMapper.writeValueAsString(ChangePasswordDtoFixture.getValidPasswordDto());

    mockMvc
        .perform(
            put("/api/selfChangePassword").contentType(MediaType.APPLICATION_JSON).content(content))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("SUCCESS"))
        .andExpect(jsonPath("$.message").value("PASSWORD CHANGE SUCCESSFUL"));
  }

  @Test
  @WithMockUser(username = ACTIVE_EMAIL)
  void shouldFailEmployeeSelfChangePasswordForInvalidPassword() throws Exception {

    String content =
            objectMapper.writeValueAsString(ChangePasswordDtoFixture.getInvalidPasswordDto());

    mockMvc
            .perform(
                    put("/api/selfChangePassword").contentType(MediaType.APPLICATION_JSON).content(content))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value("BAD_REQUEST"))
            .andExpect(jsonPath("$.message").value("Password does not match the stored hash"));
  }
}
