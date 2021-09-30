package com.logistics.supply.auth;

import com.logistics.supply.dto.EmployeeDTO;
import com.logistics.supply.dto.RegistrationRequest;
import com.logistics.supply.model.Employee;
import com.logistics.supply.model.VerificationToken;
import com.logistics.supply.service.AbstractDataService;
import com.logistics.supply.service.EmployeeService;
import com.logistics.supply.util.CommonHelper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.Valid;
import java.util.*;


@Service
@Slf4j
@AllArgsConstructor
public class AuthService extends AbstractDataService {

  @Autowired private final EmployeeService employeeService;

  public Employee adminRegistration(@Valid RegistrationRequest request) {

    boolean isEmailValid = CommonHelper.isValidEmailAddress(request.getEmail());
    if (!isEmailValid) {
      throw new IllegalStateException("Email is invalid");
    }

    return employeeService.signUp(request);
  }

}
