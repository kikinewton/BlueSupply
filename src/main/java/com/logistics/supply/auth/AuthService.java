package com.logistics.supply.auth;

import com.logistics.supply.dto.RegistrationRequest;
import com.logistics.supply.model.Employee;
import com.logistics.supply.service.AbstractDataService;
import com.logistics.supply.service.EmployeeService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.validation.Valid;

@Service
@Slf4j
@AllArgsConstructor
public class AuthService extends AbstractDataService {

  @Autowired private final EmployeeService employeeService;

  public Employee adminRegistration(@Valid RegistrationRequest request) {

    return employeeService.signUp(request);
  }
}
