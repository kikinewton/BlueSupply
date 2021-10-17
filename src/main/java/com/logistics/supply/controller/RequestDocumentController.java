package com.logistics.supply.controller;

import com.logistics.supply.dto.ResponseDTO;
import com.logistics.supply.dto.UploadDocumentDTO;
import com.logistics.supply.model.RequestDocument;
import com.logistics.supply.service.RequestDocumentService;
import com.logistics.supply.service.RequestItemService;
import lombok.extern.slf4j.Slf4j;
import lombok.var;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.logistics.supply.util.Constants.ERROR;
import static com.logistics.supply.util.Constants.SUCCESS;

@Slf4j
@RestController
@RequestMapping(value = "/api/requestDocument")
public class RequestDocumentController {

  @Autowired RequestDocumentService requestDocumentService;
  @Autowired RequestItemService requestItemService;

  public RequestDocumentController(RequestDocumentService documentService) {
    this.requestDocumentService = documentService;
  }

  @PostMapping(value = "/upload")
  public ResponseEntity<?> uploadDocument(
      @RequestParam("file") MultipartFile multipartFile,
      Authentication authentication,
      @RequestParam("docType") String docType) {
    RequestDocument doc =
        requestDocumentService.storeFile(multipartFile, authentication.getName(), docType);
    if (Objects.isNull(doc)) failedResponse("FAILED");
    String fileDownloadUri =
        ServletUriComponentsBuilder.fromCurrentContextPath()
            .path("/downloadFile")
            .path(doc.getFileName())
            .toUriString();

    UploadDocumentDTO result =
        new UploadDocumentDTO(
            doc.getFileName(),
            multipartFile.getSize(),
            multipartFile.getContentType(),
            fileDownloadUri);
    if (Objects.isNull(result)) {
      return failedResponse("FAILED");
    }

    ResponseDTO successResponse = new ResponseDTO<>("DOCUMENT_UPLOADED", SUCCESS, result);
    return ResponseEntity.ok(successResponse);
  }

  @PostMapping("/uploadMultipleFiles")
  public ResponseEntity<?> uploadMultipleFiles(
      @RequestParam("files") MultipartFile[] files, Authentication authentication) {
    List<RequestDocument> docs =
        Arrays.asList(files).stream()
            .map(file -> requestDocumentService.storeFile(file, authentication.getName(), ""))
            .collect(Collectors.toList());

    if (docs.size() > 0) {
      ResponseDTO response = new ResponseDTO("DOCUMENT_UPLOADED", SUCCESS, docs);
      return ResponseEntity.ok(response);
    }
    return failedResponse("FAILED");
  }

  @GetMapping(value = "/download/{fileName}")
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

  @GetMapping(value = "requestItem/{requestItemId}")
  public ResponseEntity<?> getDocumentsForRequest(
      @PathVariable("requestItemId") int requestItemId) {
    var documentMap =
        requestItemService
            .findById(requestItemId)
            .map(
                x -> {
                  Map<String, RequestDocument> res = null;
                  try {
                    res = requestDocumentService.findDocumentForRequest(x.getId());
                  } catch (Exception e) {
                    log.error(e.getMessage());
                  }
                  return res;
                });

    if (documentMap.isPresent()) {
      ResponseDTO response = new ResponseDTO("REQUEST_DOCUMENT", ERROR, documentMap.get());
      return ResponseEntity.ok(response);
    }
    return failedResponse("DOCUMENT_NOT_FOUND");
  }

  public ResponseEntity<ResponseDTO> failedResponse(String message) {
    ResponseDTO failed = new ResponseDTO(message, ERROR, null);
    return ResponseEntity.badRequest().body(failed);
  }
}
