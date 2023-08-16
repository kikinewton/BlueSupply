package com.logistics.supply.event.listener;

import com.logistics.supply.enums.EmailType;
import com.logistics.supply.model.Employee;
import com.logistics.supply.service.EmployeeService;
import com.logistics.supply.util.Constants;
import com.logistics.supply.util.EmailSenderUtil;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import com.logistics.supply.model.RequestItem;

import java.text.MessageFormat;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class HodReviewListener {
  private final EmployeeService employeeService;
  private final EmailSenderUtil emailSenderUtil;

  @Value("${procurement.defaultMail}")
  String emailTemplate;

  @Async
  @EventListener(condition = "#hodReviewEvent.getIsHodReview() == 'HOD_REVIEW'")
  public void sendMailToHod(HodReviewEvent hodReviewEvent) {
    log.info("===== SEND MAIL TO GM AFTER HOD HAS REVIEWED QUOTATION =====");
    Employee gm = employeeService.getGeneralManager();
    String message =
        MessageFormat.format(
            "Dear {0}, You have received requests pending approval", gm.getFullName());

    emailSenderUtil.sendComposeAndSendEmail(
            Constants.REQUEST_PENDING_APPROVAL_TITLE,
            message,
            emailTemplate,
            EmailType.HOD_REVIEW_QUOTATION,
            gm.getEmail());

    log.info("Email HOD review sent");
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

}
