package com.logistics.supply.controller;

import com.logistics.supply.dto.ResponseDTO;
import com.logistics.supply.dto.UploadDocumentDTO;
import com.logistics.supply.model.RequestDocument;
import com.logistics.supply.repository.RequestDocumentRepository;
import com.logistics.supply.service.AbstractRestService;
import com.logistics.supply.service.RequestDocumentService;
import lombok.extern.slf4j.Slf4j;
import lombok.var;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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
public class RequestDocumentController extends AbstractRestService {

  private RequestDocumentService documentService;
  @Autowired private RequestDocumentRepository requestDocumentRepository;

  public RequestDocumentController(RequestDocumentService documentService) {
    this.documentService = documentService;
  }

  @PostMapping(value = "/upload")
  public ResponseDTO<UploadDocumentDTO> uploadDocument(
      @RequestParam("file") MultipartFile multipartFile,
      @RequestParam("employeeId") int employeeId,
      @RequestParam("docType") String docType) {
    RequestDocument doc = documentService.storeFile(multipartFile, employeeId, docType);
    if (Objects.isNull(doc)) return new ResponseDTO<>(ERROR, null, HttpStatus.BAD_REQUEST.name());
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
    if (Objects.isNull(result))
      return new ResponseDTO<>(ERROR, null, HttpStatus.BAD_REQUEST.name());

    return new ResponseDTO<>(SUCCESS, result, HttpStatus.CREATED.name());
  }

  @PostMapping("/uploadMultipleFiles")
  public ResponseDTO<List<RequestDocument>> uploadMultipleFiles(
      @RequestParam("files") MultipartFile[] files, @RequestParam("employeeId") int employeeId) {
    List<RequestDocument> docs =
        Arrays.asList(files).stream()
            .map(file -> documentService.storeFile(file, employeeId, ""))
            .collect(Collectors.toList());

    if (docs.size() > 0) {
      return new ResponseDTO<>(SUCCESS, docs, HttpStatus.CREATED.name());
    }
    return new ResponseDTO<>(ERROR, null, HttpStatus.BAD_REQUEST.name());
  }

  @GetMapping(value = "/download/{fileName}")
  public ResponseEntity<Resource> downloadDocument(
      @PathVariable("fileName") String fileName, HttpServletRequest request) {
    RequestDocument doc = requestDocumentRepository.findByFileName(fileName);
    System.out.println("doc = " + doc);
    if (Objects.isNull(doc)) return ResponseEntity.notFound().build();
    Resource resource = null;
    try {
      resource = documentService.loadFileAsResource(fileName);
    } catch (Exception e) {
      e.printStackTrace();
    }
    String contentType = null;
    try {
      contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
    } catch (IOException e) {
      e.printStackTrace();
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
                  Map<String, RequestDocument> res =
                      requestDocumentService.findDocumentForRequest(x.getId());
                  return res;
                });

    if (documentMap.isPresent()) return ResponseEntity.ok(documentMap.get());
    return ResponseEntity.badRequest().body("Document not found");
  }
}
