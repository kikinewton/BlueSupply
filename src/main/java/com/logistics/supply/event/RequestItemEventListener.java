package com.logistics.supply.event;

import com.logistics.supply.email.EmailSender;
import com.logistics.supply.enums.EmailType;
import com.logistics.supply.model.Employee;
import com.logistics.supply.model.RequestItem;
import com.logistics.supply.service.EmployeeService;
import com.logistics.supply.util.HibernateUtil;
import com.logistics.supply.util.IdentifierUtil;
import lombok.RequiredArgsConstructor;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.PostPersist;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaUpdate;
import javax.persistence.criteria.Root;
import javax.validation.constraints.Email;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static com.logistics.supply.util.CommonHelper.buildEmail;
import static com.logistics.supply.util.CommonHelper.buildNewHtmlEmail;
import static com.logistics.supply.util.Constants.*;

@Component
@RequiredArgsConstructor
public class RequestItemEventListener {

  private final EmailSender emailSender;
  private final EmployeeService employeeService;

//  public RequestItemEventListener(EmailSender emailSender) {
//    this.emailSender = emailSender;
//  }

//  @PostPersist
//  public void setRequestItemRef(RequestItem requestItem) {
//    Session session = HibernateUtil.getHibernateSession();
//    CriteriaBuilder cb = session.getCriteriaBuilder();
//    //      CriteriaQuery<RequestItem> cr = cb.createQuery(RequestItem.class);
//    CriteriaUpdate<RequestItem> criteriaUpdate = cb.createCriteriaUpdate(RequestItem.class);
//    Root<RequestItem> root = criteriaUpdate.from(RequestItem.class);
//    String ref =
//        IdentifierUtil.idHandler(
//            "RQ", requestItem.getUserDepartment().getName(), requestItem.getId().toString());
//    criteriaUpdate.set("requestItemRef", ref);
//    criteriaUpdate.where(cb.equal(root.get("id"), requestItem.getId()));
//
//    Transaction transaction = session.beginTransaction();
//    session.createQuery(criteriaUpdate).executeUpdate();
//    transaction.commit();
//  }

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
  @Transactional
  @EventListener(condition = "#requestItemEvent.isEndorsed == 'ENDORSED'")
  public void handleEndorseRequestItemEvent(BulkRequestItemEvent requestItemEvent) {
    System.out.println("=============== ENDORSEMENT COMPLETE ================");
    Map<@Email String, String> requesters =
        requestItemEvent.getRequestItems().stream()
            .map(x -> x.getEmployee())
            .collect(
                Collectors.toMap(
                    e -> e.getEmail(),
                    e -> e.getLastName(),
                    (existingValue, newValue) -> existingValue));

    String emailToProcurement =
        buildNewHtmlEmail(
            REQUEST_PENDING_PROCUREMENT_DETAILS_LINK, "PROCUREMENT", PROCUREMENT_DETAILS_MAIL);

    CompletableFuture<String> hasSentEmailToProcurementAndRequesters =
        CompletableFuture.supplyAsync(
                () -> {
                  try {
                    emailSender.sendMail(
                        DEFAULT_PROCUREMENT_MAIL, EmailType.NEW_REQUEST_MAIL, emailToProcurement);
                    return true;
                  } catch (Exception e) {
                    e.printStackTrace();
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
                                      buildNewHtmlEmail("", name, EMPLOYEE_REQUEST_ENDORSED_MAIL);
                                  try {
                                    emailSender.sendMail(
                                        email,
                                        EmailType.NOTIFY_EMPLOYEE_OF_ENDORSEMENT_MAIL,
                                        emailToRequester);
                                    System.out.println(
                                        "EMAIL SENT TO PROCUREMENT AND EMPLOYEE: " + name);
                                  } catch (Exception e) {
                                    e.printStackTrace();
                                    throw new IllegalStateException(e);
                                  }
                                });
                          }
                          return "EMAIL SENT TO PROCUREMENT AND EMPLOYEE";
                        }));

    System.out.println(hasSentEmailToProcurementAndRequesters + "!!");
  }
}
