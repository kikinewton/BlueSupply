package com.logistics.supply.controller;

import com.logistics.supply.dto.ResponseDTO;
import com.logistics.supply.dto.SupplierDTO;
import com.logistics.supply.model.Supplier;
import com.logistics.supply.service.AbstractRestService;
import com.logistics.supply.util.CommonHelper;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.jcajce.provider.asymmetric.ec.KeyFactorySpi;
import org.checkerframework.checker.nullness.Opt;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.*;

import static com.logistics.supply.util.Constants.ERROR;
import static com.logistics.supply.util.Constants.SUCCESS;

@Slf4j
@RestController
@RequestMapping("/api")
public class SupplierController extends AbstractRestService {

  @PostMapping(value = "/suppliers")
  public ResponseDTO createSupplier(@RequestBody SupplierDTO supplierDTO) {

    if (Objects.isNull(supplierDTO)) return new ResponseDTO("ERROR", HttpStatus.BAD_REQUEST.name());

    Supplier supplier = new Supplier();
    supplier.setName(supplierDTO.getName());
    supplier.setDescription(supplierDTO.getDescription());
    supplier.setEmail(supplierDTO.getEmail());
    supplier.setLocation(supplierDTO.getLocation());
    supplier.setPhone_no(supplierDTO.getPhone_no());
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
  public ResponseDTO<List<Supplier>> getAllSuppliers() {
    List<Supplier> suppliers;
    try {
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

  @PutMapping(value = "/suppliers/{supplierId}")
  public ResponseDTO<Supplier> updateSupplier(
      @PathVariable int supplierId, @RequestBody SupplierDTO supplierDTO) {
    Optional<Supplier> supplier = supplierService.findBySupplierId(supplierId);

    if (Objects.isNull(supplier.get()))
      return new ResponseDTO<>(ERROR, null, HttpStatus.BAD_REQUEST.name());

    String[] nullValues = CommonHelper.getNullPropertyNames(supplierDTO);
    System.out.println("count of null properties: " + Arrays.stream(nullValues).count());

    Set<String> l = Set.of(nullValues);
    if (l.size() > 0) {
      return new ResponseDTO<>(HttpStatus.BAD_REQUEST.name(), null, "ERROR");
    }

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
