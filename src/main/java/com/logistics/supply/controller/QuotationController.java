package com.logistics.supply.controller;

import com.logistics.supply.dto.*;
import com.logistics.supply.event.AssignQuotationRequestItemEvent;
import com.logistics.supply.model.Quotation;
import com.logistics.supply.model.RequestDocument;
import com.logistics.supply.model.RequestItem;
import com.logistics.supply.model.Supplier;
import com.logistics.supply.repository.QuotationRepository;
import com.logistics.supply.repository.RequestDocumentRepository;
import com.logistics.supply.repository.RequestItemRepository;
import com.logistics.supply.repository.SupplierRequestMapRepository;
import com.logistics.supply.service.AbstractRestService;
import com.logistics.supply.service.RequestDocumentService;
import lombok.extern.slf4j.Slf4j;
import lombok.var;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.stream.Collectors;

import static com.logistics.supply.util.Constants.ERROR;
import static com.logistics.supply.util.Constants.SUCCESS;

// import com.logistics.supply.event.BulkRequestItemEvent;

@Slf4j
@RestController
@RequestMapping("/api")
public class QuotationController extends AbstractRestService {

  @Autowired SupplierRequestMapRepository supplierRequestMapRepository;
  private RequestDocumentService documentService;
  @Autowired private RequestItemRepository requestItemRepository;
  @Autowired private QuotationRepository quotationRepository;
  @Autowired private ApplicationEventPublisher applicationEventPublisher;
  @Autowired private RequestDocumentRepository requestDocumentRepository;
  public QuotationController(RequestDocumentService documentService) {
    this.documentService = documentService;
  }

  @PostMapping(value = "/quotations/suppliers/{supplierId}")
  @PreAuthorize("hasRole('ROLE_PROCUREMENT_OFFICER')")
  public ResponseDTO<Quotation> createQuotation(
      @PathVariable("supplierId") int supplierId,
      @RequestParam("file") MultipartFile multipartFile,
      Authentication authentication) {

    RequestDocument doc = documentService.storeFile(multipartFile, authentication.getName(), "");
    Quotation savedQuotation;
    if (Objects.nonNull(doc)) {
      Quotation quotation = new Quotation();
      Supplier s = supplierService.findBySupplierId(supplierId).get();
      quotation.setSupplier(s);
      quotation.setRequestDocument(doc);
      savedQuotation = quotationService.save(quotation);
      if (Objects.nonNull(savedQuotation)) {
        Set<RequestItem> requestItems = requestItemService.findRequestItemsForSupplier(supplierId);
        var result =
            requestItems.stream()
                .map(
                    r -> {
                      r.getQuotations().add(savedQuotation);
                      RequestItem res = requestItemRepository.save(r);
                      if (Objects.nonNull(res)) {
                        supplierRequestMapRepository.updateDocumentStatus(res.getId(), s.getId());
                        return res;
                      }
                      return null;
                    })
                .collect(Collectors.toSet());
        result.forEach(System.out::println);
        return new ResponseDTO<>(HttpStatus.OK.name(), savedQuotation, SUCCESS);
      }

      return new ResponseDTO<>(HttpStatus.BAD_REQUEST.name(), null, "QUOTATION NOT CREATED");
    }
    return new ResponseDTO<>(HttpStatus.BAD_REQUEST.name(), null, ERROR);
  }

