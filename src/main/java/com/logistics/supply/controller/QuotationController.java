package com.logistics.supply.controller;

import com.logistics.supply.dto.QuotationDTO;
import com.logistics.supply.dto.ResponseDTO;
import com.logistics.supply.model.Quotation;
import com.logistics.supply.model.Supplier;
import com.logistics.supply.service.AbstractRestService;
import com.sun.org.apache.xpath.internal.operations.Quo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.*;

import static com.logistics.supply.util.CommonHelper.getNullPropertyNames;
import static com.logistics.supply.util.Constants.ERROR;
import static com.logistics.supply.util.Constants.SUCCESS;

@Slf4j
@RestController
@RequestMapping("/api")
public class QuotationController extends AbstractRestService {

  @PostMapping(value = "/quotations")
  @Secured(value = "ROLE_PROCUREMENT_OFFICER")
  public ResponseDTO<Quotation> addQuotation(@RequestBody QuotationDTO quotationDTO) {
    String[] nullValues = getNullPropertyNames(quotationDTO);
    System.out.println("count of null properties: " + Arrays.stream(nullValues).count());

    Set<String> l = new HashSet<>(Arrays.asList(nullValues));
    if (l.size() > 0) {
      return new ResponseDTO<>(HttpStatus.BAD_REQUEST.name(), null, ERROR);
    }

    Quotation q = quotationService.findByRequestDocumentId(quotationDTO.getRequestDocument().getId());
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
  public ResponseDTO<List<Quotation>> getQuotationsBySupplier(
      @PathVariable("supplierId") int supplierId) {
    Optional<Supplier> supplier = supplierService.findBySupplierId(supplierId);
    if (!supplier.isPresent()) return new ResponseDTO<>(HttpStatus.BAD_REQUEST.name(), null, ERROR);
    List<Quotation> quotations = new ArrayList<>();
    try {
      quotations.addAll(quotationService.findBySupplier(supplierId));
      return new ResponseDTO<>(HttpStatus.OK.name(), quotations, SUCCESS);
    } catch (Exception e) {
      e.printStackTrace();
    }
    return new ResponseDTO<>(HttpStatus.BAD_REQUEST.name(), null, ERROR);
  }
}
