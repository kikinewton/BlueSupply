package com.logistics.supply.controller;

import com.logistics.supply.dto.MapQuotationsToRequestItemsDTO;
import com.logistics.supply.dto.QuotationDTO;
import com.logistics.supply.dto.ResponseDTO;
import com.logistics.supply.event.AssignQuotationEventListener;
import com.logistics.supply.event.AssignQuotationRequestItemEvent;
//import com.logistics.supply.event.BulkRequestItemEvent;
import com.logistics.supply.model.Quotation;
import com.logistics.supply.model.RequestItem;
import com.logistics.supply.model.Supplier;
import com.logistics.supply.repository.QuotationRepository;
import com.logistics.supply.repository.RequestItemRepository;
import com.logistics.supply.service.AbstractRestService;
import com.sun.org.apache.xpath.internal.operations.Quo;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.crypto.ec.ECElGamalDecryptor;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

import static com.logistics.supply.util.CommonHelper.getNullPropertyNames;
import static com.logistics.supply.util.Constants.ERROR;
import static com.logistics.supply.util.Constants.SUCCESS;

@Slf4j
@RestController
@RequestMapping("/api")
public class QuotationController extends AbstractRestService {

  @Autowired private RequestItemRepository requestItemRepository;
  @Autowired private QuotationRepository quotationRepository;
  @Autowired private ApplicationEventPublisher applicationEventPublisher;

  @PostMapping(value = "/quotations")
  @Secured(value = "ROLE_PROCUREMENT_OFFICER")
  public ResponseDTO<Quotation> addQuotation(@RequestBody QuotationDTO quotationDTO) {
    String[] nullValues = getNullPropertyNames(quotationDTO);
    System.out.println("count of null properties: " + Arrays.stream(nullValues).count());

    Set<String> l = new HashSet<>(Arrays.asList(nullValues));
    if (l.size() > 0) {
      return new ResponseDTO<>(HttpStatus.BAD_REQUEST.name(), null, ERROR);
    }

    Quotation q =
        quotationService.findByRequestDocumentId(quotationDTO.getRequestDocument().getId());
    if (Objects.nonNull(q)) return new ResponseDTO<>(HttpStatus.BAD_REQUEST.name(), null, ERROR);

    Quotation quotation = new Quotation();
    BeanUtils.copyProperties(quotationDTO, quotation);
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

    Set<RequestItem> items =
        mappingDTO.getRequestItems().stream()
            .filter(i -> requestItemRepository.existsById(i.getId()))
            .map(r -> requestItemRepository.findById(r.getId()).get())
            .collect(Collectors.toSet());
    Set<Quotation> quotations =
        mappingDTO.getQuotations().stream()
            .filter(q -> quotationRepository.existsById(q.getId()))
            .map(p -> quotationRepository.findById(p.getId()).get())
            .collect(Collectors.toSet());
    try {
      List<RequestItem> result =
          items.stream()
              .map(i -> quotationService.assignToRequestItem(i, quotations))
              .collect(Collectors.toList());

      AssignQuotationRequestItemEvent requestItemEvent = new AssignQuotationRequestItemEvent(this, result);
      applicationEventPublisher.publishEvent(requestItemEvent);
      return new ResponseDTO<>(HttpStatus.OK.name(), result, SUCCESS);
    } catch (Exception e) {
      e.printStackTrace();
    }
    return new ResponseDTO<>(HttpStatus.BAD_REQUEST.name(), null, ERROR);
  }
}
