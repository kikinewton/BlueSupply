package com.logistics.supply.controller;

import com.logistics.supply.dto.ResponseDTO;
import com.logistics.supply.dto.UploadDocumentDTO;
import com.logistics.supply.model.RequestDocument;
import com.logistics.supply.service.AbstractRestService;
import lombok.extern.slf4j.Slf4j;
import lombok.var;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.Objects;

import static com.logistics.supply.util.Constants.ERROR;
import static com.logistics.supply.util.Constants.SUCCESS;

@Slf4j
@RestController
@RequestMapping(value = "/api/requestDocument")
public class RequestDocumentController extends AbstractRestService {

  @PostMapping(value = "/upload")
  public ResponseDTO<UploadDocumentDTO> uploadDocument(
      @RequestParam("file") MultipartFile multipartFile,
      @RequestParam("employeeId") int employeeId,
      @RequestParam("docType") String docType) {
    RequestDocument doc = requestDocumentService.storeFile(multipartFile, employeeId, docType);
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

//  public ResponseDTO<>
}
