package com.logistics.supply.event;

import com.logistics.supply.email.EmailSender;
import com.logistics.supply.enums.EmailType;
import com.logistics.supply.model.Employee;
import com.logistics.supply.model.EmployeeRole;
import com.logistics.supply.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring5.SpringTemplateEngine;

@Slf4j
@Component
@RequiredArgsConstructor
public class PettyCashListener {

    final EmailSender emailSender;
    final SpringTemplateEngine templateEngine;
    final EmployeeRepository employeeRepository;

    @Value("${config.templateMail}")
    String pettyCashEndorsementEmail;

    @EventListener
    private void sendHODEmail(PettyCashEvent pettyCashEvent) {
        log.info("==== SEND MAIL TO HOD ====");
        String title = "PETTY CASH ENDORSEMENT";
        String message = "Kindly review this petty cash request pending endorsement";
        String emailContent = composeEmail(title, message, pettyCashEndorsementEmail);
        try {
            Employee employee =
                    employeeRepository.findDepartmentHod(
                            pettyCashEvent.getPettyCash().stream().findFirst().get().getDepartment().getId(), EmployeeRole.ROLE_HOD.ordinal());
            emailSender.sendMail(employee.getEmail(), EmailType.PETTY_CASH_ENDORSEMENT_EMAIL, emailContent);
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
