package com.logistics.supply.event.listener;

import com.logistics.supply.email.EmailSender;
import com.logistics.supply.enums.EmailType;
import com.logistics.supply.enums.VerificationType;
import com.logistics.supply.model.VerificationToken;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring5.SpringTemplateEngine;

import javax.persistence.PostPersist;
import java.text.MessageFormat;

@Slf4j
@Component
@RequiredArgsConstructor
public class VerificationEventListener {
  private final SpringTemplateEngine templateEngine;
  private final EmailSender emailSender;

  @Value("${config.templateMail}")
  String template;

  @PostPersist
  public void sendVerificationToken(VerificationToken token) {
    log.debug("==== SEND VERIFICATION EMAIL");
    if(token.getVerificationType().equals(VerificationType.PASSWORD_RESET)) {
        String title = "PASSWORD RESET";
        String message = MessageFormat.format("{0} is your password reset token", token.getToken());
        String emailContent = composeEmail(title, message, template);
        emailSender.sendMail(token.getEmail(), EmailType.EMPLOYEE_PASSWORD_RESET, emailContent);
    }

  }

  private String composeEmail(String title, String message, String template) {
    Context context = new Context();
    context.setVariable("title", title);
    context.setVariable("message", message);
    return templateEngine.process(template, context);
  }
}
