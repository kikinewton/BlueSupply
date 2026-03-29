package com.logistics.supply.service;

import com.logistics.supply.configuration.FileStorageProperties;
import com.logistics.supply.dto.RequestDocumentDto;
import com.logistics.supply.dto.UploadDocumentDto;
import com.logistics.supply.exception.RequestDocumentNotFoundException;
import com.logistics.supply.exception.RequestItemSuppliedByNotFoundException;
import com.logistics.supply.model.Quotation;
import com.logistics.supply.model.RequestDocument;
import com.logistics.supply.model.RequestItem;
import com.logistics.supply.repository.GoodsReceivedNoteRepository;
import com.logistics.supply.repository.LocalPurchaseOrderRepository;
import com.logistics.supply.repository.RequestDocumentRepository;
import com.logistics.supply.util.Constants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.AccessDeniedException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.MessageFormat;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Slf4j
@Service
public class RequestDocumentService {

  private static final Set<String> ALLOWED_EXTENSIONS = Set.of(
      "pdf", "doc", "docx", "xls", "xlsx", "png", "jpg", "jpeg");

  private final Path fileStorageLocation;
  @Autowired RequestDocumentRepository requestDocumentRepository;
  @Autowired RequestItemService requestItemService;
  @Autowired LocalPurchaseOrderRepository localPurchaseOrderRepository;
  @Autowired GoodsReceivedNoteRepository goodsReceivedNoteRepository;


  public RequestDocumentService(FileStorageProperties fileStorageProperties) {
    this.fileStorageLocation =
        Paths.get(fileStorageProperties.getUploadDirectory()).toAbsolutePath().normalize();
    try {
      Files.createDirectories(Paths.get(this.fileStorageLocation + File.separator + "/supply"));
    } catch (Exception e) {
      log.error("Failed to create file storage directory at {}", this.fileStorageLocation, e);
    }
  }

  public Set<RequestDocumentDto> findAll() {

    log.info("Find all request documents");
    List<RequestDocument> documents = requestDocumentRepository.findAll();
    return documents.stream()
        .map(RequestDocumentDto::toDto)
        .collect(Collectors.toSet());
  }

  public Set<RequestDocumentDto> findQuotationsForRequestItem(int requestItemId) {

    log.info("Find request documents related to request item id {}", requestItemId);
    return requestDocumentRepository.findQuotationsByRequestItem(requestItemId).stream()
        .map(RequestDocumentDto::toDto)
        .collect(Collectors.toSet());
  }

  public RequestDocument findById(int requestDocumentId) {

    log.info("Find request document with id {}", requestDocumentId);
    return requestDocumentRepository
        .findById(requestDocumentId)
        .orElseThrow(() -> new RequestDocumentNotFoundException(requestDocumentId));
  }

  public RequestDocument storeFile(MultipartFile file, String employeeEmail, String docType) {

    if (null == file.getOriginalFilename()) {
      throw new AssertionError();
    }
    log.info("Store file with name {}", file.getOriginalFilename());
    String originalFileName = file.getOriginalFilename().replace(" ", "");
    String fileName =
        com.google.common.io.Files.getNameWithoutExtension(file.getOriginalFilename())
            .replace(" ", "");
    String fileExtension =
        getExtension(originalFileName)
            .orElseThrow(() -> new IllegalStateException("FILE TYPE IS NOT VALID"));
    if (!ALLOWED_EXTENSIONS.contains(fileExtension.toLowerCase())) {
      throw new IllegalStateException(
          "File type '." + fileExtension + "' is not allowed. Permitted types: " + ALLOWED_EXTENSIONS);
    }
    fileName = employeeEmail + "_" + fileName + "_" + new Date().getTime() + "." + fileExtension;
    Path targetLocation = this.fileStorageLocation.resolve(fileName.replace(" ", ""));
    try {
      Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
    } catch (IOException e) {
      log.error("Failed to copy uploaded file to storage: {}", fileName, e);
    }

    RequestDocument newDoc = new RequestDocument();
    String documentType = docType.isEmpty() ? fileExtension : docType;
    newDoc.setDocumentType(documentType);
    newDoc.setFileName(fileName);
    newDoc.setDocumentFormat(file.getContentType());
    return requestDocumentRepository.save(newDoc);
  }

