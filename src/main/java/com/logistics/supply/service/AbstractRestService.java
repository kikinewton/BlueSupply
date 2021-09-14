package com.logistics.supply.service;

import com.logistics.supply.model.RequestCategory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AbstractRestService {

  @Autowired public DepartmentService departmentService;

  @Autowired public EmployeeService employeeService;

  @Autowired public InventoryService inventoryService;

  @Autowired public RequestItemService requestItemService;

  @Autowired public SupplierService supplierService;

  @Autowired public ProcurementService procurementService;

  @Autowired public RequestCategoryService requestCategoryService;

  @Autowired public RequestDocumentService requestDocumentService;

  @Autowired public GoodsReceivedNoteService goodsReceivedNoteService;

  @Autowired public InvoiceService invoiceService;

  @Autowired public PaymentService paymentService;

  @Autowired public PaymentDraftService paymentDraftService;

  @Autowired public QuotationService quotationService;

  @Autowired public LocalPurchaseOrderService localPurchaseOrderService;

  @Autowired public ExcelService excelService;

  @Autowired public CommentService commentService;
}
