package com.logistics.supply.event.listener;

import com.logistics.supply.email.EmailSender;
import com.logistics.supply.enums.EmailType;
import com.logistics.supply.model.Employee;
import com.logistics.supply.service.EmployeeService;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring5.SpringTemplateEngine;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class EmployeeDisabledEventListener {

  final EmailSender emailSender;
  final SpringTemplateEngine templateEngine;
  final EmployeeService employeeService;

  @Value("${config.templateMail}")
  String disabledEmployeeMail;

  @Async
  @EventListener(condition = "disableEvent.isDisabled eq true")
  public void sendMailOnEmployeeDisable(EmployeeDisableEvent disableEvent) {
    try {
      String hodEmail = employeeService.getDepartmentHOD(disableEvent.getEmployee().getDepartment()).getEmail();
      String hodContent = "Kindly note that this user has been disabled by the Admin";
      String hodEmailContent = composeEmail("EMPLOYEE DISABLED ", hodContent, disabledEmployeeMail);

      String employeeContent = "Kindly note that your account has been disabled";
      String employeeEmailContent =
          composeEmail("EMPLOYEE DISABLED ", employeeContent, disabledEmployeeMail);

      Map<String, String> disableEmployeeMap = new HashMap<>();
      disableEmployeeMap.put(hodEmail, hodEmailContent);
      disableEmployeeMap.put(disableEvent.getEmployee().getEmail(), employeeEmailContent);
      sendEmails(disableEmployeeMap);
    } catch (Exception e) {
      log.error(e.toString());
    }
  }

  private void sendEmails(Map<String, String> emailMap) {
    try {
      emailMap.forEach((k, v) -> emailSender.sendMail(k, EmailType.EMPLOYEE_DISABLED, v));
    } catch (Exception e) {
      log.error(e.toString());
    }
  }

  private String composeEmail(String title, String message, String template) {
    Context context = new Context();
    context.setVariable("title", title);
    context.setVariable("message", message);
    return templateEngine.process(template, context);
  }



  @Getter
  public static class EmployeeDisableEvent extends ApplicationEvent {
    private Employee employee;
    private final boolean isDisabled;

    public EmployeeDisableEvent(Object source, Employee employee) {
      super(source);
      this.employee = employee;
      this.isDisabled = employee.getEnabled();
    }
  }
}
