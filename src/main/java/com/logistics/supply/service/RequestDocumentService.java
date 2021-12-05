package com.logistics.supply.service;

import com.logistics.supply.configuration.FileStorageProperties;
import com.logistics.supply.model.LocalPurchaseOrder;
import com.logistics.supply.model.RequestDocument;
import com.logistics.supply.model.RequestItem;
import com.logistics.supply.repository.GoodsReceivedNoteRepository;
import com.logistics.supply.repository.LocalPurchaseOrderRepository;
import com.logistics.supply.repository.RequestDocumentRepository;
import com.logistics.supply.repository.RequestItemRepository;
import lombok.extern.slf4j.Slf4j;
import lombok.var;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
public class RequestDocumentService {

  private final Path fileStorageLocation;
  @Autowired RequestDocumentRepository requestDocumentRepository;
  @Autowired RequestItemRepository requestItemRepository;
  @Autowired LocalPurchaseOrderRepository localPurchaseOrderRepository;
  @Autowired GoodsReceivedNoteRepository goodsReceivedNoteRepository;

  public RequestDocumentService(FileStorageProperties fileStorageProperties) {
    this.fileStorageLocation =
        Paths.get(fileStorageProperties.getUploadDirectory()).toAbsolutePath().normalize();
    try {
      Files.createDirectories(this.fileStorageLocation);
    } catch (Exception e) {
      log.error(e.toString());
    }
  }

  public RequestDocument findById(int requestDocumentId) {
    try {
      Optional<RequestDocument> document = requestDocumentRepository.findById(requestDocumentId);
      if (document.isPresent()) return document.get();
    } catch (Exception e) {
      log.error(e.getMessage());
    }
    return null;
  }

  public RequestDocument storeFile(MultipartFile file, String employeeEmail, String docType) {
    String originalFileName = file.getOriginalFilename();
    String fileName =
        com.google.common.io.Files.getNameWithoutExtension(file.getOriginalFilename());
    String fileExtension =
        getExtension(originalFileName)
            .orElseThrow(() -> new IllegalStateException("File type is not valid"));
    fileName = employeeEmail + "_" + fileName + "_" + new Date().getTime() + "." + fileExtension;
    Path targetLocation = this.fileStorageLocation.resolve(fileName);
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

  public RequestDocument findByFileName(String fileName) {
    try {
      return requestDocumentRepository.findByFileName(fileName);
    } catch (Exception e) {
      log.error(e.getMessage());
    }
    return null;
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
      System.out.println("filePath = " + filePath);
      Resource resource = new UrlResource(filePath.toUri());
      if (resource.exists()) {
        return resource;
      } else {
        throw new FileNotFoundException("File not found " + fileName);
      }
    } catch (MalformedURLException ex) {
      log.error(ex.toString());
      throw new FileNotFoundException("File not found " + fileName);
    }
  }

  public Map<String, RequestDocument> findDocumentForRequest(int requestItemId) throws Exception {
    Optional<RequestItem> item = requestItemRepository.findById(requestItemId);
    int supplierId = item.map(RequestItem::getSuppliedBy).orElseThrow(Exception::new);

    var quotation =
        item.map(requestItem -> requestItem.getQuotations())
            .map(x -> x.stream().filter(i -> i.getSupplier().getId().equals(supplierId)))
            .get()
            .findFirst()
            .get();

    RequestDocument quotationDoc = quotation.getRequestDocument();

    LocalPurchaseOrder lpo = localPurchaseOrderRepository.findLpoByRequestItem(requestItemId);
    RequestDocument invoiceDoc = null;
    if(lpo != null) {
       invoiceDoc =
              goodsReceivedNoteRepository.findBySupplier(supplierId).stream()
                      .filter(x -> x.getLocalPurchaseOrder().getId().equals(lpo))
                      .findFirst()
                      .get()
                      .getInvoice()
                      .getInvoiceDocument();
    }

    Map<String, RequestDocument> requestDocumentMap = new LinkedHashMap<>();
    requestDocumentMap.put("quotationDoc", quotationDoc);
    requestDocumentMap.put("invoiceDoc", invoiceDoc);
    return requestDocumentMap;
  }
}
