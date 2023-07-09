package com.logistics.supply.util;

import com.logistics.supply.exception.NotFoundException;
import org.springframework.security.core.Authentication;

import com.logistics.supply.model.EmployeeRole;

public class AuthHelper {

  public static Boolean checkAuthorityExist(Authentication authentication, EmployeeRole role) {
    return authentication.getAuthorities().stream()
        .map(x -> x.getAuthority().equalsIgnoreCase(role.name()))
        .filter(x -> x == true)
        .findAny()
        .orElseThrow(
            () -> new NotFoundException("Employee with role %s not found".formatted(role.name())));
  }
}