  public RequestDocument storePdfFile(InputStream inputStream, String fileName) {

      CompletableFuture.runAsync(() -> saveFile(inputStream, fileName));
      RequestDocument newDoc = new RequestDocument();
      String documentType = "pdf";
      newDoc.setDocumentType(documentType);
      newDoc.setFileName(fileName);
      newDoc.setDocumentFormat("pdf");
      return requestDocumentRepository.save(newDoc);
  }

  private void saveFile(InputStream inputStream, String fileName) {

    log.info("Store file with name {}", fileName);
    Path targetLocation = this.fileStorageLocation.resolve(fileName.replace(" ", ""));
    try {
      Files.copy(inputStream, targetLocation, StandardCopyOption.REPLACE_EXISTING);
    } catch (IOException e) {
      log.error("Failed to save PDF file to storage: {}", fileName, e);
    }
  }

  public void findByFileName(String fileName) {

    log.info("Find document with name {}", fileName);
    requestDocumentRepository.findByFileName(fileName)
            .orElseThrow(() -> new RequestDocumentNotFoundException(fileName));
  }

  public Optional<String> getExtension(String filename) {

    return Optional.ofNullable(filename)
        .filter(f -> f.contains("."))
        .map(f -> f.substring(filename.lastIndexOf(".") + 1));
  }

  public void verifyIfDocExist(int requestDocumentId) {

    log.info("Verify document with id {} exists", requestDocumentId);
    boolean documentExists = requestDocumentRepository.existsById(requestDocumentId);
    if (!documentExists) {
      throw new RequestDocumentNotFoundException(requestDocumentId);
    }
  }

  public Resource loadFileAsResource(String fileName) throws Exception {

    try {
      Path filePath = this.fileStorageLocation.resolve(fileName).normalize().toAbsolutePath();
      if (!filePath.startsWith(this.fileStorageLocation)) {
        throw new AccessDeniedException("Access to file outside upload directory is not allowed");
      }
      Resource resource = new UrlResource(filePath.toUri());
      if (resource.exists()) {
        return resource;
      } else {
        throw new FileNotFoundException(MessageFormat.format("{0}{1}", Constants.FILE_NOT_FOUND, fileName));
      }
    } catch (MalformedURLException ex) {
      log.error(ex.toString());
      throw new FileNotFoundException(MessageFormat.format("{0}{1}", Constants.FILE_NOT_FOUND, fileName));
    }
  }
  
  public Map<String, RequestDocument> findDocumentForRequest(int requestItemId) {

    log.info("Find document for request item id {}", requestItemId);
    RequestItem item = requestItemService.findById(requestItemId);
    if (Objects.isNull(item.getSuppliedBy())) {
      throw new RequestItemSuppliedByNotFoundException(requestItemId);
    }
    int supplierId = item.getSuppliedBy();

    // get final quotation for this request item
    Set<Quotation> quotations = item.getQuotations();
    quotations.removeIf(q -> q.getSupplier().getId() != supplierId);
    RequestDocument quotationDoc = new RequestDocument();
    quotations.stream()
        .findFirst()
        .ifPresent(
            q -> BeanUtils.copyProperties(q.getRequestDocument(), quotationDoc));

    RequestDocument invoiceDoc = new RequestDocument();

    localPurchaseOrderRepository
            .findLpoByRequestItem(requestItemId)
            .flatMap(lpo -> goodsReceivedNoteRepository.findByLocalPurchaseOrder(lpo))
            .ifPresent(g -> BeanUtils.copyProperties(g.getInvoice().getInvoiceDocument(), invoiceDoc));

    Map<String, RequestDocument> requestDocumentMap = new LinkedHashMap<>();
    requestDocumentMap.put("quotationDoc", quotationDoc);
    requestDocumentMap.put("invoiceDoc", invoiceDoc);

    return requestDocumentMap;
  }

  public UploadDocumentDto uploadDocument(MultipartFile multipartFile, String email, String docType) {

    log.info("Upload document");
    RequestDocument requestDocument = storeFile(multipartFile, email, docType);
    String fileDownloadUri =
            ServletUriComponentsBuilder.fromCurrentContextPath()
                    .path("/res/requestDocuments/download/")
                    .path(requestDocument.getFileName())
                    .toUriString();

    return new UploadDocumentDto(
            requestDocument.getId(),
            requestDocument.getFileName(),
            multipartFile.getSize(),
            multipartFile.getContentType(),
            fileDownloadUri);
  }
}
