package com.logistics.supply.event.listener;

import com.logistics.supply.email.EmailSender;
import com.logistics.supply.enums.EmailType;
import com.logistics.supply.model.Employee;
import com.logistics.supply.model.EmployeeRole;
import com.logistics.supply.model.FloatOrder;
import com.logistics.supply.model.Role;
import com.logistics.supply.service.EmployeeService;
import com.logistics.supply.service.RoleService;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEvent;
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
  @Value("${config.templateMail}")
  String floatRetirementEmail;
  private SpringTemplateEngine templateEngine;

  @Async
  @EventListener(condition = "#event.isProcessed() eq true && #event.getFloatOrder().isHasDocument() eq true")
  public void sendMailToAuditor(FloatRetirementEvent event) {
    log.info("===== EMAIL AUDITOR ======");
    FloatOrder order = event.getFloatOrder();
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
          "#event.getFloatOrder().getStatus() eq 'PROCESSED'  and #event.getFloatOrder().isHasDocument() eq true and #event.getFloatOrder().getAuditorRetirementApproval() eq true")
  public void sendMailToGM(FloatRetirementEvent event) {
    log.info("===== EMAIL AUDITOR ======");
    String title = "FLOAT RETIREMENT";
    FloatOrder order = event.getFloatOrder();
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

  @Getter
  @Setter
  public static class FloatRetirementEvent extends ApplicationEvent {
    private FloatOrder floatOrder;
    private boolean isProcessed;

    public FloatRetirementEvent(Object source, FloatOrder floatOrder) {
      super(source);
      this.floatOrder = floatOrder;
    }
  }
}
