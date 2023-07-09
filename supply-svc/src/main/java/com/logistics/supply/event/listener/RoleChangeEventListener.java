package com.logistics.supply.event.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import com.logistics.supply.enums.EmailType;
import com.logistics.supply.event.RoleChangeEvent;
import com.logistics.supply.service.EmployeeService;
import com.logistics.supply.util.EmailSenderUtil;
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
  //
  //  final EmailSender emailSender;
  //  final SpringTemplateEngine templateEngine;
  private final EmployeeService employeeService;
  private final EmailSenderUtil emailSenderUtil;

  @Value("${config.templateMail}")
  private String changeRelatedMail;

  @Async
  @EventListener(
      condition =
          "#roleChangeEvent.roleChanged eq true && " + "#roleChangeEvent.getEmployee() != null")
  public void sendGMMailOnRoleChange(RoleChangeEvent roleChangeEvent) {
    log.info("==== SEND MAIL TO HOD & GM ====");
    String message =
        MessageFormat.format(
            "Kindly note that the user {0} has been updated {1} by the admin",
            roleChangeEvent.getEmployee().getFullName(),
            roleChangeEvent.getEmployee().getRoles().get(0).getName());
    try {
      String hodEmail =
          employeeService
              .getDepartmentHOD(roleChangeEvent.getEmployee().getDepartment())
              .getEmail();
      String gmEmail = employeeService.getGeneralManager().getEmail();
      if (gmEmail == null || hodEmail == null) {
        log.info("==== Either HOD or GM does not exist ====");
        return;
      }
      Set<String> emails =
          Stream.of(hodEmail, gmEmail).collect(Collectors.toCollection(HashSet::new));
      sendEmails(message, emails);
    } catch (Exception e) {
      log.error(e.toString());
    }
  }

  private void sendEmails(String content, Set<String> emails) {
    CompletableFuture.runAsync(
        () -> {
          try {
            String title = "EMPLOYEE ROLE UPDATE";
            emails.forEach(
                e ->
                    emailSenderUtil.sendComposeAndSendEmail(
                        title, content, changeRelatedMail, EmailType.EMPLOYEE_ROLE_CHANGE, e));
          } catch (Exception e) {
            log.error(e.toString());
          }
        });
  }
}
