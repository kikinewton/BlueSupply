package com.logistics.supply.controller;

import com.logistics.supply.dto.ResponseDto;
import com.logistics.supply.dto.SupplierDTO;
import com.logistics.supply.model.Supplier;
import com.logistics.supply.service.SupplierService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Optional;

import static com.logistics.supply.util.Constants.SUCCESS;

@Slf4j
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class SupplierController {

  private final SupplierService supplierService;

  @PostMapping(value = "/suppliers")
  public ResponseEntity<ResponseDto<Supplier>> createSupplier(@Valid @RequestBody SupplierDTO supplierDTO) {

    Supplier savedSupplier = supplierService.add(supplierDTO);
    return ResponseDto.wrapSuccessResult(savedSupplier, "SUPPLIER CREATED SUCCESSFULLY");
  }

  @GetMapping(value = "/suppliers")
  public ResponseEntity<?> listAllSuppliers(
      @RequestParam(required = false, name = "suppliersForRequestProcurement")
          Optional<Boolean> suppliersForRequest,
      @RequestParam(required = false, name = "suppliersWithRQ") Optional<Boolean> suppliersWithRQ,
      @RequestParam(required = false) Optional<Boolean> unRegisteredSuppliers) {
    List<Supplier> suppliers;

    if (suppliersForRequest.isPresent() && suppliersForRequest.get()) {
      suppliers = supplierService.findSupplierWithNoDocFromSRM();
      ResponseDto response = new ResponseDto("FETCH SUCCESSFUL", SUCCESS, suppliers);
      return ResponseEntity.ok(response);
    }
    if (suppliersWithRQ.isPresent() && suppliersWithRQ.get()) {
      suppliers = supplierService.findSuppliersWithQuotationForLPO();
      ResponseDto response = new ResponseDto("FETCH SUCCESSFUL", SUCCESS, suppliers);
      return ResponseEntity.ok(response);
    }
    if (unRegisteredSuppliers.isPresent() && unRegisteredSuppliers.get()) {
      suppliers = supplierService.findUnRegisteredSuppliers();
      ResponseDto response = new ResponseDto("FETCH SUCCESSFUL", SUCCESS, suppliers);
      return ResponseEntity.ok(response);
    }
    suppliers = supplierService.getAll();
    ResponseDto response = new ResponseDto("FETCH SUCCESSFUL", SUCCESS, suppliers);
    return ResponseEntity.ok(response);
  }

  @DeleteMapping(value = "/suppliers/{supplierId}")
  public ResponseEntity<?> deleteSupplier(@PathVariable int supplierId) {
    supplierService.delete(supplierId);
    ResponseDto response = new ResponseDto("SUPPLIER DELETED", SUCCESS, null);
    return ResponseEntity.ok(response);
  }

  @GetMapping(value = "/suppliers/{supplierId}")
  public ResponseEntity<?> getSupplier(@PathVariable("supplierId") int supplierId) {
    Supplier supplier = supplierService.findBySupplierId(supplierId);
    return ResponseDto.wrapSuccessResult(supplier, "SUPPLIER FOUND");
  }

  @PutMapping(value = "/suppliers/{supplierId}")
  public ResponseEntity<?> updateSupplier(
      @PathVariable("supplierId") int supplierId, @Valid @RequestBody SupplierDTO supplierDTO) {
    Supplier updated = supplierService.updateSupplier(supplierId, supplierDTO);
    ResponseDto response = new ResponseDto("UPDATE SUCCESSFUL", SUCCESS, updated);
    return ResponseEntity.ok(response);
  }
}
