package com.logistics.supply.controller;

import com.logistics.supply.dto.MapQuotationsToRequestItemsDTO;
import com.logistics.supply.dto.QuotationDTO;
import com.logistics.supply.dto.ResponseDTO;
import com.logistics.supply.event.AssignQuotationRequestItemEvent;
import com.logistics.supply.model.Quotation;
import com.logistics.supply.model.RequestDocument;
import com.logistics.supply.model.RequestItem;
import com.logistics.supply.model.Supplier;
import com.logistics.supply.repository.QuotationRepository;
import com.logistics.supply.repository.RequestDocumentRepository;
import com.logistics.supply.repository.RequestItemRepository;
import com.logistics.supply.service.AbstractRestService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

import static com.logistics.supply.util.CommonHelper.getNullPropertyNames;
import static com.logistics.supply.util.Constants.ERROR;
import static com.logistics.supply.util.Constants.SUCCESS;

// import com.logistics.supply.event.BulkRequestItemEvent;

@Slf4j
@RestController
@RequestMapping("/api")
public class QuotationController extends AbstractRestService {

  @Autowired private RequestItemRepository requestItemRepository;
  @Autowired private QuotationRepository quotationRepository;
  @Autowired private ApplicationEventPublisher applicationEventPublisher;
  @Autowired private RequestDocumentRepository requestDocumentRepository;

  @PostMapping(value = "/quotations")
  @Secured(value = "ROLE_PROCUREMENT_OFFICER")
  public ResponseDTO<Quotation> addQuotation(@RequestBody QuotationDTO quotationDTO) {
    String[] nullValues = getNullPropertyNames(quotationDTO);
    System.out.println("count of null properties: " + Arrays.stream(nullValues).count());

    Set<String> l = new HashSet<>(Arrays.asList(nullValues));
    if (l.size() > 0) {
      return new ResponseDTO<>(HttpStatus.BAD_REQUEST.name(), null, ERROR);
    }

    Optional<RequestDocument> rd =
        requestDocumentRepository.findById(quotationDTO.getRequestDocument().getId());
    Optional<Supplier> supplier =
        supplierService.findBySupplierId(quotationDTO.getSupplier().getId());
    if (!rd.isPresent())
      return new ResponseDTO<>(HttpStatus.BAD_REQUEST.name(), null, "REQUEST DOCUMENT NOT FOUND");

    if (!supplier.isPresent())
      return new ResponseDTO<>(HttpStatus.BAD_REQUEST.name(), null, "SUPPLIER NOT FOUND");

    Quotation quotation = new Quotation();
    BeanUtils.copyProperties(quotationDTO, quotation);
    quotation.setRequestDocument(rd.get());
    quotation.setSupplier(supplier.get());
    try {
      Quotation result = quotationService.save(quotation);
      return new ResponseDTO<>(HttpStatus.OK.name(), result, SUCCESS);
    } catch (Exception e) {
      log.error(e.getMessage());
      e.printStackTrace();
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
}
