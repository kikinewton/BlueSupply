package com.logistics.supply.controller;

import com.logistics.supply.common.annotations.IntegrationTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;

@IntegrationTest
class AddRequestItemTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    public void shouldAddRequestItems() {

    }
}