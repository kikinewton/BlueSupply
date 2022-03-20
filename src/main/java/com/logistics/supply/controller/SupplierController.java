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

import static com.logistics.supply.util.Constants.SUCCESS;
import static com.logistics.supply.util.Helper.failedResponse;

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
      ResponseDTO response = new ResponseDTO("SUPPLIER CREATED SUCCESSFULLY", SUCCESS, s);
      return ResponseEntity.ok(response);
    } catch (Exception e) {
      log.error(e.getMessage());
    }
    return failedResponse("SUPPLIER CREATION FAILED");
  }

  @GetMapping(value = "/suppliers")
  public ResponseEntity<?> listAllSuppliers(
      @RequestParam(required = false, name = "suppliersForRequestProcurement")
          boolean suppliersForRequest,
      @RequestParam(required = false, name = "suppliersWithRQ") boolean suppliersWithRQ) {
    List<Supplier> suppliers;
    try {
      if (suppliersForRequest) {
        suppliers = supplierService.findSupplierWithNoDocFromSRM();
        ResponseDTO response = new ResponseDTO("FETCH SUCCESSFUL", SUCCESS, suppliers);
        return ResponseEntity.ok(response);
      }
      if (suppliersWithRQ) {
        suppliers = supplierService.findSuppliersWithQuotationForLPO();
        ResponseDTO response = new ResponseDTO("FETCH SUCCESSFUL", SUCCESS, suppliers);
        return ResponseEntity.ok(response);
      }
      suppliers = supplierService.getAll();
      ResponseDTO response = new ResponseDTO("FETCH SUCCESSFUL", SUCCESS, suppliers);
      return ResponseEntity.ok(response);
    } catch (Exception e) {
      log.error(e.getMessage());
    }
    return failedResponse("FETCH FAILED");
  }

  @DeleteMapping(value = "/suppliers/{supplierId}")
  public ResponseEntity<?> deleteSupplier(@PathVariable int supplierId) {
    try {
      supplierService.delete(supplierId);
      ResponseDTO response = new ResponseDTO("SUPPLIER DELETED", SUCCESS, null);
      return ResponseEntity.ok(response);
    } catch (Exception e) {
      log.error(e.getMessage());
    }
    return failedResponse("DELETE SUPPLIER FAILED");
  }

  @GetMapping(value = "/suppliers/{supplierId}")
  public ResponseEntity<?> getSupplier(@PathVariable("supplierId") int supplierId) {
    try {
      Optional<Supplier> supplier = supplierService.findBySupplierId(supplierId);
      if (!supplier.isPresent()) {
        return failedResponse("SUPPLIER NOT FOUND");
      }
      ResponseDTO response = new ResponseDTO("SUPPLIER FOUND", SUCCESS, supplier.get());
      return ResponseEntity.ok(response);
    } catch (Exception e) {
      log.error(e.getMessage());
    }
    return failedResponse("FETCH FAILED");
  }

  @PutMapping(value = "/suppliers/{supplierId}")
  public ResponseEntity<?> updateSupplier(
      @PathVariable("supplierId") int supplierId, @Valid @RequestBody SupplierDTO supplierDTO) {
    try {
      Supplier updated = supplierService.updateSupplier(supplierId, supplierDTO);
      System.out.println("updated = " + updated);
      if(updated != null) {
        ResponseDTO response = new ResponseDTO("UPDATE SUCCESSFUL", SUCCESS, updated);
        return ResponseEntity.ok(response);
      }

    } catch (Exception e) {
      log.error(e.getMessage());
    }
    return failedResponse("UPDATE FAILED");
  }


}
