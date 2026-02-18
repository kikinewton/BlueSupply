package com.logistics.supply.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.logistics.supply.common.annotations.IntegrationTest;
import com.logistics.supply.dto.LoginRequest;
import com.logistics.supply.dto.RegistrationRequest;
import com.logistics.supply.fixture.RegistrationRequestFixture;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Safety net for Phase 2 (jjwt upgrade) and Phase 1 (OAuth2 stack migration).
 *
 * The login tests confirm the full authentication flow — credential validation,
 * token generation, and response shaping — continues to work after library changes.
 */
@IntegrationTest
class AuthControllerTest {

    // Seeded in init_script.sql — BCrypt of "password" placeholder
    private static final String SEEDED_EMAIL    = "kikinewton@gmail.com";
    private static final String SEEDED_PASSWORD = "Admin1234!";

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Test
    void shouldTestSignup() throws Exception {
        RegistrationRequest registrationRequest = RegistrationRequestFixture.getRegistrationRequest();
        String content = objectMapper.writeValueAsString(registrationRequest);
        mockMvc.perform(post("/auth/admin/signup")
                        .content(content)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.message").value("SIGNUP SUCCESSFUL"));
    }

    @Test
    void shouldReturnJwtTokenOnSuccessfulLogin() throws Exception {
        LoginRequest loginRequest = new LoginRequest(SEEDED_EMAIL, SEEDED_PASSWORD);
        String content = objectMapper.writeValueAsString(loginRequest);

        mockMvc.perform(post("/auth/login")
                        .content(content)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.message").value("LOGIN SUCCESSFUL"))
                .andExpect(jsonPath("$.data.token").isNotEmpty());
    }

    @Test
    void shouldRejectLoginWithWrongPassword() throws Exception {
        LoginRequest loginRequest = new LoginRequest(SEEDED_EMAIL, "wrongpassword123!");
        String content = objectMapper.writeValueAsString(loginRequest);

        mockMvc.perform(post("/auth/login")
                        .content(content)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void shouldRejectLoginWithUnknownEmail() throws Exception {
        LoginRequest loginRequest = new LoginRequest("nobody@unknown.com", SEEDED_PASSWORD);
        String content = objectMapper.writeValueAsString(loginRequest);

        mockMvc.perform(post("/auth/login")
                        .content(content)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError());
    }
}
