package com.logistics.supply.event.listener;

import com.logistics.supply.email.EmailSender;
import com.logistics.supply.enums.EmailType;
import com.logistics.supply.model.Employee;
import com.logistics.supply.model.EmployeeRole;
import com.logistics.supply.model.FloatOrder;
import com.logistics.supply.model.Role;
import com.logistics.supply.service.EmployeeService;
import com.logistics.supply.service.RoleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring5.SpringTemplateEngine;

import java.text.MessageFormat;

@Slf4j
@Component
@RequiredArgsConstructor
public class FloatRetirementListener {
  private final EmailSender emailSender;
  private final RoleService roleService;
  private final EmployeeService employeeService;
  private SpringTemplateEngine templateEngine;

  @Value("${config.templateMail}")
  String floatRetirementEmail;



  @Async
  @EventListener(condition = "#order.getStatus() == 'PROCESSED'")
  public void sendMailToAuditor(FloatOrder order) {
    log.info("===== EMAIL AUDITOR ======");
    String title = "FLOAT RETIREMENT";
    Role role = roleService.findByName(EmployeeRole.ROLE_AUDITOR.name());
    Employee auditor = employeeService.findRecentEmployeeWithRoleId(role.getId());
    String message =
        MessageFormat.format(
            "Float by {0} is ready for retirement", order.getCreatedBy().getFullName());
    String emailContent = composeEmail(title, message, floatRetirementEmail);
    emailSender.sendMail(auditor.getEmail(), EmailType.AUDITOR_FLOAT_RETIREMENT, emailContent);
  }

  @Async
  @EventListener(
      condition =
          "#order.getStatus() == 'PROCESSED'  and #order.isHasDocument () eq true and #order.getAuditorRetirementApproval() eq true")
  public void sendMailToGM(FloatOrder order) {
    log.info("===== EMAIL AUDITOR ======");
    String title = "FLOAT RETIREMENT";
    Employee generalManager = employeeService.getGeneralManager();
    String message =
        MessageFormat.format(
            "Float by {0} has been endorsed by auditor for retirement",
            order.getCreatedBy().getFullName());
    String emailContent = composeEmail(title, message, floatRetirementEmail);
    emailSender.sendMail(generalManager.getEmail(), EmailType.GM_FLOAT_RETIREMENT, emailContent);
  }

  private String composeEmail(String title, String message, String template) {
    Context context = new Context();
    context.setVariable("title", title);
    context.setVariable("message", message);
    return templateEngine.process(template, context);
  }

}
