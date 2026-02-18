package com.logistics.supply.event.listener;

import com.logistics.supply.enums.EmailType;
import com.logistics.supply.model.Employee;
import com.logistics.supply.model.EmployeeRole;
import com.logistics.supply.model.PaymentDraftComment;
import com.logistics.supply.service.EmployeeService;
import com.logistics.supply.util.EmailSenderUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import jakarta.persistence.PostPersist;
import java.text.MessageFormat;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Component
public class PaymentDraftCommentListener {
  private final EmailSenderUtil emailSenderUtil;
  private final EmployeeService employeeService;

  @Value("${config.templateMail}")
  String newCommentEmail;

  public PaymentDraftCommentListener(EmailSenderUtil emailSenderUtil, @Lazy EmployeeService employeeService) {
    this.emailSenderUtil = emailSenderUtil;
    this.employeeService = employeeService;
  }

  @PostPersist
  public void sendPaymentDraftComment(PaymentDraftComment comment) {
    log.info("======= EMAIL ON PAYMENT DRAFT COMMENT ==========");
    String title = "PAYMENT DRAFT COMMENT";
    String purchaseNumber = comment.getPayment().getPurchaseNumber();
    String message =
        MessageFormat.format(
            "{0} has commented on your payment with PN: {1}",
            comment.getEmployee().getFullName(), purchaseNumber);
    CompletableFuture.runAsync(
        () -> {
          String mailTo = "";
          switch (comment.getProcessWithComment()) {
            case REVIEW_PAYMENT_DRAFT_AUDITOR:
              mailTo = comment.getPayment().getCreatedBy()
                  .map(Employee::getEmail)
                  .orElse("");
              break;
            case REVIEW_PAYMENT_DRAFT_FM:
            case ACCOUNT_OFFICER_RESPONSE_TO_AUDITOR_COMMENT:
              mailTo =
                  employeeService.getManagerByRoleName(EmployeeRole.ROLE_AUDITOR.name()).getEmail();
              break;

            case REVIEW_PAYMENT_DRAFT_GM:
            case AUDITOR_RESPONSE_TO_FM_COMMENT:
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
