package com.logistics.supply.event.listener;

import com.logistics.supply.email.EmailSender;
import com.logistics.supply.enums.EmailType;
import com.logistics.supply.model.Department;
import com.logistics.supply.model.LocalPurchaseOrderDraft;
import com.logistics.supply.model.Quotation;
import com.logistics.supply.model.RequestItem;
import com.logistics.supply.service.EmployeeService;
import com.logistics.supply.service.QuotationService;
import com.logistics.supply.service.RequestItemService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring5.SpringTemplateEngine;

import javax.persistence.PostPersist;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.logistics.supply.util.Constants.HOD_REVIEW_MAIL;

@Slf4j
public class LpoDraftEventListener {

  final EmailSender emailSender;
  final EmployeeService employeeService;
  private final QuotationService quotationService;
  private final RequestItemService requestItemService;
  @Autowired SpringTemplateEngine templateEngine;

  @Value("${config.templateMail}")
  String emailTemplate;

  public LpoDraftEventListener(
      @Lazy QuotationService quotationService,
      @Lazy RequestItemService requestItemService,
      @Lazy EmailSender emailSender,
      @Lazy EmployeeService employeeService) {
    this.quotationService = quotationService;
    this.requestItemService = requestItemService;
    this.employeeService = employeeService;
    this.emailSender = emailSender;
  }

  @Async
  @PostPersist
  public void expireQuotations(LocalPurchaseOrderDraft lpo) {
    try {
      /** Flag quotation related to lpo as linked */
      quotationService.updateLinkedToLPO(lpo.getQuotation().getId());

      /**
       * Loop through all quotations related to request items in lpo and invalidate(expire = true)
       * if all the request items associated to that quotations have unit price
       */
      List<Integer> requestItemIds =
          lpo.getRequestItems().stream().map(RequestItem::getId).collect(Collectors.toList());
      requestItemIds.forEach(System.out::println);
      if (requestItemService.priceNotAssigned(requestItemIds)) {
        log.info("=== Some items have not been assigned a final supplier ===");
      } else {
        log.info("=== All items been assigned a final supplier, expire related quotations ===");
        List<Quotation> l =
            quotationService.findNonExpiredNotLinkedToLPO(requestItemIds).stream()
                .map(
                    q -> {
                      q.setExpired(true);
                      return quotationService.save(q);
                    })
                .collect(Collectors.toList());
      }

      // send mail to HOD to review quotation
      String reviewQuotationEmail =
          composeEmail("QUOTATION REVIEW", HOD_REVIEW_MAIL, emailTemplate);

      Set<Department> departments = new HashSet<>();
      lpo.getRequestItems().forEach(r -> departments.add(r.getUserDepartment()));

      departments.stream()
          .map(d -> employeeService.getDepartmentHOD(d))
          .forEach(
              e -> {
                sendMail(reviewQuotationEmail, e.getEmail());
              });

    } catch (Exception e) {
      log.error(e.toString());
    }
  }

  private boolean sendMail(String reviewQuotationEmail, String hodEmail) {
    log.info("Send review quotation mail to: " + hodEmail);
    emailSender.sendMail(hodEmail, EmailType.HOD_REVIEW_QUOTATION, reviewQuotationEmail);
    return true;
  }

  private String composeEmail(String title, String message, String template) {
    Context context = new Context();
    context.setVariable("title", title);
    context.setVariable("message", message);
    return templateEngine.process(template, context);
  }
}
