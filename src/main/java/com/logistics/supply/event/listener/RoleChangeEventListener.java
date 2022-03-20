package com.logistics.supply.event.listener;

import com.logistics.supply.email.EmailSender;
import com.logistics.supply.enums.EmailType;
import com.logistics.supply.event.RoleChangeEvent;
import com.logistics.supply.service.EmployeeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring5.SpringTemplateEngine;

import java.text.MessageFormat;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Component
@RequiredArgsConstructor
public class RoleChangeEventListener {

  final EmailSender emailSender;
  final SpringTemplateEngine templateEngine;
  final EmployeeService employeeService;

  @Value("${config.templateMail}")
  String changeRelatedMail;

  @Async
  @EventListener(condition = "#roleChangeEvent.roleChanged eq true && " +
          "#roleChangeEvent.getEmployee() != null")
  public void sendGMMailOnRoleChange(RoleChangeEvent roleChangeEvent) {
    log.info("==== SEND MAIL TO HOD & GM ====");
    String title = "EMPLOYEE ROLE UPDATE";
    String message =
        MessageFormat.format(
            "Kindly note that the user {0} has been updated {1} by the admin",
            roleChangeEvent.getEmployee().getFullName(),
            roleChangeEvent.getEmployee().getRoles().get(0).getName());
    String emailContent = composeEmail(title, message, changeRelatedMail);
    try {
      String hodEmail =
          employeeService
              .getDepartmentHOD(roleChangeEvent.getEmployee().getDepartment())
              .getEmail();
      String gmEmail = employeeService.getGeneralManager().getEmail();
      Set<String> emails =
          Stream.of(hodEmail, gmEmail).collect(Collectors.toCollection(HashSet::new));
      sendEmails(emailContent, emails);
    } catch (Exception e) {
      log.error(e.toString());
      e.printStackTrace();
    }
  }

  private void sendEmails(String content, Set<String> emails) {
    CompletableFuture.runAsync(
        () -> {
          try {
            emails.forEach(e -> emailSender.sendMail(e, EmailType.EMPLOYEE_ROLE_CHANGE, content));
          } catch (Exception e) {
            log.error(e.toString());
            e.printStackTrace();
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
