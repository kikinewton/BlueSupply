package com.logistics.supply.controller;

import com.logistics.supply.configuration.AsyncConfig;
import com.logistics.supply.dto.*;
import com.logistics.supply.errorhandling.GeneralException;
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
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.*;
import java.net.URLConnection;
import java.text.MessageFormat;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

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
  private final SupplierRequestMapRepository supplierRequestMapRepository;
  private final RequestDocumentService documentService;
  private final ApplicationEventPublisher applicationEventPublisher;

  @PostMapping(value = "/quotations")
  @PreAuthorize("hasRole('ROLE_PROCUREMENT_OFFICER') or hasRole('ROLE_PROCUREMENT_MANAGER')")
  public ResponseEntity<?> createQuotation(
      @Valid @RequestBody CreateQuotationRequest quotationRequest) {
    RequestDocument doc = documentService.findById(quotationRequest.getDocumentId());

    try {
      if (Objects.nonNull(doc)) {
        Quotation quotation = new Quotation();
        Supplier supplier =
            supplierService.findBySupplierId(quotationRequest.getSupplierId());
        quotation.setSupplier(supplier);
        long count = quotationService.count();

        String ref = IdentifierUtil.idHandler("QUO", supplier.getName(), String.valueOf(count));
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
                      supplierRequestMapRepository.updateDocumentStatus(res.getId(), supplier.getId());
                      return res;
                    }
                    return null;
                  })
              .collect(Collectors.toSet());
          ResponseDTO response =
              new ResponseDTO("QUOTATION ASSIGNED TO REQUEST ITEMS", SUCCESS, savedQuotation);
          return ResponseEntity.ok(response);
        }
        return failedResponse("QUOTATION NOT CREATED");
      }
    } catch (Exception e) {
      log.error(e.toString());
    }
    return failedResponse("DOCUMENT DOES NOT EXIST");
  }

  @Operation(
      summary = "Present response of quotation with related request items",
      tags = "QUOTATION")
  @GetMapping(value = "/quotations/suppliers/{supplierId}")
  @PreAuthorize("hasRole('ROLE_GENERAL_MANAGER') or hasRole('ROLE_PROCUREMENT_OFFICER')")
  public ResponseEntity<?> getQuotationsBySupplier(@PathVariable("supplierId") int supplierId) throws GeneralException {

    Supplier supplier = supplierService.findBySupplierId(supplierId);

    Set<Quotation> quotations = new HashSet<>();
    quotations.addAll(quotationService.findBySupplier(supplierId));
    if (quotations.isEmpty()) return failedResponse("NO REQUEST ITEM LINKED TO QUOTATION");
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

    ResponseDTO response = new ResponseDTO("FETCHED QUOTATIONS BY SUPPLIER", SUCCESS, res);
    return ResponseEntity.ok(response);
  }

  @Operation(
      summary =
          "Gets all the quotations, those with link to lpo (linkedToLpo = true) and those without link to lpo (notLinkedToLpo = true)",
      tags = "QUOTATION")
  @GetMapping(value = "/quotations")
  @PreAuthorize(
      "hasRole('ROLE_GENERAL_MANAGER') or hasRole('ROLE_PROCUREMENT_OFFICER') or hasRole('ROLE_PROCUREMENT_MANAGER')")
  public ResponseEntity<?> getAllQuotations(
      @RequestParam(required = false) Optional<Boolean> linkedToLpo) {
    try {
      Set<Quotation> quotations = new HashSet<>();
      if (linkedToLpo.isPresent() && linkedToLpo.get()) {
        quotations.addAll(quotationService.findQuotationLinkedToLPO());
        List<QuotationAndRelatedRequestItemsDTO> result =
            pairQuotationsRelatedWithRequestItems(quotations);
        ResponseDTO response = new ResponseDTO("FETCH ALL QUOTATIONS", SUCCESS, result);
        return ResponseEntity.ok(response);
      } else if (linkedToLpo.isPresent() && !linkedToLpo.get()) {

        quotations.addAll(quotationService.findQuotationNotExpiredAndNotLinkedToLpo());
        // pair the quotations with their related request items
        List<QuotationAndRelatedRequestItemsDTO> result =
            pairQuotationsRelatedWithRequestItems(quotations);
        ResponseDTO response = new ResponseDTO("FETCH ALL QUOTATIONS", SUCCESS, result);
        return ResponseEntity.ok(response);
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
          new ResponseDTO("FETCH QUOTATIONS WITHOUT DOCUMENTS", SUCCESS, reqQuotations);
      return ResponseEntity.ok(response);
    } catch (Exception e) {
      log.error(e.toString());
    }
    return failedResponse("FAILED TO FETCH QUOTATIONS");
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
      @PathVariable("requestDocumentId") int requestDocumentId) {
    RequestDocument requestDocument = documentService.findById(requestDocumentId);
    if (requestDocument != null) {
      Quotation result =
          quotationService.assignRequestDocumentToQuotation(quotationId, requestDocument);
      ResponseDTO response = new ResponseDTO("", SUCCESS, result);
      return ResponseEntity.ok(response);
    }
    return failedResponse("UPDATE FAILED");
  }

  @GetMapping(value = "/quotations/supplierRequest")
  public ResponseEntity<?> testDoc(@RequestParam("registered") Optional<Boolean> registered) {
    try {
      if (registered.isPresent() && registered.get() == true) {
        List<Supplier> registeredSuppliers = supplierService.findSupplierWithNoDocFromSRM();
        List<SupplierRequest> supplierRequests = getRequestSupplierPair(registeredSuppliers);
        ResponseDTO response = new ResponseDTO("FETCH SUCCESSFUL", SUCCESS, supplierRequests);
        return ResponseEntity.ok(response);
      }
      if (registered.isPresent() && registered.get() == false) {
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

  @Async(AsyncConfig.TASK_EXECUTOR_CONTROLLER)
  @Operation(summary = "Generate quotation for unregistered suppliers", tags = "QUOTATION")
  @PostMapping(value = "/quotations/generateQuoteForSupplier")
  public CompletableFuture<Object> generateQuoteForSupplier9(
      @RequestBody GeneratedQuoteDTO request, HttpServletResponse response) {
    try {

      CompletableFuture<File> fileCompletableFuture =
          generatedQuoteService.createQuoteForUnregisteredSupplier(request);
      return fileCompletableFuture
          .thenApplyAsync(
              (file) -> {
                String mimeType = URLConnection.guessContentTypeFromName(file.getName());
                if (mimeType == null) {
                  mimeType = "application/octet-stream";
                }
                response.setContentType(mimeType);
                response.setHeader(
                    "Content-Disposition",
                    String.format("inline; filename=\"" + file.getName() + "\""));

                response.setContentLength((int) file.length());

                InputStream inputStream = null;
                try {
                  inputStream = new BufferedInputStream(new FileInputStream(file));
                } catch (FileNotFoundException e) {
                  log.error(e.toString());
                }
                String epoch = String.valueOf(System.currentTimeMillis());
                String fileName =
                    MessageFormat.format(
                        "supplier_{0}_{1}.pdf", request.getSupplier().getId(), epoch);
                Map<String, InputStream> fileResponse = new HashMap<>();
                fileResponse.put(fileName, inputStream);
                return fileResponse;
              })
          .thenApplyAsync(
              (res) -> {
                Optional<String> fileName = res.keySet().stream().findFirst();
                if (fileName.isPresent()) {
                  return documentService
                      .storePdfFile(res.get(fileName.get()), fileName.get())
                      .thenApplyAsync(
                          i -> {
                            ResponseDTO resp =
                                new ResponseDTO<>(
                                    "GENERATE QUOTATION DOCUMENTS SUCCESSFUL", "SUCCESS", i);
                            return ResponseEntity.ok(resp);
                          });
                }
                return null;
              });

    } catch (Exception e) {
      log.error(e.toString());
    }
    return CompletableFuture.supplyAsync(
        () -> failedResponse("FAILED TO GENERATE QUOTATION DOCUMENT"));
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
