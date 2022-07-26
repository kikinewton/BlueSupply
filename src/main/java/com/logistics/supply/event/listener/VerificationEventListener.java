package com.logistics.supply.event.listener;

import com.logistics.supply.enums.EmailType;
import com.logistics.supply.enums.VerificationType;
import com.logistics.supply.errorhandling.GeneralException;
import com.logistics.supply.model.VerificationToken;
import com.logistics.supply.util.EmailSenderUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.persistence.PostPersist;
import java.text.MessageFormat;

@Slf4j
@Component
@RequiredArgsConstructor
public class VerificationEventListener {
  private final EmailSenderUtil emailSenderUtil;
//  private final EmployeeService employeeService;
  @Value("${config.templateMail}")
  private String template;

  @PostPersist
  public void sendVerificationToken(VerificationToken token) throws GeneralException {
    log.debug("==== SEND VERIFICATION EMAIL");
    if (token.getVerificationType().equals(VerificationType.PASSWORD_RESET)) {
      String title = "PASSWORD RESET";
      String message = MessageFormat.format("{0} is your password reset token", token.getToken());

      emailSenderUtil.sendComposeAndSendEmail(
          title, message, template, EmailType.EMPLOYEE_PASSWORD_RESET, token.getEmail());
    }
  }
}
