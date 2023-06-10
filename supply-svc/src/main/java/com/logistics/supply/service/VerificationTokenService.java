package com.logistics.supply.service;

import com.logistics.supply.enums.VerificationType;
import com.logistics.supply.model.Employee;
import com.logistics.supply.model.VerificationToken;
import com.logistics.supply.repository.VerificationTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.stereotype.Service;

import javax.validation.constraints.Email;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class VerificationTokenService {
  private final VerificationTokenRepository verificationTokenRepository;
  private final EmployeeService employeeService;

  public boolean generateVerificationToken(String email, VerificationType verificationType) {

    String token = RandomStringUtils.randomAlphanumeric(10);
    VerificationToken vt = new VerificationToken(token, email, verificationType);
    VerificationToken verificationToken = verificationTokenRepository.save(vt);
    Employee employee =
        employeeService.changePassword(verificationToken.getToken(), verificationToken.getEmail());
    return employee != null;
  }

  public boolean confirmVerificationCode(String token, @Email String email) {
    Optional<VerificationToken> vt =
        verificationTokenRepository.findFirstByEmailAndTokenOrderByIdDesc(email, token);
    if (vt.isEmpty()) return false;
    return !vt.get().getExpiryDate().isAfter(LocalDateTime.now());
  }
}
