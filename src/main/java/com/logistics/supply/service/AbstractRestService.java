package com.logistics.supply.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AbstractRestService {

    @Autowired
    public DepartmentService departmentService;

    @Autowired
    public EmployeeService employeeService;

    @Autowired
    public   InventoryService inventoryService;

    @Autowired
    public RequestItemService requestItemService;

    @Autowired
    public SupplierService supplierService;
}
