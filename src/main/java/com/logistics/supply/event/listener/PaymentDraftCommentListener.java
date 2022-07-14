package com.logistics.supply.event.listener;

import com.logistics.supply.enums.EmailType;
import com.logistics.supply.model.EmployeeRole;
import com.logistics.supply.model.PaymentDraft;
import com.logistics.supply.model.PaymentDraftComment;
import com.logistics.supply.service.EmployeeService;
import com.logistics.supply.util.EmailSenderUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.persistence.PostPersist;
import java.text.MessageFormat;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentDraftCommentListener {
  private final EmailSenderUtil emailSenderUtil;
  private final EmployeeService employeeService;

  @Value("${config.templateMail}")
  String newCommentEmail;

  @PostPersist
  public void sendPaymentDraftComment(PaymentDraftComment comment) {
    log.info("======= EMAIL 0N PAYMENT DRAFT COMMENT ==========");
    String title = "PAYMENT DRAFT COMMENT";
    PaymentDraft draft = comment.getPaymentDraft();
    String message =
        MessageFormat.format(
            "{0} has commented on your payment with PN: {1}",
            comment.getEmployee().getFullName(), comment.getPaymentDraft().getPurchaseNumber());
    CompletableFuture.runAsync(
        () -> {
          /**
           * based on the request process, forward the mail to the appropriate authority if
           * REVIEW_PAYMENT_DRAFT_AUDITOR -> send mail to Account officer REVIEW_PAYMENT_DRAFT_FM ->
           * send mail to Auditor REVIEW_PAYMENT_DRAFT_GM -> send mail to FM
           */
          String mailTo = "";
          switch (comment.getProcessWithComment()) {
            case REVIEW_PAYMENT_DRAFT_AUDITOR:
              mailTo = comment.getPaymentDraft().getCreatedBy().getEmail();
              break;
            case REVIEW_PAYMENT_DRAFT_FM, ACCOUNT_OFFICER_RESPONSE_TO_AUDITOR_COMMENT:
              mailTo =
                  employeeService.getManagerByRoleName(EmployeeRole.ROLE_AUDITOR.name()).getEmail();
              break;
            case REVIEW_PAYMENT_DRAFT_GM, AUDITOR_RESPONSE_TO_FM_COMMENT:
              mailTo =
                  employeeService
                      .getManagerByRoleName(EmployeeRole.ROLE_FINANCIAL_MANAGER.name())
                      .getEmail();
              break;
          }

          emailSenderUtil.sendComposeAndSendEmail(
              title, message, newCommentEmail, EmailType.PAYMENT_DRAFT_COMMENT, mailTo);
        });
  }
}
