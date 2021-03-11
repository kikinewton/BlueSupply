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

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Transactional
@Service
@Slf4j
@AllArgsConstructor
public class AuthService extends AbstractDataService {

  @Autowired private final EmployeeService employeeService;

  public Employee register(EmployeeDTO employeeDTO) {
    String[] nullValues = CommonHelper.getNullPropertyNames(employeeDTO);
    Set<String> l = Set.of(nullValues);
    l.forEach(x -> System.out.println(x));
    boolean isEmailValid = CommonHelper.isValidEmailAddress(employeeDTO.getEmail());
    if (!isEmailValid) {
      throw new IllegalStateException("Email is invalid");
    }

    if (l.size() > 0) {
      throw new IllegalStateException("Missing required employee information");
    }
    return employeeService.signUp(employeeDTO);
  }

  public Employee adminRegistration(RegistrationRequest request) {
    String[] nullValues = CommonHelper.getNullPropertyNames(request);
    Set<String> l = Set.of(nullValues);
    l.forEach(x -> System.out.println(x));
    boolean isEmailValid = CommonHelper.isValidEmailAddress(request.getEmail());
    if (!isEmailValid) {
      throw new IllegalStateException("Email is invalid");
    }

    if (l.size() > 0) {
      throw new IllegalStateException("Missing required employee information");
    }
    return employeeService.signUp(request);
  }


  public String generateVerificationToken(Employee employee) {
    String token = UUID.randomUUID().toString();
    VerificationToken verificationToken = new VerificationToken();
    verificationToken.setEmployee(employee);
    verificationToken.setToken(token);

    verificationTokenRepository.save(verificationToken);

    return token;
  }

  public void verifyAccount(String token) {
    Optional<VerificationToken> verificationToken = verificationTokenRepository.findByToken(token);

    verificationToken.orElseThrow(() -> new IllegalStateException("Invalid Token"));

    fetchEmployeeAndEnable(verificationToken.get());
  }

  public void fetchEmployeeAndEnable(VerificationToken verificationToken) {
    String username = verificationToken.getEmployee().getEmail();

    Employee employee= employeeRepository.findByEmail(username).orElseThrow(() -> new IllegalStateException("User Not Found"));
    employee.setEnabled(true);
    employeeRepository.save(employee);
  }

}
