package com.logistics.supply.dto;

public interface PaymentReportDTO {
  int getId();

  String getSupplier();

  String getInvoiceNo();

  String getAccountNumber();

  String getChequeNumber();

  String getPurchaseNumber();

  String getPaymentDueDate();

  String getPaidAmount();

  String getPaymentStatus();

  String getCreatedDate();

  String getWHTAmount();
}
