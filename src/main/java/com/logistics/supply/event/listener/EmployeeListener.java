package com.logistics.supply.event.listener;

import com.logistics.supply.email.EmailSender;
import com.logistics.supply.enums.EmailType;
import com.logistics.supply.model.Employee;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring5.SpringTemplateEngine;

import javax.persistence.PostPersist;

@Slf4j
@RequiredArgsConstructor
public class EmployeeListener {

  final SpringTemplateEngine templateEngine;
  private final EmailSender emailSender;

  @Value("${config.templateMail}")
  String newEmployeeEmail;

  @PostPersist
  public void sendEmployeeEmail(Employee employee) {
    log.info("==== SEND EMAIL TO NEW EMPLOYEES ====");
    String title = "Welcome " + employee.getLastName();
    String message =
        "You now have access to the procurement software. Kindly contact your admin for your default credentials";
    String emailContent = composeEmail(title, message, newEmployeeEmail);
    try {
      emailSender.sendMail(employee.getEmail(), EmailType.NEW_USER_CONFIRMATION_MAIL, emailContent);
    } catch (Exception e) {
      log.error(e.getMessage());
    }
  }

  private String composeEmail(String title, String message, String template) {
    Context context = new Context();
    context.setVariable("title", title);
    context.setVariable("message", message);
    return templateEngine.process(template, context);
  }
}
