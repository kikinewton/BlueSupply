package com.logistics.supply.service;

import com.logistics.supply.configuration.FileStorageProperties;
import com.logistics.supply.errorhandling.GeneralException;
import com.logistics.supply.model.LocalPurchaseOrder;
import com.logistics.supply.model.Quotation;
import com.logistics.supply.model.RequestDocument;
import com.logistics.supply.model.RequestItem;
import com.logistics.supply.repository.GoodsReceivedNoteRepository;
import com.logistics.supply.repository.LocalPurchaseOrderRepository;
import com.logistics.supply.repository.RequestDocumentRepository;
import com.logistics.supply.repository.RequestItemRepository;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.MessageFormat;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static com.logistics.supply.util.Constants.*;

@Slf4j
@Service
public class RequestDocumentService {

  private final Path fileStorageLocation;
  @Autowired RequestDocumentRepository requestDocumentRepository;
  @Autowired RequestItemRepository requestItemRepository;
  @Autowired LocalPurchaseOrderRepository localPurchaseOrderRepository;
  @Autowired GoodsReceivedNoteRepository goodsReceivedNoteRepository;
  public static final String FINAL_SUPPLIER_NOT_ASSIGNED = "FINAL SUPPLIER NOT ASSIGNED";

  public RequestDocumentService(FileStorageProperties fileStorageProperties) {
    this.fileStorageLocation =
        Paths.get(fileStorageProperties.getUploadDirectory()).toAbsolutePath().normalize();
    try {
      Files.createDirectories(Paths.get(this.fileStorageLocation + File.separator + "/supply"));
    } catch (Exception e) {
      log.error(e.toString());
    }
  }

  public Set<RequestDocument.RequestDocumentDTO> findAll() {
    List<RequestDocument> documents = requestDocumentRepository.findAll();
    return documents.stream()
        .map(d -> RequestDocument.RequestDocumentDTO.toDto(d))
        .collect(Collectors.toSet());
  }

  @SneakyThrows
  public RequestDocument findById(int requestDocumentId) {
    return requestDocumentRepository
        .findById(requestDocumentId)
        .orElseThrow(() -> new GeneralException(REQUEST_DOCUMENT_NOT_FOUND, HttpStatus.NOT_FOUND));
  }

  public RequestDocument storeFile(MultipartFile file, String employeeEmail, String docType) {
    String originalFileName = file.getOriginalFilename().replace(" ", "");
    String fileName =
        com.google.common.io.Files.getNameWithoutExtension(file.getOriginalFilename())
            .replace(" ", "");
    String fileExtension =
        getExtension(originalFileName)
            .orElseThrow(() -> new IllegalStateException("FILE TYPE IS NOT VALID"));
    fileName = employeeEmail + "_" + fileName + "_" + new Date().getTime() + "." + fileExtension;
    Path targetLocation = this.fileStorageLocation.resolve(fileName.replace(" ", ""));
    try {
      Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
    } catch (IOException e) {
      log.error(e.toString());
    }

    RequestDocument newDoc = new RequestDocument();
    String documentType = docType.isEmpty() ? fileExtension : docType;
    newDoc.setDocumentType(documentType);
    newDoc.setFileName(fileName);
    newDoc.setDocumentFormat(file.getContentType());
    return requestDocumentRepository.save(newDoc);
  }

  @SneakyThrows(GeneralException.class)
  public RequestDocument storePdfFile(InputStream inputStream, String fileName) {
    try {
      CompletableFuture.runAsync(() -> saveFile(inputStream, fileName));
      RequestDocument newDoc = new RequestDocument();
      String documentType = "pdf";
      newDoc.setDocumentType(documentType);
      newDoc.setFileName(fileName);
      newDoc.setDocumentFormat("pdf");
      return requestDocumentRepository.save(newDoc);
    } catch (Exception e) {
      log.error(e.toString());
    }
    throw new GeneralException("FAILED TO STORE FILE", HttpStatus.BAD_REQUEST);
  }

  private void saveFile(InputStream inputStream, String fileName) {
    Path targetLocation = this.fileStorageLocation.resolve(fileName.replace(" ", ""));
    try {
      Files.copy(inputStream, targetLocation, StandardCopyOption.REPLACE_EXISTING);
    } catch (IOException e) {
      log.error(e.toString());
    }
  }

  public RequestDocument findByFileName(String fileName) {
    return requestDocumentRepository.findByFileName(fileName);
  }

  public Optional<String> getExtension(String filename) {
    return Optional.ofNullable(filename)
        .filter(f -> f.contains("."))
        .map(f -> f.substring(filename.lastIndexOf(".") + 1));
  }

  public boolean verifyIfDocExist(int requestDocumentId) {
    return requestDocumentRepository.existsById(requestDocumentId);
  }

  public Resource loadFileAsResource(String fileName) throws Exception {
    try {
      Path filePath = this.fileStorageLocation.resolve(fileName);
      Resource resource = new UrlResource(filePath.toUri());
      if (resource.exists()) {
        return resource;
      } else {
        throw new FileNotFoundException(MessageFormat.format("{0}{1}", FILE_NOT_FOUND, fileName));
      }
    } catch (MalformedURLException ex) {
      log.error(ex.toString());
      throw new FileNotFoundException(MessageFormat.format("{0}{1}", FILE_NOT_FOUND, fileName));
    }
  }

  public Map<String, RequestDocument> findDocumentForRequest(int requestItemId) throws Exception {
    RequestItem item =
        requestItemRepository
            .findById(requestItemId)
            .orElseThrow(() -> new GeneralException(REQUEST_ITEM_NOT_FOUND, HttpStatus.NOT_FOUND));
    if (Objects.isNull(item.getSuppliedBy())) {
      throw new GeneralException(FINAL_SUPPLIER_NOT_ASSIGNED, HttpStatus.NOT_FOUND);
    }
    int supplierId = item.getSuppliedBy();

    /** get final quotation for this request item */
    Set<Quotation> quotations = item.getQuotations();
    quotations.removeIf(q -> q.getSupplier().getId() != supplierId);
    RequestDocument quotationDoc = new RequestDocument();
    quotations.stream()
        .findFirst()
        .ifPresent(
            q -> {
              BeanUtils.copyProperties(q.getRequestDocument(), quotationDoc);
            });

    LocalPurchaseOrder lpo =
        localPurchaseOrderRepository
            .findLpoByRequestItem(requestItemId)
            .orElseThrow(() -> new GeneralException(LPO_NOT_FOUND, HttpStatus.NOT_FOUND));

    RequestDocument invoiceDoc = new RequestDocument();

    RequestDocument finalInvoiceDoc = invoiceDoc;
    goodsReceivedNoteRepository
        .findByLocalPurchaseOrder(lpo)
        .ifPresent(
            g -> {
              BeanUtils.copyProperties(g.getInvoice().getInvoiceDocument(), finalInvoiceDoc);
            });

    Map<String, RequestDocument> requestDocumentMap = new LinkedHashMap<>();
    if (Objects.nonNull(quotationDoc)) requestDocumentMap.put("quotationDoc", quotationDoc);
    if (Objects.nonNull(invoiceDoc)) requestDocumentMap.put("invoiceDoc", invoiceDoc);

    return requestDocumentMap;
  }
}
