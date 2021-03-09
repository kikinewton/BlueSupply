package com.logistics.supply.controller;

import com.logistics.supply.dto.ResponseDTO;
import com.logistics.supply.dto.SupplierDTO;
import com.logistics.supply.model.Supplier;
import com.logistics.supply.service.AbstractRestService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

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
}
