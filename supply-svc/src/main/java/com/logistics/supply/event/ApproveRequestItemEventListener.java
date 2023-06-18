package com.logistics.supply.event;

import com.logistics.supply.email.EmailSender;
import com.logistics.supply.enums.EmailType;
import com.logistics.supply.model.Employee;
import com.logistics.supply.model.RequestItem;
import com.logistics.supply.service.EmployeeService;
import com.logistics.supply.util.CommonHelper;
import com.logistics.supply.util.Constants;
import com.logistics.supply.util.EmailComposer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import javax.validation.constraints.Email;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Slf4j
@Component
public class ApproveRequestItemEventListener {

  private final EmailSender emailSender;

  @Value("${procurement.defaultMail}")
  String defaultProcurementMail;

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

  @Async
  @EventListener(condition = "#requestItemEvent.getIsApproved() eq 'APPROVED'")
  public void handleApproval(ApproveRequestItemEvent requestItemEvent) throws Exception {
    log.debug("=============== APPROVAL BY GM COMPLETE ================");

    Employee hod =
        requestItemEvent.getRequestItems().stream()
            .map(x -> x.getEmployee().getDepartment())
            .limit(1)
            .map(department -> employeeService.getDepartmentHOD(department))
            .findFirst()
            .orElseThrow(Exception::new);

    Map<@Email String, List<RequestItem>> empRequests =
        requestItemEvent.getRequestItems().stream()
            .map(x -> x.getEmployee())
            .collect(Collectors.toMap(e -> e.getEmail(), e -> requestItemEvent.getRequestItems()));

    CompletableFuture<String> hasSentApprovalMailToRequester =
        CompletableFuture.supplyAsync(
                () -> {
                  try {
                    empRequests
                        .keySet()
                        .forEach(
                            a -> {
                              String requestHtmlTable =
                                  CommonHelper.buildHtmlTableForRequestItems(
                                      requestItemTableTitleList(), empRequests.get(a));
                              String emailContent =
                                  EmailComposer.buildEmailWithTable(
                                      "REQUEST APPROVAL",
                                      Constants.REQUEST_APPROVAL_MAIL_TO_EMPLOYEE,
                                      requestHtmlTable);
                              emailSender.sendMail(
                                  a, EmailType.APPROVED_REQUEST_MAIL, emailContent);
                            });
                  } catch (Exception e) {
                    log.error(e.getMessage());
                  }
                  return "Approval email sent to respective employees";
                })
            .thenCompose(
                next ->
                    CompletableFuture.supplyAsync(
                        () -> {
                          try {
                            String requestHtmlTable =
                                CommonHelper.buildHtmlTableForRequestItems(
                                    requestItemTableTitleList(),
                                    requestItemEvent.getRequestItems());
                            String emailContent =
                                EmailComposer.buildEmailWithTable(
                                    "REQUEST APPROVAL",
                                    Constants.REQUEST_APPROVAL_MAIL_TO_EMPLOYEE,
                                    requestHtmlTable);
                            emailSender.sendMail(
                                hod.getEmail(), EmailType.APPROVED_REQUEST_MAIL, emailContent);
                            emailSender.sendMail(
                                defaultProcurementMail,
                                EmailType.APPROVED_REQUEST_MAIL,
                                emailContent);

                          } catch (Exception e) {
                            log.error(e.getMessage());
                          }
                          return "Email sent to HOD & Procurement";
                        }));
  }
}
