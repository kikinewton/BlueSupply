package com.logistics.supply.controller;

import com.logistics.supply.common.annotations.IntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@IntegrationTest
class RequestDocumentControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Test
    @WithMockUser
     void testUploadDocument() throws Exception {

        MockMultipartFile file = new MockMultipartFile("file", "test.pdf", "application/pdf", "Test Document".getBytes());
        String docType = "pdf";

        mockMvc.perform(MockMvcRequestBuilders.multipart("/api/requestDocuments/upload")
                        .file(file)
                        .param("docType", docType))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.message").value("DOCUMENT UPLOADED"))
                .andExpect(jsonPath("$.data.fileSize").value(file.getSize()))
                .andExpect(jsonPath("$.data.fileType").value("application/pdf"));
    }

    @Test
    @WithMockUser
    void testUploadMultipleFiles() throws Exception {

        MockMultipartFile file1 = new MockMultipartFile("files", "test1.pdf", "application/pdf", "Test Document".getBytes());
        MockMultipartFile file2 = new MockMultipartFile("files", "test2.pdf", "application/pdf", "Test Document".getBytes());

        mockMvc.perform(MockMvcRequestBuilders.multipart("/api/requestDocuments/uploadMultipleFiles")
                        .file(file1)
                        .file(file2))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.message").value("DOCUMENT UPLOADED"));
      }
}