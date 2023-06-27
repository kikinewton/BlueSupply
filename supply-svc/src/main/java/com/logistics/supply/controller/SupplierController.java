package com.logistics.supply.controller;

import com.logistics.supply.dto.ResponseDto;
import com.logistics.supply.dto.SupplierDto;
import com.logistics.supply.model.Supplier;
import com.logistics.supply.service.SupplierService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Optional;

import static com.logistics.supply.util.Constants.FETCH_SUCCESSFUL;

@Slf4j
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class SupplierController {

  private final SupplierService supplierService;

  @PostMapping(value = "/suppliers")
  public ResponseEntity<ResponseDto<Supplier>> createSupplier(
          @Valid @RequestBody SupplierDto supplierDTO) {

    Supplier savedSupplier = supplierService.add(supplierDTO);
    return ResponseDto.wrapSuccessResult(savedSupplier, "SUPPLIER CREATED SUCCESSFULLY");
  }

  @GetMapping(value = "/suppliers")
  public ResponseEntity<ResponseDto<List<Supplier>>> listAllSuppliers(
      @RequestParam(required = false, name = "suppliersForRequestProcurement")
          Optional<Boolean> suppliersForRequest,
      @RequestParam(required = false, name = "suppliersWithRQ") Optional<Boolean> suppliersWithRQ,
      @RequestParam(required = false) Optional<Boolean> unRegisteredSuppliers) {
    List<Supplier> suppliers;

    if (suppliersForRequest.isPresent() && Boolean.TRUE.equals(suppliersForRequest.get())) {
      suppliers = supplierService.findSupplierWithNoDocFromSRM();
      return ResponseDto.wrapSuccessResult( suppliers, FETCH_SUCCESSFUL);
    }
    if (suppliersWithRQ.isPresent() && Boolean.TRUE.equals(suppliersWithRQ.get())) {
      suppliers = supplierService.findSuppliersWithQuotationForLPO();
      return ResponseDto.wrapSuccessResult(suppliers, FETCH_SUCCESSFUL);
    }
    if (unRegisteredSuppliers.isPresent() && Boolean.TRUE.equals(unRegisteredSuppliers.get())) {
      suppliers = supplierService.findUnRegisteredSuppliers();
      return ResponseDto.wrapSuccessResult( suppliers, FETCH_SUCCESSFUL);
    }
    suppliers = supplierService.getAll();
    return ResponseDto.wrapSuccessResult( suppliers, FETCH_SUCCESSFUL);
  }

  @DeleteMapping(value = "/suppliers/{supplierId}")
  public ResponseEntity<ResponseDto<Void>> deleteSupplier(
          @PathVariable("supplierId") int supplierId) {

    supplierService.delete(supplierId);
     return ResponseDto.wrapSuccessResult(  null, "SUPPLIER DELETED");
  }

  @GetMapping(value = "/suppliers/{supplierId}")
  public ResponseEntity<ResponseDto<Supplier>> getSupplier(
          @PathVariable("supplierId") int supplierId) {

    Supplier supplier = supplierService.findBySupplierId(supplierId);
    return ResponseDto.wrapSuccessResult(supplier, "SUPPLIER FOUND");
  }

  @PutMapping(value = "/suppliers/{supplierId}")
  public ResponseEntity<ResponseDto<Supplier>> updateSupplier(
      @PathVariable("supplierId") int supplierId,
      @Valid @RequestBody SupplierDto supplierDTO) {

    Supplier updated = supplierService.updateSupplier(supplierId, supplierDTO);
    return ResponseDto.wrapSuccessResult(updated, "UPDATE SUCCESSFUL");

  }
}
