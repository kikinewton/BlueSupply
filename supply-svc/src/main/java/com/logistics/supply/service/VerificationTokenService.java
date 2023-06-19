package com.logistics.supply.service;

import com.logistics.supply.enums.VerificationType;
import com.logistics.supply.exception.VerificationTokenExpiredException;
import com.logistics.supply.exception.VerificationTokenNotFoundException;
import com.logistics.supply.model.VerificationToken;
import com.logistics.supply.repository.VerificationTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.stereotype.Service;

import javax.validation.constraints.Email;
import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;

@Service
@Slf4j
@RequiredArgsConstructor
public class VerificationTokenService {
  private final VerificationTokenRepository verificationTokenRepository;
  private final EmployeeService employeeService;

  public void generateVerificationToken(String email, VerificationType verificationType) {

    log.info("Generate verification code for email: {}", email);
    String token = RandomStringUtils.randomAlphanumeric(10);
    VerificationToken vt = new VerificationToken(token, email, verificationType);
    VerificationToken verificationToken = verificationTokenRepository.save(vt);
    changeEmployeePassword(verificationToken);
  }

  private void changeEmployeePassword(VerificationToken verificationToken) {
    String email = verificationToken.getEmail();
    log.info("Change the password for employee: {}", email);
    CompletableFuture.runAsync(
            () -> employeeService.changePassword(
                    verificationToken.getToken(),
                    email));
  }

  public void checkVerificationCode(String token, @Email String email) {

    log.info("Check verification token: {} for email {} has not expired".formatted(token, email));
    VerificationToken vt = getVerificationToken(token, email);
    if (vt.getExpiryDate().isAfter(LocalDateTime.now())) {
      throw new VerificationTokenExpiredException(token);
    }
  }

  private VerificationToken getVerificationToken(String token, String email) {

    log.info("Find verification code {} for email: {}", token, email);
    return verificationTokenRepository.findFirstByEmailAndTokenOrderByIdDesc(email, token)
                .orElseThrow(() -> new VerificationTokenNotFoundException(token));
  }
}
