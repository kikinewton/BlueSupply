package com.logistics.supply.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AbstractRestService {

  @Autowired public EmployeeService employeeService;

  @Autowired public RequestItemService requestItemService;

  @Autowired public SupplierService supplierService;

  @Autowired public RequestCategoryService requestCategoryService;

  @Autowired public RequestDocumentService requestDocumentService;

  @Autowired public InvoiceService invoiceService;

  @Autowired public PaymentService paymentService;

  @Autowired public ExcelService excelService;
}
