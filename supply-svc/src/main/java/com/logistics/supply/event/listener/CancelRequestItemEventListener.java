package com.logistics.supply.event.listener;

import com.logistics.supply.enums.EmailType;
import com.logistics.supply.model.Employee;
import com.logistics.supply.util.CommonHelper;
import com.logistics.supply.util.Constants;
import com.logistics.supply.util.EmailComposer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import javax.validation.constraints.Email;
import com.logistics.supply.email.EmailSender;
import com.logistics.supply.event.CancelRequestItemEvent;
import com.logistics.supply.model.CancelledRequestItem;
import com.logistics.supply.model.RequestItem;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Slf4j
@Component
public class CancelRequestItemEventListener {
  private final EmailSender emailSender;
  public CancelRequestItemEventListener(EmailSender emailSender) {
    this.emailSender = emailSender;
  }

  private List<String> requestItemTableTitleList() {
    List<String> title = new ArrayList<>();
    title.add("Description");
    title.add("Quantity");
    title.add("Reason");
    title.add("purpose");
    return title;
  }

  @Async
  @EventListener(condition = "cancelRequestItemEvent.getCancelledRequestItems().isEmpty() == false")
  public void handleCancelRequestItemEvent(CancelRequestItemEvent cancelRequestItemEvent) {
    List<Employee> employees =
        cancelRequestItemEvent.getCancelledRequestItems().stream()
            .map(CancelledRequestItem::getEmployee)
            .collect(Collectors.toList());

    List<RequestItem> items =
        cancelRequestItemEvent.getCancelledRequestItems().stream()
            .map(CancelledRequestItem::getRequestItem)
            .collect(Collectors.toList());

    Map<@Email String, List<RequestItem>> empRequests = new HashMap<>();
    for (Employee employee : employees) {
      List<RequestItem> empItems = new ArrayList<>();
      for (RequestItem r : items) {
        if (Objects.equals(r.getEmployee().getId(), employee.getId())) empItems.add(r);
      }
      empRequests.put(employee.getEmail(), empItems);
    }

     CompletableFuture.supplyAsync(()-> {
      try {
        empRequests.keySet().forEach(e -> {
          String requestHtmlTable =
                  CommonHelper.buildHtmlTableForRequestItems(
                          requestItemTableTitleList(),
                          empRequests.get(e));
          String emailContent = EmailComposer.buildEmailWithTable("CANCELLED REQUEST ITEMS", Constants.REQUEST_CANCELLED_MAIL_TO_EMPLOYEE, requestHtmlTable);
          emailSender.sendMail(e, EmailType.CANCELLED_REQUEST_MAIL, emailContent);
        });

      }
      catch (Exception e) {
        e.printStackTrace();
        throw new IllegalStateException(e);
      }
      return "Cancelled request email sent";
    });
    log.debug("Emails to for cancelled request items sent");
  }
}