  @GetMapping(value = "/quotations/{supplierId}")
  @PreAuthorize("hasRole('ROLE_PROCUREMENT_OFFICER')")
  public ResponseDTO<List<Quotation>> getQuotationsBySupplier(
      @PathVariable("supplierId") int supplierId) {
    System.out.println(supplierId);
    Optional<Supplier> supplier = supplierService.findBySupplierId(supplierId);
    if (!supplier.isPresent()) return new ResponseDTO<>(HttpStatus.BAD_REQUEST.name(), null, ERROR);
    List<Quotation> quotations = new ArrayList<>();
    try {
      quotations.addAll(quotationService.findBySupplier(supplier.get().getId()));
      if (quotations.size() > 0) {
        return new ResponseDTO<>(HttpStatus.OK.name(), quotations, SUCCESS);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    return new ResponseDTO<>(HttpStatus.BAD_REQUEST.name(), null, ERROR);
  }

  @GetMapping(value = "/quotations/all")
  @PreAuthorize("hasRole('ROLE_GENERAL_MANAGER') or hasRole('ROLE_PROCUREMENT_OFFICER')")
  public ResponseDTO<List<Quotation>> getAllQuotations() {
    List<Quotation> quotations = new ArrayList<>();
    try {
      quotations.addAll(quotationService.findAll());
      return new ResponseDTO<>(HttpStatus.OK.name(), quotations, SUCCESS);
    } catch (Exception e) {
      e.printStackTrace();
    }
    return new ResponseDTO<>(HttpStatus.BAD_REQUEST.name(), null, ERROR);
  }

  @GetMapping(value = "/quotations/withoutDocument")
  @PreAuthorize("hasRole('ROLE_GENERAL_MANAGER') or hasRole('ROLE_PROCUREMENT_OFFICER')")
  public ResponseDTO<List<RequestQuotationDTO>> getAllQuotationsWithoutDocument() {
    List<RequestQuotationDTO> reqQuotations = new ArrayList<>();
    try {
      reqQuotations.addAll(quotationService.findQuotationsWithoutAssignedDocument());
      return new ResponseDTO<>(HttpStatus.OK.name(), reqQuotations, SUCCESS);
    } catch (Exception e) {
      e.printStackTrace();
    }
    return new ResponseDTO<>(HttpStatus.BAD_REQUEST.name(), null, ERROR);
  }

  @GetMapping(value = "/quotations/pair")
  public ResponseDTO<List<RequestQuotationPair>> testController() {
    List<RequestQuotationPair> pair = new ArrayList<>();
    pair.addAll(quotationRepository.findQuotationRequestItemPairId());
    return new ResponseDTO<>(HttpStatus.OK.name(), pair, SUCCESS);
  }

  @PutMapping(value = "/quotations/assignToRequestItems")
  @PreAuthorize("hasRole('ROLE_PROCUREMENT_OFFICER')")
  public ResponseDTO<List<RequestItem>> assignQuotationsToRequestItems(
      @RequestBody MapQuotationsToRequestItemsDTO mappingDTO) {

    System.out.println("mappingDTO size = " + mappingDTO.getQuotations().size());
    Set<RequestItem> items =
        mappingDTO.getRequestItems().stream()
            .filter(i -> requestItemRepository.existsById(i.getId()))
            .map(r -> requestItemRepository.findById(r.getId()).get())
            .collect(Collectors.toSet());
    System.out.println("items size = " + items.size());

    Set<Quotation> quotations =
        mappingDTO.getQuotations().stream()
            .filter(q -> quotationRepository.existsById(q.getId()))
            .map(p -> quotationRepository.findById(p.getId()).get())
            .collect(Collectors.toSet());
    System.out.println("quotations = " + quotations.size());

    try {
      List<RequestItem> result =
          items.stream()
              .peek(System.out::println)
              .map(i -> quotationService.assignToRequestItem(i, quotations))
              .collect(Collectors.toList());

      AssignQuotationRequestItemEvent requestItemEvent =
          new AssignQuotationRequestItemEvent(this, result);
      applicationEventPublisher.publishEvent(requestItemEvent);
      return new ResponseDTO<>(HttpStatus.OK.name(), result, SUCCESS);
    } catch (Exception e) {
      e.printStackTrace();
    }
    return new ResponseDTO<>(HttpStatus.BAD_REQUEST.name(), null, ERROR);
  }

  @PutMapping(value = "/quotations/{quotationId}/assignRequestDocument/{requestDocumentId}")
  @PreAuthorize("hasRole('ROLE_PROCUREMENT_OFFICER')")
  public ResponseDTO<Quotation> assignRequestDocumentToQuotation(
      @PathVariable("quotationId") int quotationId,
      @PathVariable("requestDocumentId") int requestDocumentId) {
    Optional<RequestDocument> requestDocument =
        requestDocumentRepository.findById(requestDocumentId);
    if (requestDocument.isPresent()) {
      Quotation result =
          quotationService.assignRequestDocumentToQuotation(quotationId, requestDocument.get());
      return new ResponseDTO<>(HttpStatus.OK.name(), result, SUCCESS);
    }
    return new ResponseDTO<>(HttpStatus.BAD_REQUEST.name(), null, ERROR);
  }

  @GetMapping(value = "/quotations/supplierRequest")
  public ResponseDTO<List<SupplierRequest>> testDoc() {
    List<SupplierRequest> supplierRequests = new ArrayList<>();
    try {
      List<Supplier> suppliers = supplierService.findSupplierWithNoDocFromSRM();
      for (Supplier s : suppliers) {
        Set<RequestItem> res = requestItemService.findRequestItemsForSupplier(s.getId());

        if (res.size() > 0) {
          SupplierRequest supplierRequest = new SupplierRequest();
          supplierRequest.setRequests(res);
          supplierRequest.setSupplierName(s.getName());
          supplierRequest.setSupplierId(s.getId());
          supplierRequests.add(supplierRequest);
        }
      }

      return new ResponseDTO<>(HttpStatus.OK.name(), supplierRequests, SUCCESS);
    } catch (Exception e) {
      e.printStackTrace();
    }
    return new ResponseDTO<>(HttpStatus.BAD_REQUEST.name(), null, ERROR);
  }
}
