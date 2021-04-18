package com.logistics.supply.event;

import com.logistics.supply.email.EmailSender;
import com.logistics.supply.model.Employee;
import com.logistics.supply.model.RequestItem;
import com.logistics.supply.service.EmployeeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import javax.validation.constraints.Email;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class ApproveRequestItemEventListener {

  private final EmailSender emailSender;
  @Autowired private EmployeeService employeeService;

  public ApproveRequestItemEventListener(EmailSender emailSender) {
    this.emailSender = emailSender;
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


  }
}
