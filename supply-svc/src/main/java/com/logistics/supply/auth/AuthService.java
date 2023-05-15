package com.logistics.supply.auth;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.validation.Valid;
import com.logistics.supply.dto.RegistrationRequest;
import com.logistics.supply.model.Employee;
import com.logistics.supply.service.EmployeeService;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthService  {

  private final EmployeeService employeeService;

  public Employee adminRegistration(@Valid RegistrationRequest request) {

    return employeeService.signUp(request);
  }
}
