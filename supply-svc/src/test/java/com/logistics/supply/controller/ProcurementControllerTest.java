package com.logistics.supply.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.logistics.supply.common.annotations.IntegrationTest;
import com.logistics.supply.dto.MappingSuppliersAndRequestItemsDto;
import com.logistics.supply.exception.FileGenerationException;
import com.logistics.supply.fixture.RequestItemFixture;
import com.logistics.supply.fixture.SupplierFixture;
import com.logistics.supply.model.RequestItem;
import com.logistics.supply.model.Supplier;
import com.logistics.supply.repository.RequestItemRepository;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import jakarta.servlet.http.HttpServletResponse;

import java.util.Set;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@IntegrationTest
class ProcurementControllerTest {

  @Autowired
  MockMvc mockMvc;

  @Autowired
  ObjectMapper objectMapper;

  @Autowired
  ProcurementController procurementController;

  @Autowired
  RequestItemRepository requestItemRepository;

  @Mock
  private HttpServletResponse response;

  public void setup() {
    MockitoAnnotations.openMocks(this);
  }

    @Test
    void shouldFailToGenerateRequestListForSupplierWithNoItems() {
      int supplierId = 2;

      FileGenerationException exception = assertThrows(FileGenerationException.class, () -> {
        procurementController.generateRequestListFileForSupplier(supplierId, response);
      });

      assertEquals("Supplier with id %s has no LPO request assigned".formatted(supplierId), exception.getMessage());
    }

  @Test
  @WithMockUser(roles = "PROCUREMENT_MANAGER")
  void shouldAddSuppliersToRequestItem() throws Exception {

    MappingSuppliersAndRequestItemsDto suppliersAndRequestItemsDto = new MappingSuppliersAndRequestItemsDto();
    Supplier supplier = SupplierFixture.getSupplier("Jilorm Ventures");

    RequestItem requestItem = RequestItemFixture.builder().build();
    requestItemRepository.save(requestItem);
    suppliersAndRequestItemsDto.setRequestItems(Set.of(requestItem));
    suppliersAndRequestItemsDto.setSuppliers(Set.of(supplier));
    String content = objectMapper.writeValueAsString(suppliersAndRequestItemsDto);

    mockMvc.perform(put("/api/procurement/assignSuppliers/requestItems")
                    .content(content)
                    .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data[0].suppliers").isArray())
            .andExpect(jsonPath("$.data[0].suppliers", hasSize(1)))
            .andExpect(jsonPath("$.status").value("SUCCESS"))
            .andExpect(jsonPath("$.message").value("UPDATE SUCCESSFUL"));
    }

  @Test
  @WithMockUser
  void generateRequestListForSupplier() throws Exception {
    int supplierId = 1;

    MockHttpServletResponse response = mockMvc.perform(
            get("/res/procurement/generateRequestListForSupplier/suppliers/{supplierId}", supplierId)
                    .accept(MediaType.APPLICATION_OCTET_STREAM))
            .andReturn()
            .getResponse();

    // Verify response
    assertEquals("application/pdf", response.getContentType());
    MatcherAssert.assertThat(response.getHeader("Content-Disposition"), containsString("JilormVentures"));
  }
}