package com.logistics.supply.service;

import com.logistics.supply.dto.EmployeeDTO;
import com.logistics.supply.model.Employee;
import com.logistics.supply.util.CommonHelper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Transactional
@Service
@Slf4j
@AllArgsConstructor
public class RegistrationService {

  @Autowired private final EmployeeService employeeService;

  public Employee register(EmployeeDTO employeeDTO) {
    String[] nullValues = CommonHelper.getNullPropertyNames(employeeDTO);
    Set<String> l = Set.of(nullValues);
    boolean isEmailValid = CommonHelper.isValidEmailAddress(employeeDTO.getEmail());
    if (!isEmailValid) {
      throw new IllegalStateException("Email is invalid");
    }

    if (l.size() > 0) {
      throw new IllegalStateException("Missing required employee information");
    }
    return employeeService.signUp(employeeDTO);
  }
}
