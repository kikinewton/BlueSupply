package com.logistics.supply.controller;

import com.logistics.supply.common.annotations.IntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.io.File;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

@IntegrationTest
class ProcurementControllerTest {

  @Autowired
  MockMvc mockMvc;

//  @MockBean
//  RequestItemService requestItemService;


    @Test
    @WithMockUser
    void generateRequestListForSupplier() throws Exception {
      int supplierId = 1;
      File testFile = new File("testfile.txt");
      CompletableFuture<File> future = CompletableFuture.completedFuture(testFile);

      // Mock the service method
//      when(requestItemService.generateRequestListForSupplier(supplierId))
//              .thenReturn(future);

      // Perform the request
      MockHttpServletResponse response = mockMvc.perform(get("/api/procurement/generateRequestListForSupplier/suppliers/{supplierId}", supplierId)
                      .accept(MediaType.APPLICATION_OCTET_STREAM))
              .andReturn()
              .getResponse();

      // Verify response
      assertEquals("text/plain", response.getContentType());
      assertEquals("inline; filename=\"testfile.txt\"", response.getHeader("Content-Disposition"));
      assertEquals((int) testFile.length(), response.getContentLength());
      // You can further verify the content of the response if needed
    }
}