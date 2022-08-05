package com.logistics.supply.enums;

import lombok.Getter;

@Getter
public enum EmployeeLevel {
  REGULAR("REGULAR"),
  HOD("HOD"),
  GENERAL_MANAGER("GENERAL_MANAGER"),
  PROCUREMENT_OFFICER("PROCUREMENT_OFFICER"),
  STORE_OFFICER("STORE_OFFICER"),
  ACCOUNT_OFFICER("ACCOUNT_OFFICER"),
  ADMIN("ADMIN"),
  STORE_MANAGER("STORE_MANAGER");

  private String employeeLevel;

   EmployeeLevel(String employeeLevel) {
    this.employeeLevel = employeeLevel;
  }


}
