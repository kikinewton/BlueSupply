package com.logistics.supply.event.listener;

import com.logistics.supply.email.EmailSender;
import com.logistics.supply.enums.EmailType;
import com.logistics.supply.event.BulkRequestItemEvent;
import com.logistics.supply.model.Department;
import com.logistics.supply.model.Employee;
import com.logistics.supply.service.EmployeeService;
import com.logistics.supply.util.Constants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
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

@Slf4j
@Component
@RequiredArgsConstructor
public class RequestItemEventListener {

  private final EmailSender emailSender;
  private final EmployeeService employeeService;
  private final SpringTemplateEngine templateEngine;

  @Value("${config.templateMail}")
  public String emailTemplate;

  @Value("#{'${procurement.defaultMail}'}")
  String DEFAULT_PROCUREMENT_MAIL;

  @Async
  @EventListener(condition = "#requestItemEvent.isEndorsed == 'PENDING'")
  public void handleRequestItemEvent(BulkRequestItemEvent requestItemEvent)  {

    Department userDepartment =
        requestItemEvent.getRequestItems().stream().findFirst().get().getUserDepartment();

      log.info("Send email to HOD of department: {}", userDepartment.getName());

    Employee hod = employeeService.getDepartmentHOD(userDepartment);

    String message =
        MessageFormat.format(
            "Dear {0}, You have received requests pending endorsement", hod.getFullName());
    String content = composeEmail(Constants.REQUEST_PENDING_ENDORSEMENT_TITLE, message, emailTemplate);

    CompletableFuture.runAsync(
        () -> {
          try {
            emailSender.sendMail(hod.getEmail(), EmailType.NEW_REQUEST, content);
          } catch (Exception e) {
            log.error(e.getMessage());
          }
        });
  }

  @Async
  @Transactional
  @EventListener(condition = "#requestItemEvent.isEndorsed == 'ENDORSED'")
  public void handleEndorseRequestItemEvent(BulkRequestItemEvent requestItemEvent) {
    log.info("Send notifications for endorsement of request items");
    Map<@Email String, String> requesters =
        requestItemEvent.getRequestItems().stream()
            .map(x -> x.getEmployee())
            .collect(
                Collectors.toMap(
                    e -> e.getEmail(),
                    e -> e.getLastName(),
                    (existingValue, newValue) -> existingValue));

    String content =
        "Dear PROCUREMENT\n, Please note that you have endorsed request(s) pending procurement details";
    String emailToProcurement =
        composeEmail("PROCUREMENT DETAILS FOR LPO REQUEST", content, emailTemplate);


    CompletableFuture<String> hasSentEmailToProcurementAndRequesters =
        CompletableFuture.supplyAsync(
                () -> {
                  try {
                    emailSender.sendMail(
                        DEFAULT_PROCUREMENT_MAIL, EmailType.NEW_REQUEST, emailToProcurement);
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
                                          "REQUEST ENDORSEMENT",
                                          Constants.EMPLOYEE_REQUEST_ENDORSED_MAIL,
                                          emailTemplate);
                                  try {
                                    emailSender.sendMail(
                                        email,
                                        EmailType.NOTIFY_EMPLOYEE_OF_ENDORSEMENT_MAIL,
                                        emailToRequester);
                                    log.info("EMAIL SENT TO PROCUREMENT AND EMPLOYEE: " + name);
                                  } catch (Exception e) {
                                    log.error(e.getMessage());
                                    throw new IllegalStateException(e);
                                  }
                                });
                          }

                          return "EMAIL SENT TO PROCUREMENT AND EMPLOYEE";
                        }));

    log.info(hasSentEmailToProcurementAndRequesters + "!!");
  }

  private String composeEmail(String title, String message, String template) {
    Context context = new Context();
    context.setVariable("title", title);
    context.setVariable("message", message);
    return templateEngine.process(template, context);
  }
}
