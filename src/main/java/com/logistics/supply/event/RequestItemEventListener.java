package com.logistics.supply.event;

import com.logistics.supply.email.EmailSender;
import com.logistics.supply.enums.EmailType;
import com.logistics.supply.model.Employee;
import com.logistics.supply.service.EmployeeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

import static com.logistics.supply.util.CommonHelper.buildEmail;
import static com.logistics.supply.util.Constants.*;

@Component
public class RequestItemEventListener {

  private final EmailSender emailSender;
  @Autowired private EmployeeService employeeService;

  public RequestItemEventListener(EmailSender emailSender) {
    this.emailSender = emailSender;
  }

  @Async
  @EventListener
  public void handleRequestItemEvent(BulkRequestItemEvent requestItemEvent) throws Exception {
    Employee hod =
        requestItemEvent.getRequestItems().stream()
            .map(x -> x.getEmployee().getDepartment())
            .limit(1)
            .map(employeeService::getDepartmentHOD)
            .findFirst()
            .orElseThrow(Exception::new);

    String emailContent =
        buildEmail(
            hod.getLastName(),
            REQUEST_PENDING_ENDORSEMENT_LINK,
            REQUEST_PENDING_ENDORSEMENT_TITLE,
            REQUEST_ENDORSEMENT_MAIL);

    CompletableFuture.runAsync(
        () -> {
          try {
            emailSender.sendMail(hod.getEmail(), EmailType.NEW_REQUEST_MAIL, emailContent);
          } catch (Exception e) {
            e.printStackTrace();
          }
        });
  }

  @Async
  @EventListener(condition = "#requestItemEvent.isEndorsed == 'ENDORSED'")
  public void handleEndorseRequestItemEvent(BulkRequestItemEvent requestItemEvent) {
    System.out.println("=============== ENDORSEMENT COMPLETE ================");
    String emailContent =
        buildEmail(
            "PROCUREMENT",
            REQUEST_PENDING_PROCUREMENT_DETAILS_LINK,
            REQUEST_PENDING_PROCUREMENT_DETAILS_TITLE,
            REQUEST_ENDORSEMENT_MAIL);

    String emailToEmployee = "";

    CompletableFuture.runAsync(
        () -> {
          try {
            emailSender.sendMail(
                DEFAULT_PROCUREMENT_MAIL, EmailType.NEW_REQUEST_MAIL, emailContent);
          } catch (Exception e) {
            e.printStackTrace();
          }
        });
  }
}
