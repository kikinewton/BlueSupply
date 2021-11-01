package com.logistics.supply.event;

import com.logistics.supply.email.EmailSender;
import com.logistics.supply.enums.EmailType;
import com.logistics.supply.model.Employee;
import com.logistics.supply.service.EmployeeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring5.SpringTemplateEngine;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@RequiredArgsConstructor
public class RoleChangeEventListener {

  final EmailSender emailSender;
  final SpringTemplateEngine templateEngine;
  final EmployeeService employeeService;

  @Value("${config.templateMail}")
  String changeRelatedMail;

  public void sendGMMailOnRoleChange(Employee employee) {
    log.info("==== SEND MAIL TO HOD & GM ====");
    String title = "EMPLOYEE ROLE CHANGED";
    String message = "Kindly note that the role for this employee has been changed by the admin";
    String emailContent = composeEmail(title, message, changeRelatedMail);
    try {
      String hodEmail = employeeService.getDepartmentHOD(employee.getDepartment()).getEmail();
      String gmEmail = employeeService.getGeneralManager().getEmail();
      Set<String> emails =
          Stream.of(hodEmail, gmEmail).collect(Collectors.toCollection(HashSet::new));
      sendEmails(emailContent, emails);
    } catch (Exception e) {
      log.error(e.toString());
    }
  }

  public void sendMailOnEmployeeDisable(Employee employee) {
    try {

    } catch (Exception e) {
      log.error(e.toString());
    }
  }

  private void sendEmails(String content, Set<String> emails) {

    CompletableFuture.runAsync(
        () -> {
          try {
            emails.forEach(e -> emailSender.sendMail(e, EmailType.EMPLOYEE_ROLE_CHANGE, content));
          } catch (Exception e) {
            log.error(e.toString());
          }
        });
  }

  private String composeEmail(String title, String message, String template) {
    Context context = new Context();
    context.setVariable("title", title);
    context.setVariable("message", message);
    return templateEngine.process(template, context);
  }
}
