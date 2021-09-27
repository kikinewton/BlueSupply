package com.logistics.supply.service;

import com.logistics.supply.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AbstractDataService {



  @Autowired public EmployeeRepository employeeRepository;



  @Autowired public SupplierRepository supplierRepository;

  @Autowired public RequestItemRepository requestItemRepository;




  @Autowired public PaymentRepository paymentRepository;



}
