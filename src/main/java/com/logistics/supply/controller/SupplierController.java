package com.logistics.supply.controller;

import com.logistics.supply.dto.ResponseDTO;
import com.logistics.supply.dto.SupplierDTO;
import com.logistics.supply.model.Supplier;
import com.logistics.supply.service.AbstractRestService;
import com.logistics.supply.util.CommonHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static com.logistics.supply.util.Constants.ERROR;
import static com.logistics.supply.util.Constants.SUCCESS;

@Slf4j
@RestController
@RequestMapping("/api")
public class SupplierController extends AbstractRestService {

  @PostMapping(value = "/suppliers")
  public ResponseDTO createSupplier(@Valid @RequestBody SupplierDTO supplierDTO) {

    if (Objects.isNull(supplierDTO)) return new ResponseDTO("ERROR", HttpStatus.BAD_REQUEST.name());

    Supplier supplier = new Supplier();
    BeanUtils.copyProperties(supplierDTO, supplier);
    try {
      supplierService.add(supplier);
      return new ResponseDTO(SUCCESS, HttpStatus.CREATED.name());
    } catch (Exception e) {
      log.error(e.getMessage());
      e.printStackTrace();
    }
    return new ResponseDTO(ERROR, HttpStatus.BAD_REQUEST.name());
  }

  @GetMapping(value = "/suppliers")
  public ResponseDTO<List<Supplier>> getAllSuppliers(
      @RequestParam(required = false, name = "suppliersForRequestProcurement")
          boolean suppliersForRequest,
      @RequestParam(required = false, name = "suppliersWithRQ") boolean suppliersWithRQ) {
    List<Supplier> suppliers;
    try {
      if (suppliersForRequest) {
        suppliers = supplierService.findSuppliersWithNonFinalProcurement();
        //        if (suppliers.size() > 0)
        return new ResponseDTO<>(SUCCESS, suppliers, HttpStatus.FOUND.name());
      }
      if (suppliersWithRQ) {
        suppliers = supplierService.findSuppliersWithQuotationForLPO();
        return new ResponseDTO<>(SUCCESS, suppliers, HttpStatus.FOUND.name());
      }
      suppliers = supplierService.getAll();
      if (suppliers.size() > 0)
        return new ResponseDTO<>(SUCCESS, suppliers, HttpStatus.FOUND.name());
    } catch (Exception e) {
      log.error(e.getMessage());
      e.printStackTrace();
    }
    return new ResponseDTO<>(ERROR, null, HttpStatus.NOT_FOUND.name());
  }

  @DeleteMapping(value = "/suppliers/{supplierId}")
  public ResponseDTO deleteSupplier(@PathVariable int supplierId) {
    try {
      Optional<Supplier> supplier = supplierService.findBySupplierId(supplierId);
      if (supplier.isPresent()) {
        supplierService.delete(supplierId);
        return new ResponseDTO(SUCCESS, HttpStatus.OK.name());
      }
    } catch (Exception e) {
      log.error(e.getMessage());
      e.printStackTrace();
    }
    return new ResponseDTO(ERROR, HttpStatus.NOT_FOUND.name());
  }

  @GetMapping(value = "/suppliers/{supplierId}")
  public ResponseDTO<Supplier> getSupplier(@PathVariable int supplierId) {
    try {
      Optional<Supplier> supplier = supplierService.findBySupplierId(supplierId);
      if (!supplier.isPresent()) {
        return new ResponseDTO<Supplier>(HttpStatus.BAD_REQUEST.name(), null, "Supplier Not Found");
      }
      return new ResponseDTO<Supplier>(HttpStatus.OK.name(), supplier.get(), "Supplier Found");
    } catch (Exception e) {
      throw new RuntimeException(e.getMessage());
    }
  }

  @PutMapping(value = "/suppliers/{supplierId}")
  public ResponseDTO<Supplier> updateSupplier(
      @PathVariable int supplierId, @Valid @RequestBody SupplierDTO supplierDTO) {
    Optional<Supplier> supplier = supplierService.findBySupplierId(supplierId);

    if (Objects.isNull(supplier.get()))
      return new ResponseDTO<>(ERROR, null, HttpStatus.BAD_REQUEST.name());

    String[] nullValues = CommonHelper.getNullPropertyNames(supplierDTO);
    System.out.println("null properties: ");
    Arrays.stream(nullValues).forEach(System.out::println);

    try {
      Supplier updated = supplierService.edit(supplierId, supplierDTO);
      return new ResponseDTO<>(SUCCESS, updated, "SUPPLIER EDIT");
    } catch (Exception e) {
      log.error("Update failed", e);
      e.printStackTrace();
    }
    return new ResponseDTO<>(ERROR, null, "EDIT FAILED");
  }
}
