package com.logistics.supply.controller;

import com.logistics.supply.dto.*;
import com.logistics.supply.event.AssignQuotationRequestItemEvent;
import com.logistics.supply.model.Quotation;
import com.logistics.supply.model.RequestDocument;
import com.logistics.supply.model.RequestItem;
import com.logistics.supply.model.Supplier;
import com.logistics.supply.repository.SupplierRequestMapRepository;
import com.logistics.supply.service.*;
import com.logistics.supply.util.IdentifierUtil;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.var;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URLConnection;
import java.util.*;
import java.util.stream.Collectors;

import static com.logistics.supply.util.Constants.ERROR;
import static com.logistics.supply.util.Constants.SUCCESS;
import static com.logistics.supply.util.Helper.failedResponse;
import static com.logistics.supply.util.Helper.notFound;

@Slf4j
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class QuotationController {

  final GeneratedQuoteService generatedQuoteService;
  final SupplierService supplierService;
  final QuotationService quotationService;
  final RequestItemService requestItemService;
  final SupplierRequestMapRepository supplierRequestMapRepository;
  final RequestDocumentService documentService;

  @Autowired ApplicationEventPublisher applicationEventPublisher;

  @PostMapping(value = "/quotations")
  @PreAuthorize("hasRole('ROLE_PROCUREMENT_OFFICER') or hasRole('ROLE_PROCUREMENT_MANAGER')")
  public ResponseEntity<?> createQuotation(
      @Valid @RequestBody CreateQuotationRequest quotationRequest) {
    RequestDocument doc = documentService.findById(quotationRequest.getDocumentId());

    try {
      if (Objects.nonNull(doc)) {
        Quotation quotation = new Quotation();
        Optional<Supplier> supplier =
            supplierService.findBySupplierId(quotationRequest.getSupplierId());
        if (!supplier.isPresent()) return failedResponse("SUPPLIER_DOES_NOT_EXIST");
        Supplier s = supplier.get();
        quotation.setSupplier(s);
        long count = quotationService.count();

        String ref = IdentifierUtil.idHandler("QUO", s.getName(), String.valueOf(count));
        quotation.setQuotationRef(ref);
        quotation.setRequestDocument(doc);
        Quotation savedQuotation = quotationService.save(quotation);

        if (Objects.nonNull(savedQuotation)) {
          Set<RequestItem> requestItems =
              quotationRequest.getRequestItemIds().stream()
                  .map(x -> requestItemService.findById(x).get())
                  .collect(Collectors.toSet());
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
    } catch (Exception e) {
      log.error(e.toString());
    }
    return failedResponse("DOCUMENT_DOES_NOT_EXIST");
  }

  @Operation(summary = "Get the quotations related to department", tags = "QUOTATIONS")
  public ResponseEntity<?> getQuotationsForDepartment(Authentication authentication) {
    try {

    } catch (Exception e) {
      log.error(e.toString());
    }
    return null;
  }

  @Operation(
      summary = "Present response of quotation with related request items",
      tags = "QUOTATION")
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

  @Operation(
      summary =
          "Gets all the quotations, those with link to lpo (linkedToLpo = true) and those without link to lpo (notLinkedToLpo = true)",
      tags = "QUOTATION")
  @GetMapping(value = "/quotations")
  @PreAuthorize(
      "hasRole('ROLE_GENERAL_MANAGER') or hasRole('ROLE_PROCUREMENT_OFFICER') or hasRole('ROLE_PROCUREMENT_MANAGER')")
  public ResponseEntity<?> getAllQuotations(@RequestParam(required = false) Boolean linkedToLpo) {
    try {
      Set<Quotation> quotations = new HashSet<>();
      if (linkedToLpo) {
        quotations.addAll(quotationService.findQuotationLinkedToLPO());
        List<QuotationAndRelatedRequestItemsDTO> result =
                pairQuotationsRelatedWithRequestItems(quotations);
        ResponseDTO response = new ResponseDTO("FETCH_ALL_QUOTATIONS", SUCCESS, result);
        return ResponseEntity.ok(response);
      } else if (!linkedToLpo) {

        quotations.addAll(quotationService.findQuotationNotExpiredAndNotLinkedToLpo());
        // pair the quotations with their related request items
        List<QuotationAndRelatedRequestItemsDTO> result =
                pairQuotationsRelatedWithRequestItems(quotations);
        ResponseDTO response = new ResponseDTO("FETCH_ALL_QUOTATIONS", SUCCESS, result);
        return ResponseEntity.ok(response);
      }

      quotations.addAll(quotationService.findAll());
      List<QuotationAndRelatedRequestItemsDTO> result = pairQuotationsRelatedWithRequestItems(quotations);
      ResponseDTO response = new ResponseDTO("FETCH_ALL_QUOTATIONS", SUCCESS, result);
      return ResponseEntity.ok(response);
    } catch (Exception e) {
      log.error(e.toString());
    }
    return notFound("NO_QUOTATION_FOUND");
  }

  private List<QuotationAndRelatedRequestItemsDTO> pairQuotationsRelatedWithRequestItems(
      Set<Quotation> quotations) {
    List<QuotationAndRelatedRequestItemsDTO> data = new ArrayList<>();
    quotations.forEach(
        x -> {
          QuotationAndRelatedRequestItemsDTO qri =
              new QuotationAndRelatedRequestItemsDTO();
          qri.setQuotation(x);
          List<RequestItem> requestItems = requestItemService.findItemsUnderQuotation(x.getId());
          qri.setRequestItems(requestItems);
          data.add(qri);
        });
    return data;
  }

  @Operation(summary = "Get all quotations without document attached", tags = "QUOTATION")
  @GetMapping(value = "/quotations/withoutDocument")
  @PreAuthorize(
      "hasRole('ROLE_GENERAL_MANAGER') or hasRole('ROLE_PROCUREMENT_OFFICER') or hasRole('ROLE_PROCUREMENT_MANAGER')")
  public ResponseEntity<?> getAllQuotationsWithoutDocument() {

    try {
      List<RequestQuotationDTO> reqQuotations = new ArrayList<>();
      reqQuotations.addAll(quotationService.findQuotationsWithoutAssignedDocument());
      ResponseDTO response =
          new ResponseDTO("FETCH_QUOTATIONS_WITHOUT_DOCUMENTS", SUCCESS, reqQuotations);
      return ResponseEntity.ok(response);
    } catch (Exception e) {
      log.error(e.toString());
    }
    return failedResponse("FAILED_TO_FETCH_QUOTATIONS");
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
      ResponseDTO response = new ResponseDTO("QUOTATION_ASSIGNMENT_SUCCESSFUL", SUCCESS, result);
      return ResponseEntity.ok(response);
    } catch (Exception e) {
      log.error(e.toString());
    }
    return failedResponse("QUOTATION_ASSIGNMENT_FAILED");
  }

  @Operation(summary = "Assign document to quotation", tags = "QUOTATION")
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
      List<Supplier> suppliers = supplierService.findSupplierWithNoDocFromSRM();
      for (Supplier s : suppliers) {
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
      ResponseDTO response = new ResponseDTO("FETCH_SUCCESSFUL", SUCCESS, supplierRequests);
      return ResponseEntity.ok(response);
    } catch (Exception e) {
      log.error(e.toString());
    }
    return failedResponse("FAILED");
  }

  @Operation(summary = "Get the quotations without documents attached", tags = "QUOTATION")
  @GetMapping(value = "/requestItems/quotations")
  public ResponseEntity<?> findRequestItemsWithoutDocsInQuotation(
      @RequestParam("withoutDocs") Boolean withoutDocs) {

    List<RequestItem> items = requestItemService.findRequestItemsWithoutDocInQuotation();
    if (withoutDocs) {
      ResponseDTO response =
          new ResponseDTO("FETCH_QUOTATIONS_WITHOUT_DOCUMENTS_SUCCESSFUL", SUCCESS, items);
      return ResponseEntity.ok(response);
    }
    return notFound("NO_QUOTATION_FOUND");
  }

  @Operation(summary = "Generate quotation for unregistered suppliers", tags = "QUOTATION")
  @PostMapping(value = "/quotations/generateQuoteForSupplier")
  @PreAuthorize("hasRole('ROLE_PROCUREMENT_OFFICER')")
  public void generateQuoteForSupplier(
      @RequestBody GeneratedQuoteDTO request, HttpServletResponse response) {
    try {
      File file = generatedQuoteService.createQuoteForUnregisteredSupplier(request);
      if (Objects.isNull(file)) log.error("Quotation file generation failed");

      String mimeType = URLConnection.guessContentTypeFromName(file.getName());
      if (mimeType == null) {
        mimeType = "application/octet-stream";
      }
      response.setContentType(mimeType);
      response.setHeader(
          "Content-Disposition", String.format("inline; filename=\"" + file.getName() + "\""));

      response.setContentLength((int) file.length());

      InputStream inputStream = new BufferedInputStream(new FileInputStream(file));

      FileCopyUtils.copy(inputStream, response.getOutputStream());
    } catch (Exception e) {
      log.error(e.toString());
    }
  }


}
