package com.logistics.supply.service;

import com.logistics.supply.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AbstractDataService {

  @Autowired public DepartmentRepository departmentRepository;

  @Autowired public EmployeeRepository employeeRepository;

  @Autowired public InventoryRepository inventoryRepository;

  @Autowired public SupplierRepository supplierRepository;

  @Autowired public RequestItemRepository requestItemRepository;

  @Autowired public RequestRepository requestRepository;

  @Autowired public VerificationTokenRepository verificationTokenRepository;
}
