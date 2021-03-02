package com.logistics.supply.controller;

import com.logistics.supply.service.SupplierService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SupplierController {

    @Autowired
    private SupplierService supplierService;
}
