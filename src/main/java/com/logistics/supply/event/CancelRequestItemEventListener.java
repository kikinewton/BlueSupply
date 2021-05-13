package com.logistics.supply.event;

import com.logistics.supply.email.EmailSender;
import com.logistics.supply.enums.EmailType;
import com.logistics.supply.model.Employee;
import com.logistics.supply.model.RequestItem;
import com.logistics.supply.service.EmployeeService;
import com.logistics.supply.service.RequestItemService;
import com.logistics.supply.util.EmailComposer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import javax.validation.constraints.Email;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static com.logistics.supply.util.CommonHelper.buildHtmlTableForRequestItems;
import static com.logistics.supply.util.Constants.REQUEST_CANCELLED_MAIL_TO_EMPLOYEE;

@Component
public class CancelRequestItemEventListener {

  private final EmailSender emailSender;
  @Autowired private EmployeeService employeeService;
  @Autowired private RequestItemService requestItemService;

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
  @EventListener
  public void handleCancelRequestItemEvent(CancelRequestItemEvent cancelRequestItemEvent) {
    List<Employee> employees =
        cancelRequestItemEvent.getCancelledRequestItems().stream()
            .map(c -> c.getEmployee())
            .collect(Collectors.toList());
    employees.forEach(System.out::println);

    List<RequestItem> items =
        cancelRequestItemEvent.getCancelledRequestItems().stream()
            .map(x -> x.getRequestItem())
            .collect(Collectors.toList());
    items.forEach(System.out::println);

    Map<@Email String, List<RequestItem>> empRequests = new HashMap<>();
    for (Employee employee : employees) {
      List<RequestItem> empItems = new ArrayList<>();
      for (RequestItem r : items) {
        if (r.getEmployee().getId() == employee.getId()) empItems.add(r);
      }
      empRequests.put(employee.getEmail(), empItems);
    }

    CompletableFuture<String> sendCancelledRequestMail = CompletableFuture.supplyAsync(()-> {
      try {
        empRequests.keySet().forEach(e -> {
          String requestHtmlTable =
                  buildHtmlTableForRequestItems(
                          requestItemTableTitleList(),
                          empRequests.get(e));
          String emailContent = EmailComposer.buildEmailWithTable("CANCELLED REQUEST ITEMS", REQUEST_CANCELLED_MAIL_TO_EMPLOYEE, requestHtmlTable);
          emailSender.sendMail(e, EmailType.CANCELLED_REQUEST_MAIL, emailContent);
        });

      }
      catch (Exception e) {
        e.printStackTrace();
        throw new IllegalStateException(e);
      }
      return "Cancelled request email sent";
    });

    System.out.println("sendCancelledRequestMail = " + sendCancelledRequestMail);
  }
}
