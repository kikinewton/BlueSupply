package com.logistics.supply.controller;

import com.logistics.supply.dto.ResponseDTO;
import com.logistics.supply.dto.UploadDocumentDTO;
import com.logistics.supply.errorhandling.GeneralException;
import com.logistics.supply.model.RequestDocument;
import com.logistics.supply.model.RequestItem;
import com.logistics.supply.service.RequestDocumentService;
import com.logistics.supply.service.RequestItemService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static com.logistics.supply.util.Constants.*;
import static com.logistics.supply.util.Helper.failedResponse;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/api")
public class RequestDocumentController {
  private final RequestDocumentService requestDocumentService;
  private final RequestItemService requestItemService;

  @PostMapping(value = "/requestDocuments/upload")
  public ResponseEntity<?> uploadDocument(
      @RequestParam("file") MultipartFile multipartFile,
      Authentication authentication,
      @RequestParam("docType") String docType) {
    RequestDocument doc =
        requestDocumentService.storeFile(multipartFile, authentication.getName(), docType);

    if (Objects.isNull(doc)) failedResponse("FAILED");
    String fileDownloadUri =
        ServletUriComponentsBuilder.fromCurrentContextPath()
            .path("/api/requestDocuments/download/")
            .path(doc.getFileName())
            .toUriString();

    UploadDocumentDTO result =
        new UploadDocumentDTO(
            doc.getId(),
            doc.getFileName(),
            multipartFile.getSize(),
            multipartFile.getContentType(),
            fileDownloadUri);
    return ResponseDTO.wrapSuccessResult(result, "DOCUMENT UPLOADED");
  }

  @PostMapping("/requestDocuments/uploadMultipleFiles")
  public ResponseEntity<?> uploadMultipleFiles(
      @RequestParam("files") MultipartFile[] files, Authentication authentication) {
    List<RequestDocument> docs =
        Arrays.asList(files).stream()
            .map(file -> requestDocumentService.storeFile(file, authentication.getName(), ""))
            .collect(Collectors.toList());

    if (!docs.isEmpty()) {
      return ResponseDTO.wrapSuccessResult(docs, "DOCUMENT UPLOADED");
    }
    return failedResponse("FAILED");
  }

  @GetMapping(value = "/requestDocuments/download/{fileName}")
  public ResponseEntity<Resource> downloadDocument(
      @PathVariable("fileName") String fileName, HttpServletRequest request) {
    RequestDocument doc = requestDocumentService.findByFileName(fileName);
    if (Objects.isNull(doc)) return ResponseEntity.notFound().build();
    Resource resource = null;
    try {
      resource = requestDocumentService.loadFileAsResource(fileName);
    } catch (Exception e) {
      log.error(e.getMessage());
    }
    String contentType = null;
    try {
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

  @GetMapping(value = "/requestDocuments/requestItems/{requestItemId}")
  public ResponseEntity<?> getDocumentsForRequest(@PathVariable("requestItemId") int requestItemId)
      throws Exception {
    RequestItem requestItem =
        requestItemService
            .findById(requestItemId)
            .orElseThrow(() -> new GeneralException(REQUEST_ITEM_NOT_FOUND, HttpStatus.NOT_FOUND));
    Map<String, RequestDocument> documentForRequest =
        requestDocumentService.findDocumentForRequest(requestItem.getId());
    return ResponseDTO.wrapSuccessResult(documentForRequest, FETCH_SUCCESSFUL);
  }

  @GetMapping(value = "/requestDocuments")
  public ResponseEntity<?> getAllDocuments() {
    Set<RequestDocument.RequestDocumentDTO> all = requestDocumentService.findAll();
    return ResponseDTO.wrapSuccessResult(all, "REQUEST DOCUMENTS");
  }
}
