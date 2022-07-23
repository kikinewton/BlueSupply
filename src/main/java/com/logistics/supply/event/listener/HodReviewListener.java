package com.logistics.supply.event.listener;

import com.logistics.supply.enums.EmailType;
import com.logistics.supply.model.Employee;
import com.logistics.supply.model.RequestItem;
import com.logistics.supply.service.EmployeeService;
import com.logistics.supply.util.EmailSenderUtil;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.text.MessageFormat;
import java.util.List;

import static com.logistics.supply.util.Constants.REQUEST_PENDING_APPROVAL_TITLE;

@Slf4j
@Component
@RequiredArgsConstructor
public class HodReviewListener {
  private final EmployeeService employeeService;
  private final EmailSenderUtil emailSenderUtil;

  @Value("${procurement.defaultMail}")
  String emailTemplate;

//  @EventListener(condition = "#quotationHodReviewEvent.isReview() == true")
//  public void sendQuotationReviewMail(QuotationHodReviewEvent quotationHodReviewEvent) {
//    String quotationRef = quotationHodReviewEvent.getQuotation().getQuotationRef();
//    log.debug("===== SEND MAIL TO PROCUREMENT MANAGER =====");
//    Employee procurementManager =
//        employeeService.getManagerByRoleName(EmployeeRole.ROLE_PROCUREMENT_MANAGER.name());
//    String message =
//        MessageFormat.format(
//            "Dear {0}, Quotation with reference {1} has been reviewed.",
//            procurementManager.getFullName(), quotationRef);
//
//    String title = "QUOTATION HOD REVIEW";
//    CompletableFuture.runAsync(
//        () ->
//            emailSenderUtil.sendComposeAndSendEmail(
//                title,
//                message,
//                emailTemplate,
//                EmailType.HOD_REVIEW_QUOTATION,
//                procurementManager.getEmail()));
//  }

  @Async
  @EventListener(condition = "#hodReviewEvent.isHodReview() == 'HOD_REVIEW'")
  public void sendMailToHod(HodReviewEvent hodReviewEvent) {
    log.debug("===== SEND MAIL TO GM =====");
    Employee gm = employeeService.getGeneralManager();
    String message =
        MessageFormat.format(
            "Dear {0}, You have received requests pending approval", gm.getFullName());

    emailSenderUtil.sendComposeAndSendEmail(
            REQUEST_PENDING_APPROVAL_TITLE, message, emailTemplate, EmailType.HOD_REVIEW_QUOTATION, gm.getEmail());

    log.debug("Email HOD review sent");
  }


  @Getter
  public static class HodReviewEvent {
    private List<RequestItem> requestItems;
    private String isHodReview;

    public HodReviewEvent(List<RequestItem> requestItems) {
      this.requestItems = requestItems;
      this.isHodReview =
          requestItems.stream().map(RequestItem::getRequestReview).findFirst().get().toString();
    }
  }

//  @Getter
//  @Setter
//  public static final class QuotationHodReviewEvent extends ApplicationEvent {
//    private boolean review;
//    private Quotation quotation;
//
//    public QuotationHodReviewEvent(Object source, Quotation quotation) {
//      super(source);
//      this.quotation = quotation;
//      this.review = quotation.isReviewed();
//    }
//  }
}
