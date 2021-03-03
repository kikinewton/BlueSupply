package com.logistics.supply.controller;

import com.logistics.supply.dto.ResponseDTO;
import com.logistics.supply.dto.SupplierDTO;
import com.logistics.supply.model.Supplier;
import com.logistics.supply.service.AbstractRestService;
import com.logistics.supply.service.SupplierService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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
            return new ResponseDTO("ERROR", HttpStatus.CREATED.name());
        } catch (Exception e) {
            log.error(e.getMessage());
            e.printStackTrace();
        }
        return new ResponseDTO("ERROR", HttpStatus.BAD_REQUEST.name());
    }

    @GetMapping(value = "/suppliers")
    public ResponseDTO<List<Supplier>> getAllSuppliers() {
        List<Supplier> suppliers = new ArrayList<>();
        try {
            suppliers = supplierService.getAll();
            if (suppliers.size() > 0) return new ResponseDTO<>("SUCCESS", suppliers, HttpStatus.FOUND.name());
        }
        catch (Exception e) {
            log.error(e.getMessage());
            e.printStackTrace();
        }
        return new ResponseDTO<>("ERROR",null, HttpStatus.NOT_FOUND.name());
    }


}
