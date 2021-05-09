package com.logistics.supply.event;

import com.logistics.supply.email.EmailSender;
import com.logistics.supply.enums.EmailType;
import com.logistics.supply.model.Employee;
import com.logistics.supply.model.RequestItem;
import com.logistics.supply.service.EmployeeService;
import com.logistics.supply.util.Constants;
import com.logistics.supply.util.EmailComposer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import javax.validation.constraints.Email;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static com.logistics.supply.util.Constants.*;

@Component
public class ApproveRequestItemEventListener {

  private final EmailSender emailSender;
  @Autowired private EmployeeService employeeService;

  public ApproveRequestItemEventListener(EmailSender emailSender) {
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

  private static String buildHtmlTableForRequestItems(List<String> title, List<RequestItem> items) {
    StringBuilder header = new StringBuilder();
    for (String t : title) header.append(String.format(tableHeader, t));
    header = new StringBuilder(String.format(tableRow, header));
    String ri =
        items.stream()
            .map(
                i ->
                    String.format(tableData, i.getName())
                        + String.format(tableData, i.getQuantity())
                        + String.format(tableData, i.getReason())
                        + String.format(tableData, i.getPurpose()))
            .map(j -> String.format(tableRow, j))
            .collect(Collectors.joining("", "", ""));
    return header.toString().concat(ri);
  }

  @Async
  @EventListener
  public void handleApproval(ApproveRequestItemEvent requestItemEvent) throws Exception {
    System.out.println("=============== APPROVAL BY GM COMPLETE ================");

    Employee hod =
        requestItemEvent.getRequestItems().stream()
            .map(x -> x.getEmployee().getDepartment())
            .limit(1)
            .map(employeeService::getDepartmentHOD)
            .findFirst()
            .orElseThrow(Exception::new);

    Map<@Email String, String> requesters =
        requestItemEvent.getRequestItems().stream()
            .map(x -> x.getEmployee())
            .collect(
                Collectors.toMap(
                    e -> e.getEmail(),
                    e -> e.getLastName(),
                    (existingValue, newValue) -> existingValue));

    Map<@Email String, List<RequestItem>> empRequests =
        requestItemEvent.getRequestItems().stream()
            .map(x -> x.getEmployee())
            .collect(Collectors.toMap(e -> e.getEmail(), e -> requestItemEvent.getRequestItems()));

    /**
     * ? TODO Send employee email about request approval by general manager Send hod email on
     * approval of request by general manager
     */
    CompletableFuture<String> hasSentApprovalMailToRequester =
        CompletableFuture.supplyAsync(
                () -> {
                  try {
                    empRequests
                        .keySet()
                        .forEach(
                            a -> {
                              String requestHtmlTable =
                                  buildHtmlTableForRequestItems(
                                      requestItemTableTitleList(), empRequests.get(a));
                              String emailContent =
                                  EmailComposer.buildEmailWithTable(
                                      "REQUEST APPROVAL",
                                      REQUEST_APPROVAL_MAIL_TO_EMPLOYEE,
                                      requestHtmlTable);
                              emailSender.sendMail(
                                  a, EmailType.APPROVED_REQUEST_MAIL, emailContent);
                            });
                  } catch (Exception e) {
                    e.printStackTrace();
                    throw new IllegalStateException(e);
                  }
                  return "Approval email sent to respective employees";
                })
            .thenCompose(
                next ->
                    CompletableFuture.supplyAsync(
                        () -> {
                          try {
                            String requestHtmlTable =
                                buildHtmlTableForRequestItems(
                                    requestItemTableTitleList(),
                                    requestItemEvent.getRequestItems());
                            String emailContent =
                                EmailComposer.buildEmailWithTable(
                                    "REQUEST APPROVAL",
                                    REQUEST_APPROVAL_MAIL_TO_EMPLOYEE,
                                    requestHtmlTable);
                            emailSender.sendMail(
                                hod.getEmail(), EmailType.APPROVED_REQUEST_MAIL, emailContent);
                            emailSender.sendMail(
                                DEFAULT_PROCUREMENT_MAIL,
                                EmailType.APPROVED_REQUEST_MAIL,
                                emailContent);

                          } catch (Exception e) {
                            e.printStackTrace();
                            throw new IllegalStateException(e);
                          }
                          return "Email sent to HOD & Procurement";
                        }));
  }
}
