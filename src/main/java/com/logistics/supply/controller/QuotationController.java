package com.logistics.supply.controller;

import com.logistics.supply.dto.*;
import com.logistics.supply.errorhandling.GeneralException;
import com.logistics.supply.event.AssignQuotationRequestItemEvent;
import com.logistics.supply.model.Quotation;
import com.logistics.supply.model.RequestDocument;
import com.logistics.supply.model.RequestItem;
import com.logistics.supply.model.Supplier;
import com.logistics.supply.service.*;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.text.MessageFormat;
import java.util.*;
import java.util.stream.Collectors;

import static com.logistics.supply.util.Constants.FETCH_SUCCESSFUL;
import static com.logistics.supply.util.Constants.SUCCESS;
import static com.logistics.supply.util.Helper.failedResponse;
import static com.logistics.supply.util.Helper.notFound;

@Slf4j
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class QuotationController {
  private final GeneratedQuoteService generatedQuoteService;
  private final SupplierService supplierService;
  private final QuotationService quotationService;
  private final RequestItemService requestItemService;
  private final RequestDocumentService documentService;
  private final ApplicationEventPublisher applicationEventPublisher;

  @PostMapping(value = "/quotations")
  @PreAuthorize("hasRole('ROLE_PROCUREMENT_OFFICER') or hasRole('ROLE_PROCUREMENT_MANAGER')")
  public ResponseEntity<?> createQuotation(
      @Valid @RequestBody CreateQuotationRequest quotationRequest) throws GeneralException {
    Quotation savedQuotation = quotationService.createQuotation(quotationRequest);
    return ResponseDTO.wrapSuccessResult(savedQuotation, "QUOTATION ASSIGNED TO REQUEST ITEMS");
  }

  @GetMapping(value = "/quotations/suppliers/{supplierId}")
  @PreAuthorize("hasRole('ROLE_GENERAL_MANAGER') or hasRole('ROLE_PROCUREMENT_OFFICER')")
  public ResponseEntity<?> getQuotationsBySupplier(@PathVariable("supplierId") int supplierId)
      throws GeneralException {
    List<SupplierQuotationDTO> supplierQuotation =
        quotationService.findSupplierQuotation(supplierId);
    return ResponseDTO.wrapSuccessResult(supplierQuotation, "FETCHED QUOTATIONS BY SUPPLIER");
  }

  @Operation(
      summary =
          "Gets all the quotations, those with link to lpo (linkedToLpo = true) and those without link to lpo (notLinkedToLpo = true)",
      tags = "QUOTATION")
  @GetMapping(value = "/quotations")
  @PreAuthorize(
      "hasRole('ROLE_GENERAL_MANAGER') or hasRole('ROLE_PROCUREMENT_OFFICER') or hasRole('ROLE_PROCUREMENT_MANAGER')")
  public ResponseEntity<?> getAllQuotations(
      @RequestParam(required = false) Optional<Boolean> linkedToLpo,
      @RequestParam Optional<Boolean> underReview) {
    try {
      Set<Quotation> quotations = new HashSet<>();
      if (linkedToLpo.isPresent() && linkedToLpo.get()) {
        quotations.addAll(quotationService.findQuotationLinkedToLPO());
        List<QuotationAndRelatedRequestItemsDTO> result =
            pairQuotationsRelatedWithRequestItems(quotations);
        return ResponseDTO.wrapSuccessResult(result, "FETCH ALL QUOTATIONS");

      } else if (linkedToLpo.isPresent() && !linkedToLpo.get()) {

        quotations.addAll(quotationService.findQuotationNotExpiredAndNotLinkedToLpo());
        // pair the quotations with their related request items
        List<QuotationAndRelatedRequestItemsDTO> result =
            pairQuotationsRelatedWithRequestItems(quotations);
        ResponseDTO response = new ResponseDTO("FETCH ALL QUOTATIONS", SUCCESS, result);
        return ResponseEntity.ok(response);
      }

      if (underReview.isPresent() && underReview.get()) {
        quotations.addAll(quotationService.findQuotationLinkedToLPO());
        quotations.removeIf(q -> q.isReviewed() == true);
        List<QuotationAndRelatedRequestItemsDTO> quotationAndRelatedRequestItemsDTOS =
            pairQuotationsRelatedWithRequestItems(quotations);
        return ResponseDTO.wrapSuccessResult(quotationAndRelatedRequestItemsDTOS, FETCH_SUCCESSFUL);
      }

      quotations.addAll(quotationService.findAll());
      ResponseDTO response = new ResponseDTO("FETCH ALL QUOTATIONS", SUCCESS, quotations);
      return ResponseEntity.ok(response);
    } catch (Exception e) {
      log.error(e.toString());
    }
    return notFound("NO QUOTATION FOUND");
  }

  private List<QuotationAndRelatedRequestItemsDTO> pairQuotationsRelatedWithRequestItems(
      Set<Quotation> quotations) {
    List<QuotationAndRelatedRequestItemsDTO> data = new ArrayList<>();
    quotations.forEach(
        x -> {
          QuotationAndRelatedRequestItemsDTO qri = new QuotationAndRelatedRequestItemsDTO();
          qri.setQuotation(x);
          List<RequestItem> requestItems = requestItemService.findItemsUnderQuotation(x.getId());
          List<RequestItemDTO> requestItemDTOList = new ArrayList<>();
          for (RequestItem requestItem : requestItems) {
            RequestItemDTO requestItemDTO = RequestItemDTO.toDto(requestItem);
            requestItemDTOList.add(requestItemDTO);
          }
          qri.setRequestItems(requestItemDTOList);
          data.add(qri);
        });
    return data;
  }

  @Operation(summary = "Assign quotations to request items")
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
              .map(i -> quotationService.assignToRequestItem(i, quotations))
              .collect(Collectors.toList());

      AssignQuotationRequestItemEvent requestItemEvent =
          new AssignQuotationRequestItemEvent(this, result);
      applicationEventPublisher.publishEvent(requestItemEvent);
      ResponseDTO response = new ResponseDTO("QUOTATION ASSIGNMENT SUCCESSFUL", SUCCESS, result);
      return ResponseEntity.ok(response);
    } catch (Exception e) {
      log.error(e.toString());
    }
    return failedResponse("QUOTATION ASSIGNMENT FAILED");
  }

  @Operation(summary = "Assign document to quotation", tags = "QUOTATION")
  @PutMapping(value = "/quotations/{quotationId}/assignRequestDocument/{requestDocumentId}")
  @PreAuthorize("hasRole('ROLE_PROCUREMENT_OFFICER')")
  public ResponseEntity<?> assignRequestDocumentToQuotation(
      @PathVariable("quotationId") int quotationId,
      @PathVariable("requestDocumentId") int requestDocumentId)
      throws GeneralException {
    RequestDocument requestDocument = documentService.findById(requestDocumentId);
    Quotation result =
        quotationService.assignRequestDocumentToQuotation(quotationId, requestDocument);
    return ResponseDTO.wrapSuccessResult(result, "ASSIGN DOCUMENT SUCCESSFUL");
  }

  @GetMapping(value = "/quotations/supplierRequest")
  public ResponseEntity<?> testDoc(@RequestParam("registered") Optional<Boolean> registered) {
    try {
      if (registered.isPresent() && registered.get()) {
        List<Supplier> registeredSuppliers = supplierService.findSupplierWithNoDocFromSRM();
        List<SupplierRequest> supplierRequests = getRequestSupplierPair(registeredSuppliers);
        ResponseDTO response = new ResponseDTO("FETCH SUCCESSFUL", SUCCESS, supplierRequests);
        return ResponseEntity.ok(response);
      }
      if (registered.isPresent() && !registered.get()) {
        List<Supplier> unRegSuppliers = supplierService.findUnRegisteredSupplierWithNoDocFromSRM();
        List<SupplierRequest> supplierRequests = getRequestSupplierPair(unRegSuppliers);
        ResponseDTO response = new ResponseDTO("FETCH SUCCESSFUL", SUCCESS, supplierRequests);
        return ResponseEntity.ok(response);
      }

    } catch (Exception e) {
      log.error(e.toString());
    }
    return failedResponse("FETCH REQUEST FAILED");
  }

  @Operation(summary = "Get the quotations without documents attached", tags = "QUOTATION")
  @GetMapping(value = "/requestItems/quotations")
  public ResponseEntity<?> findRequestItemsWithoutDocsInQuotation(
      @RequestParam("withoutDocs") Boolean withoutDocs) {

    List<RequestItem> items = requestItemService.findRequestItemsWithoutDocInQuotation();
    if (withoutDocs) {
      ResponseDTO response =
          new ResponseDTO("FETCH QUOTATIONS WITHOUT DOCUMENTS SUCCESSFUL", SUCCESS, items);
      return ResponseEntity.ok(response);
    }
    return notFound("NO QUOTATION FOUND");
  }

  @Operation(summary = "Generate quotation for unregistered suppliers", tags = "QUOTATION")
  @PostMapping(value = "/quotations/generateQuoteForSupplier")
  public ResponseEntity<ResponseDTO<RequestDocument>> generateQuoteForSupplier9(
      @RequestBody GeneratedQuoteDTO request) throws GeneralException, FileNotFoundException {

    File file = generatedQuoteService.createQuoteForUnregisteredSupplier(request);

    String epoch = String.valueOf(System.currentTimeMillis());
    String fileName =
        MessageFormat.format("supplier_{0}_{1}.pdf", request.getSupplier().getId(), epoch);

    BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(file));
    RequestDocument requestDocument = documentService.storePdfFile(inputStream, fileName);
    return ResponseDTO.wrapSuccessResult(requestDocument, "GENERATED QUOTATION");
  }

  private List<SupplierRequest> getRequestSupplierPair(List<Supplier> regSuppliers) {
    List<SupplierRequest> supplierRequests = new ArrayList<>();
    for (Supplier s : regSuppliers) {
      Set<RequestItem> res =
          requestItemService.findRequestItemsWithNoDocumentAttachedForSupplier(s.getId());

      if (!res.isEmpty()) {
        SupplierRequest supplierRequest = new SupplierRequest();
        supplierRequest.setRequests(res);
        supplierRequest.setSupplierName(s.getName());
        supplierRequest.setSupplierId(s.getId());
        supplierRequests.add(supplierRequest);
      }
    }
    return supplierRequests;
  }
}
