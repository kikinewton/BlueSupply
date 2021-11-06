package com.logistics.supply.controller;

import com.logistics.supply.dto.*;
import com.logistics.supply.event.AssignQuotationRequestItemEvent;
import com.logistics.supply.model.Quotation;
import com.logistics.supply.model.RequestDocument;
import com.logistics.supply.model.RequestItem;
import com.logistics.supply.model.Supplier;
import com.logistics.supply.repository.SupplierRequestMapRepository;
import com.logistics.supply.service.QuotationService;
import com.logistics.supply.service.RequestDocumentService;
import com.logistics.supply.service.RequestItemService;
import com.logistics.supply.service.SupplierService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.stream.Collectors;

import static com.logistics.supply.util.Constants.ERROR;
import static com.logistics.supply.util.Constants.SUCCESS;

@Slf4j
@RestController
@RequestMapping("/api")
public class QuotationController {

  @Autowired SupplierService supplierService;
  @Autowired QuotationService quotationService;
  @Autowired RequestItemService requestItemService;
  @Autowired SupplierRequestMapRepository supplierRequestMapRepository;
  @Autowired RequestDocumentService documentService;
  @Autowired ApplicationEventPublisher applicationEventPublisher;

  public QuotationController(RequestDocumentService documentService) {
    this.documentService = documentService;
  }

  @PostMapping(value = "/quotations/suppliers/{supplierId}")
  @PreAuthorize("hasRole('ROLE_PROCUREMENT_OFFICER')")
  public ResponseEntity<?> createQuotation(
      @PathVariable("supplierId") int supplierId,
      @RequestParam("file") MultipartFile multipartFile,
      Authentication authentication) {

    RequestDocument doc = documentService.storeFile(multipartFile, authentication.getName(), "");

    if (Objects.nonNull(doc)) {
      Quotation quotation = new Quotation();
      Optional<Supplier> supplier = supplierService.findBySupplierId(supplierId);
      if (!supplier.isPresent()) return failedResponse("SUPPLIER_DOES_NOT_EXIST");
      Supplier s = supplier.get();
      quotation.setSupplier(s);
      quotation.setRequestDocument(doc);
      Quotation savedQuotation = quotationService.save(quotation);
      if (Objects.nonNull(savedQuotation)) {
        Set<RequestItem> requestItems = requestItemService.findRequestItemsForSupplier(supplierId);
        requestItems.stream()
            .map(
                r -> {
                  r.getQuotations().add(savedQuotation);
                  RequestItem res = requestItemService.saveRequestItem(r);
                  if (Objects.nonNull(res)) {
                    supplierRequestMapRepository.updateDocumentStatus(res.getId(), s.getId());
                    return res;
                  }
                  return null;
                })
            .collect(Collectors.toSet());
        ResponseDTO response =
            new ResponseDTO("QUOTATION_ASSIGNED_TO_REQUEST_ITEMS", SUCCESS, savedQuotation);
        return ResponseEntity.ok(response);
      }

      return failedResponse("QUOTATION_NOT_CREATED");
    }
    return failedResponse("DOCUMENT_NOT_SAVED");
  }

  @GetMapping(value = "/quotations/suppliers/{supplierId}")
  @PreAuthorize("hasRole('ROLE_GENERAL_MANAGER') or hasRole('ROLE_PROCUREMENT_OFFICER')")
  public ResponseEntity<?> getQuotationsBySupplier(@PathVariable("supplierId") int supplierId) {

    Optional<Supplier> supplier = supplierService.findBySupplierId(supplierId);
    if (!supplier.isPresent()) return failedResponse("SUPPLIER_NOT_FOUND");

    Set<Quotation> quotations = new HashSet<>();
    quotations.addAll(quotationService.findBySupplier(supplierId));
    if (quotations.isEmpty()) return failedResponse("NO_REQUEST_ITEM_LINKED_TO_QUOTATION");
    List<SupplierQuotationDTO> res =
        quotations.stream()
            .map(
                x -> {
                  SupplierQuotationDTO sq = new SupplierQuotationDTO();
                  sq.setQuotation(x);
                  List<RequestItem> requestItems =
                      requestItemService.getRequestItemsByQuotation(x.getId());
                  sq.setRequestItems(requestItems);
                  return sq;
                })
            .collect(Collectors.toList());

    ResponseDTO response = new ResponseDTO("FETCHED_QUOTATIONS_BY_SUPPLIER", SUCCESS, res);
    return ResponseEntity.ok(response);
  }

