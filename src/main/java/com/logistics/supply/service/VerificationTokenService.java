package com.logistics.supply.service;

import com.logistics.supply.enums.VerificationType;
import com.logistics.supply.model.Employee;
import com.logistics.supply.model.VerificationToken;
import com.logistics.supply.repository.EmployeeRepository;
import com.logistics.supply.repository.VerificationTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import javax.validation.constraints.Email;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Service
@Slf4j
@RequiredArgsConstructor
public class VerificationTokenService {

  private final ApplicationEventPublisher applicationEventPublisher;
  private final VerificationTokenRepository verificationTokenRepository;
  private final EmployeeRepository employeeRepository;

  public boolean generateVerificationToken(String email, VerificationType verificationType) {
    try {
      Optional<Employee> e = employeeRepository.findByEmailAndEnabledIsTrue(email);
      if (!e.isPresent()) return false;
      String token = RandomStringUtils.randomAlphanumeric(10);
      VerificationToken vt = new VerificationToken(token, email, verificationType);
      VerificationToken vtoken = verificationTokenRepository.save(vt);
      CompletableFuture.runAsync(() -> applicationEventPublisher.publishEvent(vtoken));
      return true;

    } catch (Exception e) {
      log.error(e.toString());
    }
    return false;
  }

  public boolean confirmVerificationCode(String token, @Email String email) {
    try {
      Optional<VerificationToken> vt =
          verificationTokenRepository.findFirstByEmailAndTokenOrderByIdDesc(email, token);
      if (!vt.isPresent()) return false;
      if (vt.get().getExpiryDate().isAfter(LocalDateTime.now())) return false;
    } catch (Exception e) {
      log.error(e.toString());
    }
    return false;
  }
}