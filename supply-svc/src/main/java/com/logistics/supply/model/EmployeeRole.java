package com.logistics.supply.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;

@Getter
@NoArgsConstructor
public enum EmployeeRole implements GrantedAuthority {
  ROLE_REGULAR,
  ROLE_HOD,
  ROLE_GENERAL_MANAGER,
  ROLE_PROCUREMENT_OFFICER,
  ROLE_STORE_OFFICER,
  ROLE_ACCOUNT_OFFICER,
  ROLE_CHIEF_ACCOUNT_OFFICER,
  ROLE_ADMIN,
  ROLE_PROCUREMENT_MANAGER,
  ROLE_FINANCIAL_MANAGER,
  ROLE_STORE_MANAGER,
  ROLE_AUDITOR;

  @Override
  public String getAuthority() {
    return name();
  }

}