  @GetMapping(value = "/quotations")
  @PreAuthorize("hasRole('ROLE_GENERAL_MANAGER') or hasRole('ROLE_PROCUREMENT_OFFICER')")
  public ResponseEntity<?> getAllQuotations() {
    try {
      Set<Quotation> quotations = new HashSet<>();
      quotations.addAll(quotationService.findAll());
      ResponseDTO response = new ResponseDTO("FETCH_ALL_QUOTATIONS", SUCCESS, quotations);
      return ResponseEntity.ok(response);
    } catch (Exception e) {
      log.error(e.getMessage());
    }
    return failedResponse("FETCH_QUOTATION_FAILED");
  }

  @GetMapping(value = "/quotations/withoutDocument")
  @PreAuthorize("hasRole('ROLE_GENERAL_MANAGER') or hasRole('ROLE_PROCUREMENT_OFFICER')")
  public ResponseEntity<?> getAllQuotationsWithoutDocument() {

    try {
      List<RequestQuotationDTO> reqQuotations = new ArrayList<>();
      reqQuotations.addAll(quotationService.findQuotationsWithoutAssignedDocument());
      ResponseDTO response =
          new ResponseDTO("FETCH_QUOTATIONS_WITHOUT_DOCUMENTS", SUCCESS, reqQuotations);
      return ResponseEntity.ok(response);
    } catch (Exception e) {
      log.error(e.getMessage());
    }
    return failedResponse("FAILED_TO_FETCH_QUOTATIONS");
  }

  @PutMapping(value = "/quotations/assignToRequestItems")
  @PreAuthorize("hasRole('ROLE_PROCUREMENT_OFFICER')")
  public ResponseEntity<?> assignQuotationsToRequestItems(
      @RequestBody MapQuotationsToRequestItemsDTO mappingDTO) {
    Set<RequestItem> items =
        mappingDTO.getRequestItems().stream()
            .filter(i -> requestItemService.existById(i.getId()))
            .map(r -> requestItemService.findById(r.getId()).get())
            .collect(Collectors.toSet());

    Set<Quotation> quotations =
        mappingDTO.getQuotations().stream()
            .filter(q -> quotationService.existByQuotationId(q.getId()))
            .map(p -> quotationService.findById(p.getId()))
            .collect(Collectors.toSet());

    try {
      List<RequestItem> result =
          items.stream()
              .peek(System.out::println)
              .map(i -> quotationService.assignToRequestItem(i, quotations))
              .collect(Collectors.toList());

      AssignQuotationRequestItemEvent requestItemEvent =
          new AssignQuotationRequestItemEvent(this, result);
      applicationEventPublisher.publishEvent(requestItemEvent);
      ResponseDTO response = new ResponseDTO("QUOTATION_ASSIGNMENT_SUCCESSFUL", SUCCESS, result);
      return ResponseEntity.ok(response);
    } catch (Exception e) {
      e.printStackTrace();
    }
    return failedResponse("QUOTATION_ASSIGNMENT_FAILED");
  }

  @PutMapping(value = "/quotations/{quotationId}/assignRequestDocument/{requestDocumentId}")
  @PreAuthorize("hasRole('ROLE_PROCUREMENT_OFFICER')")
  public ResponseEntity<?> assignRequestDocumentToQuotation(
      @PathVariable("quotationId") int quotationId,
      @PathVariable("requestDocumentId") int requestDocumentId) {
    RequestDocument requestDocument = documentService.findById(requestDocumentId);
    if (requestDocument != null) {
      Quotation result =
          quotationService.assignRequestDocumentToQuotation(quotationId, requestDocument);
      ResponseDTO response = new ResponseDTO("", SUCCESS, result);
      return ResponseEntity.ok(response);
    }
    return failedResponse("UPDATE_FAILED");
  }

  @GetMapping(value = "/quotations/supplierRequest")
  public ResponseEntity<?> testDoc() {
    List<SupplierRequest> supplierRequests = new ArrayList<>();
    try {
      List<Supplier> suppliers = supplierService.findSuppliersWithoutDocumentInQuotation();
      for (Supplier s : suppliers) {
        Set<RequestItem> res = requestItemService.findRequestItemsForSupplier(s.getId());

        if (!res.isEmpty()) {
          SupplierRequest supplierRequest = new SupplierRequest();
          supplierRequest.setRequests(res);
          supplierRequest.setSupplierName(s.getName());
          supplierRequest.setSupplierId(s.getId());
          supplierRequests.add(supplierRequest);
        }
      }
      ResponseDTO response = new ResponseDTO("FETCH_SUCCESSFUL", SUCCESS, supplierRequests);
      return ResponseEntity.ok(response);
    } catch (Exception e) {
      e.printStackTrace();
    }
    return failedResponse("FAILED");
  }

  private ResponseEntity<ResponseDTO> failedResponse(String message) {
    ResponseDTO failed = new ResponseDTO(message, ERROR, null);
    return ResponseEntity.badRequest().body(failed);
  }
}
