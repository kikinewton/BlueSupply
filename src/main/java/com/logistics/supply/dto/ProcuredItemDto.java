package com.logistics.supply.dto;

public interface ProcuredItemDto {

  int getId();

  String getName();

  String getReason();

  String getPurpose();

  String getSuppliedBy();

  String getCategory();

  Double getTotalPrice();

  int getQuantity();

  String getUserDepartment();
}
