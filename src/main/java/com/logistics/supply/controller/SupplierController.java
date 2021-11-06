package com.logistics.supply.controller;

import com.logistics.supply.dto.ResponseDTO;
import com.logistics.supply.dto.SupplierDTO;
import com.logistics.supply.model.Supplier;
import com.logistics.supply.service.SupplierService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Optional;

import static com.logistics.supply.util.Constants.ERROR;
import static com.logistics.supply.util.Constants.SUCCESS;

@Slf4j
@RestController
@RequestMapping("/api")
public class SupplierController {
  @Autowired SupplierService supplierService;

  @PostMapping(value = "/suppliers")
  public ResponseEntity<?> createSupplier(@Valid @RequestBody SupplierDTO supplierDTO) {
    Supplier supplier = new Supplier();
    BeanUtils.copyProperties(supplierDTO, supplier);
    try {
      Supplier s = supplierService.add(supplier);
      ResponseDTO response = new ResponseDTO("SUPPLIER_CREATED_SUCCESSFULLY", SUCCESS, s);
      return ResponseEntity.ok(response);
    } catch (Exception e) {
      log.error(e.getMessage());
    }
    return failedResponse("SUPPLIER_CREATION_FAILED");
  }

  @GetMapping(value = "/suppliers")
  public ResponseEntity<?> getAllSuppliers(
      @RequestParam(required = false, name = "suppliersForRequestProcurement")
          boolean suppliersForRequest,
      @RequestParam(required = false, name = "suppliersWithRQ") boolean suppliersWithRQ) {
    List<Supplier> suppliers;
    try {
      if (suppliersForRequest) {
        suppliers = supplierService.findSuppliersWithNonFinalProcurement();
        ResponseDTO response = new ResponseDTO("FETCH_SUCCESSFUL", SUCCESS, suppliers);
        return ResponseEntity.ok(response);
      }
      if (suppliersWithRQ) {
        suppliers = supplierService.findSuppliersWithQuotationForLPO();
        ResponseDTO response = new ResponseDTO("FETCH_SUCCESSFUL", SUCCESS, suppliers);
        return ResponseEntity.ok(response);
      }
      suppliers = supplierService.getAll();
      ResponseDTO response = new ResponseDTO("FETCH_SUCCESSFUL", SUCCESS, suppliers);
      return ResponseEntity.ok(response);
    } catch (Exception e) {
      log.error(e.getMessage());
    }
    return failedResponse("FETCH_FAILED");
  }

  @DeleteMapping(value = "/suppliers/{supplierId}")
  public ResponseEntity<?> deleteSupplier(@PathVariable int supplierId) {
    try {
      supplierService.delete(supplierId);
      ResponseDTO response = new ResponseDTO("SUPPLIER_DELETED", SUCCESS, null);
      return ResponseEntity.ok(response);
    } catch (Exception e) {
      log.error(e.getMessage());
    }
    return failedResponse("DELETE_SUPPLIER_FAILED");
  }

  @GetMapping(value = "/suppliers/{supplierId}")
  public ResponseEntity<?> getSupplier(@PathVariable("supplierId") int supplierId) {
    try {
      Optional<Supplier> supplier = supplierService.findBySupplierId(supplierId);
      if (!supplier.isPresent()) {
        return failedResponse("SUPPLIER_NOT_FOUND");
      }
      ResponseDTO response = new ResponseDTO("SUPPLIER_FOUND", SUCCESS, supplier.get());
      return ResponseEntity.ok(response);
    } catch (Exception e) {
      log.error(e.getMessage());
    }
    return failedResponse("FETCH_FAILED");
  }

  @PutMapping(value = "/suppliers/{supplierId}")
  public ResponseEntity<?> updateSupplier(
      @PathVariable("supplierId") int supplierId, @Valid @RequestBody SupplierDTO supplierDTO) {

    try {
      Supplier updated = supplierService.edit(supplierId, supplierDTO);
      System.out.println("updated = " + updated);
      if(updated != null) {
        ResponseDTO response = new ResponseDTO("UPDATE_SUCCESSFUL", SUCCESS, updated);
        return ResponseEntity.ok(response);
      }

    } catch (Exception e) {
      log.error(e.getMessage());
    }
    return failedResponse("UPDATE_FAILED");
  }

  private ResponseEntity<ResponseDTO> failedResponse(String message) {
    ResponseDTO failed = new ResponseDTO(message, ERROR, null);
    return ResponseEntity.badRequest().body(failed);
  }
}
