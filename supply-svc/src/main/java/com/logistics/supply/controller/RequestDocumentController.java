package com.logistics.supply.controller;

import com.logistics.supply.dto.RequestDocumentDto;
import com.logistics.supply.dto.ResponseDto;
import com.logistics.supply.dto.UploadDocumentDto;
import com.logistics.supply.model.RequestDocument;
import com.logistics.supply.service.RequestDocumentService;
import com.logistics.supply.util.Constants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequiredArgsConstructor
public class RequestDocumentController {

  private final RequestDocumentService requestDocumentService;


  @PostMapping(value = "/api/requestDocuments/upload")
  public ResponseEntity<ResponseDto<UploadDocumentDto>> uploadDocument(
          Authentication authentication,
          @RequestParam("file") MultipartFile multipartFile,
      @RequestParam("docType") String docType) {

    UploadDocumentDto documentDto = requestDocumentService.uploadDocument(multipartFile, authentication.getName(), docType);
    return ResponseDto.wrapSuccessResult(documentDto, "DOCUMENT UPLOADED");
  }

  @PostMapping("/api/requestDocuments/uploadMultipleFiles")
  public ResponseEntity<ResponseDto<List<RequestDocument>>> uploadMultipleFiles(
      @RequestParam("files") MultipartFile[] files,
      Authentication authentication) {

    List<RequestDocument> docs =
        Arrays.stream(files)
            .map(file -> requestDocumentService.storeFile(file, authentication.getName(), ""))
            .collect(Collectors.toList());

      return ResponseDto.wrapSuccessResult(docs, "DOCUMENT UPLOADED");


  }

  @GetMapping(value = "/res/requestDocuments/download/{fileName}")
  public ResponseEntity<Resource> downloadDocument(
      @PathVariable("fileName") String fileName,
      HttpServletRequest request) {

    requestDocumentService.findByFileName(fileName);
    Resource resource = null;
    try {
      resource = requestDocumentService.loadFileAsResource(fileName);
    } catch (Exception e) {
      log.error(e.getMessage());
    }
    String contentType = null;
    try {
      assert resource != null;
      contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
    } catch (IOException e) {
      log.error(e.getMessage());
    }
    if (contentType == null) {
      contentType = "application/octet-stream";
    }

    return ResponseEntity.ok()
        .contentType(MediaType.parseMediaType(contentType))
        .header(
            HttpHeaders.CONTENT_DISPOSITION,
            "attachment; filename=\"" + resource.getFilename() + "\"")
        .body(resource);
  }

  @GetMapping(value = "/api/requestDocuments/requestItems/{requestItemId}")
  public ResponseEntity<ResponseDto<Map<String, RequestDocument>>> getDocumentsForRequest(
          @PathVariable("requestItemId") int requestItemId)
      throws Exception {

    Map<String, RequestDocument> documentForRequest =
        requestDocumentService.findDocumentForRequest(requestItemId);
    return ResponseDto.wrapSuccessResult(documentForRequest, Constants.FETCH_SUCCESSFUL);
  }

  @GetMapping(value = "/api/requestDocuments/requestItems/{requestItemId}/quotations")
  public ResponseEntity<ResponseDto<Set<RequestDocumentDto>>> getQuotationsForRequestItem(
          @PathVariable("requestItemId") int requestItemId) {

    Set<RequestDocumentDto> quotationsForRequestItem = requestDocumentService.findQuotationsForRequestItem(requestItemId);
    return ResponseDto.wrapSuccessResult(quotationsForRequestItem, "QUOTATION DOCS ASSIGNED TO REQUEST ITEM");
  }
  @GetMapping(value = "/api/requestDocuments")
  public ResponseEntity<ResponseDto<Set<RequestDocumentDto>>> getAllDocuments() {

    Set<RequestDocumentDto> all = requestDocumentService.findAll();
    return ResponseDto.wrapSuccessResult(all, "REQUEST DOCUMENTS");
  }
}
