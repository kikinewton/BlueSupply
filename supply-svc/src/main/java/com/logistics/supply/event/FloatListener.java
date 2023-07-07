package com.logistics.supply.event;

import com.logistics.supply.email.EmailSender;
import com.logistics.supply.enums.EmailType;
import com.logistics.supply.model.Employee;
import com.logistics.supply.model.Floats;
import com.logistics.supply.service.EmployeeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring5.SpringTemplateEngine;

import javax.validation.constraints.Email;
import java.text.MessageFormat;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static com.logistics.supply.util.Constants.EMPLOYEE_FLOAT_ENDORSED_MAIL;
import static com.logistics.supply.util.Constants.REQUEST_GM_APPROVAL_OF_FLOAT;

@Slf4j
@Component
@RequiredArgsConstructor
@PropertySource("classpath:application.properties")
public class FloatListener {

  final EmailSender emailSender;
  final SpringTemplateEngine templateEngine;
  final EmployeeService employeeService;

  @Value("${config.mail.template}")
  String FLOAT_ENDORSE_EMAIL;

  @Async
  @EventListener
  public void sendHODEmail(FloatEvent floatEvent) {
    log.info("Send endorse float mail to HOD");
    String title = "FLOAT ENDORSEMENT";
    String message = "Kindly review this float request pending endorsement";
    if (FLOAT_ENDORSE_EMAIL == null) return;
    String emailContent = composeEmail(title, message, FLOAT_ENDORSE_EMAIL);
    try {
      Employee employee =
          employeeService.getDepartmentHOD(
              floatEvent.getFloatOrder().getCreatedBy().getDepartment());
      emailSender.sendMail(employee.getEmail(), EmailType.FLOAT_ENDORSEMENT_EMAIL, emailContent);
    } catch (Exception e) {
      log.error(e.toString());
    }
  }

  @Async
  @EventListener(condition = "#floatEvent.getFloatOrder().getApproval() eq APPROVED")
  public void sendRequesterEmail(FloatEvent floatEvent) {
    String email = floatEvent.getFloatOrder().getCreatedBy().getEmail();
    log.info("Send approved float mail to requester: {}", email);
    String title = "FLOAT APPROVAL";

    String message =
        MessageFormat.format(
            "Kindly note that this float request, {0}, has been approved by the General Manager.",
            floatEvent.getFloatOrder().getDescription());
    if (FLOAT_ENDORSE_EMAIL == null) return;
    String emailContent = composeEmail(title, message, FLOAT_ENDORSE_EMAIL);
    try {
      emailSender.sendMail(
              email,
          EmailType.FLOAT_GM_APPROVAL,
          emailContent);
    } catch (Exception e) {
      log.error(e.toString());
    }
  }

  @Async
  @Transactional
  @EventListener(condition = "#floatEvent.isEndorsed == 'ENDORSED'")
  public void handleEndorseFloatsEvent(FloatEvent floatEvent) {
    log.info("Send mail to notify float endorsement completion ");
    Map<@Email String, String> requesters =
        floatEvent.getFloatOrder().getFloats().stream()
            .map(Floats::getCreatedBy)
            .collect(
                Collectors.toMap(
                        Employee::getEmail,
                        Employee::getLastName,
                    (existingValue, newValue) -> existingValue));

    String emailToGM =
        composeEmail("PENDING FLOATS", REQUEST_GM_APPROVAL_OF_FLOAT, FLOAT_ENDORSE_EMAIL);

    String generalManagerMail = employeeService.getGeneralManager().getEmail();

    CompletableFuture<String> hasSentEmailToGMAndRequesters =
        CompletableFuture.supplyAsync(
                () -> {
                  try {
                    emailSender.sendMail(generalManagerMail, EmailType.NEW_REQUEST, emailToGM);
                    return true;
                  } catch (Exception e) {
                    log.error(e.getMessage());
                    throw new IllegalStateException(e);
                  }
                })
            .thenCompose(
                passed ->
                    CompletableFuture.supplyAsync(
                        () -> {
                          if (passed) {
                            requesters.forEach(
                                (email, name) -> {
                                  String emailToRequester =
                                      composeEmail(
                                          "FLOAT ENDORSED",
                                          EMPLOYEE_FLOAT_ENDORSED_MAIL,
                                          FLOAT_ENDORSE_EMAIL);
                                  try {
                                    emailSender.sendMail(
                                        email,
                                        EmailType.FLOAT_ENDORSED_EMAIL_TO_EMPLOYEE,
                                        emailToRequester);
                                    log.info("Email sent to procurement and employee: " + name);
                                  } catch (Exception e) {
                                    log.error(e.getMessage());
                                    throw new IllegalStateException(e);
                                  }
                                });
                          }

                          return "Email sent to procurement and employee";
                        }));

    log.info(String.valueOf(hasSentEmailToGMAndRequesters));
  }

  private String composeEmail(String title, String message, String template) {
    Context context = new Context();
    context.setVariable("title", title);
    context.setVariable("message", message);
    return templateEngine.process(template, context);
  }
}
